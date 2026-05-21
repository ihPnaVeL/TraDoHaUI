package com.haui.devicemanagement.presenter;

import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.dao.BorrowItemDao;
import com.haui.devicemanagement.data.dao.BorrowTicketDao;
import com.haui.devicemanagement.data.dao.DeviceDetailDao;
import com.haui.devicemanagement.data.dao.NotificationDao;
import com.haui.devicemanagement.data.entity.BorrowItem;
import com.haui.devicemanagement.data.entity.BorrowTicket;
import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.DateUtils;
import com.haui.devicemanagement.util.TicketCodeGenerator;

import java.util.List;

/**
 * BorrowPresenter — xử lý toàn bộ logic nghiệp vụ mượn thiết bị.
 */
public class BorrowPresenter {

    public interface BorrowListView {
        void onTicketsLoaded(List<BorrowTicket> tickets);
        void onError(String message);
    }

    public interface BorrowActionView {
        void onSuccess(String message);
        void onError(String message);
    }

    public interface BorrowDetailView {
        void onTicketLoaded(BorrowTicket ticket, List<BorrowItem> items);
        void onError(String message);
    }

    // ─── Fields ────────────────────────────────────────────────────────────────
    private final BorrowTicketDao borrowTicketDao;
    private final BorrowItemDao   borrowItemDao;
    private final DeviceDetailDao deviceDetailDao;
    private final NotificationDao notificationDao;

    public BorrowPresenter(DatabaseHelper dbHelper) {
        this.borrowTicketDao = new BorrowTicketDao(dbHelper);
        this.borrowItemDao   = new BorrowItemDao(dbHelper);
        this.deviceDetailDao = new DeviceDetailDao(dbHelper);
        this.notificationDao = new NotificationDao(dbHelper);
    }

    // ─── LOAD TICKETS BY USER ──────────────────────────────────────────────────

    public void loadUserTickets(int userId, BorrowListView view) {
        List<BorrowTicket> list = borrowTicketDao.getByUserId(userId);
        view.onTicketsLoaded(list);
    }

    public void loadUserTicketsByStatus(int userId, String status, BorrowListView view) {
        List<BorrowTicket> all = borrowTicketDao.getByUserId(userId);
        if (status == null || status.isEmpty()) {
            view.onTicketsLoaded(all);
            return;
        }
        all.removeIf(t -> !status.equals(t.getStatus()));
        view.onTicketsLoaded(all);
    }

    // ─── LOAD PENDING (for admin) ──────────────────────────────────────────────

    public void loadPendingTickets(BorrowListView view) {
        List<BorrowTicket> list = borrowTicketDao.getPendingTickets();
        view.onTicketsLoaded(list);
    }

    public void loadAllTickets(BorrowListView view) {
        // Admin xem tất cả — lấy theo thứ tự mới nhất trước
        List<BorrowTicket> list = borrowTicketDao.getByStatus(Constants.BORROW_PENDING);
        list.addAll(borrowTicketDao.getByStatus(Constants.BORROW_BORROWED));
        view.onTicketsLoaded(list);
    }

    // ─── LOAD DETAIL ───────────────────────────────────────────────────────────

    public void loadTicketDetail(int ticketId, BorrowDetailView view) {
        BorrowTicket ticket = borrowTicketDao.getById(ticketId);
        if (ticket == null) {
            view.onError("Không tìm thấy phiếu mượn");
            return;
        }
        List<BorrowItem> items = borrowItemDao.getByTicketId(ticketId);
        view.onTicketLoaded(ticket, items);
    }

    // ─── CREATE BORROW TICKET ──────────────────────────────────────────────────

    /**
     * Sinh viên tạo phiếu mượn.
     * @param userId        ID sinh viên.
     * @param deviceIds     Danh sách device_id (loại thiết bị) muốn mượn.
     * @param reason        Lý do mượn.
     * @param expectedDate  Hạn trả dự kiến (yyyy-MM-dd).
     */
    public void createBorrowTicket(int userId, List<Integer> deviceIds,
                                    String reason, String expectedDate,
                                    BorrowActionView view) {
        // Validate
        if (deviceIds == null || deviceIds.isEmpty()) {
            view.onError("Vui lòng chọn ít nhất một loại thiết bị");
            return;
        }
        if (reason == null || reason.trim().isEmpty()) {
            view.onError("Vui lòng nhập lý do mượn");
            return;
        }
        if (expectedDate == null || expectedDate.isEmpty()) {
            view.onError("Vui lòng chọn ngày trả dự kiến");
            return;
        }
        if (DateUtils.isOverdue(expectedDate)) {
            view.onError("Ngày trả dự kiến phải sau ngày hôm nay");
            return;
        }

        // Tạo BorrowTicket
        BorrowTicket ticket = new BorrowTicket();
        ticket.setTicketCode(TicketCodeGenerator.generateBorrowCode());
        ticket.setUserId(userId);
        ticket.setStatus(Constants.BORROW_PENDING);
        ticket.setBorrowReason(reason.trim());
        ticket.setExpectedReturnDate(expectedDate);
        ticket.setCreatedAt(DateUtils.getCurrentDateTime());

        // Tạo BorrowItems (chưa có device_detail — admin sẽ gán sau)
        java.util.List<BorrowItem> items = new java.util.ArrayList<>();
        for (int deviceId : deviceIds) {
            BorrowItem item = new BorrowItem();
            item.setNote("Yêu cầu thiết bị loại ID: " + deviceId);
            // Lưu deviceId tạm vào note vì device_detail_id chưa có
            // Thực tế: có thể mở rộng thêm cột device_type_id vào borrow_items
            items.add(item);
        }

        long id = borrowTicketDao.createBorrowTicket(ticket, items);
        if (id == -1) {
            view.onError("Tạo phiếu mượn thất bại. Vui lòng thử lại.");
            return;
        }

        // Gửi notification cho tất cả admin (gửi cho receiver_id=0 = broadcast)
        notificationDao.sendNotification(
            Constants.ACCOUNT_ADMIN, 1,
            Constants.NOTIF_BORROW,
            "Phiếu mượn mới",
            "Sinh viên vừa tạo phiếu mượn " + ticket.getTicketCode() + ". Cần duyệt.",
            (int) id, Constants.REF_BORROW
        );

        view.onSuccess("Tạo phiếu mượn " + ticket.getTicketCode() + " thành công! Chờ admin duyệt.");
    }

    // ─── APPROVE TICKET ────────────────────────────────────────────────────────

    public void approveTicket(int ticketId, int adminId, String note, BorrowActionView view) {
        BorrowTicket ticket = borrowTicketDao.getById(ticketId);
        if (ticket == null) {
            view.onError("Không tìm thấy phiếu mượn");
            return;
        }
        if (!Constants.BORROW_PENDING.equals(ticket.getStatus())) {
            view.onError("Phiếu không ở trạng thái chờ duyệt");
            return;
        }

        borrowTicketDao.approveTicket(ticketId, adminId, note);

        // Notify user
        notificationDao.sendNotification(
            Constants.ACCOUNT_USER, ticket.getUserId(),
            Constants.NOTIF_BORROW,
            "Phiếu mượn được duyệt",
            "Phiếu " + ticket.getTicketCode() + " đã được duyệt. Admin sẽ sớm giao thiết bị.",
            ticketId, Constants.REF_BORROW
        );

        view.onSuccess("Đã duyệt phiếu " + ticket.getTicketCode());
    }

    // ─── REJECT TICKET ─────────────────────────────────────────────────────────

    public void rejectTicket(int ticketId, int adminId, String note, BorrowActionView view) {
        if (note == null || note.trim().isEmpty()) {
            view.onError("Vui lòng nhập lý do từ chối");
            return;
        }
        BorrowTicket ticket = borrowTicketDao.getById(ticketId);
        if (ticket == null) {
            view.onError("Không tìm thấy phiếu mượn");
            return;
        }

        borrowTicketDao.rejectTicket(ticketId, adminId, note);

        // Notify user
        notificationDao.sendNotification(
            Constants.ACCOUNT_USER, ticket.getUserId(),
            Constants.NOTIF_BORROW,
            "Phiếu mượn bị từ chối",
            "Phiếu " + ticket.getTicketCode() + " bị từ chối. Lý do: " + note,
            ticketId, Constants.REF_BORROW
        );

        view.onSuccess("Đã từ chối phiếu " + ticket.getTicketCode());
    }

    // ─── ASSIGN DEVICE ─────────────────────────────────────────────────────────

    /**
     * Admin gán thiết bị cụ thể vào borrow_item, cập nhật device_detail.availability_status.
     */
    public void assignDevice(int borrowItemId, int deviceDetailId,
                              String conditionOut, String accessoriesOut,
                              int ticketId, int adminId,
                              BorrowActionView view) {
        // Cập nhật borrow_item
        borrowItemDao.assignDeviceDetail(borrowItemId, deviceDetailId, conditionOut, accessoriesOut);

        // Cập nhật device_detail → borrowed
        deviceDetailDao.updateAvailabilityStatus(deviceDetailId, Constants.DEVICE_BORROWED);

        // Kiểm tra tất cả borrow_items đã được gán chưa
        List<BorrowItem> items = borrowItemDao.getByTicketId(ticketId);
        boolean allAssigned = true;
        for (BorrowItem item : items) {
            if (!item.isAssigned()) { allAssigned = false; break; }
        }

        if (allAssigned) {
            // Chuyển trạng thái phiếu → borrowed
            borrowTicketDao.updateStatus(ticketId, Constants.BORROW_BORROWED);
            BorrowTicket ticket = borrowTicketDao.getById(ticketId);
            if (ticket != null) {
                notificationDao.sendNotification(
                    Constants.ACCOUNT_USER, ticket.getUserId(),
                    Constants.NOTIF_BORROW,
                    "Thiết bị đã sẵn sàng",
                    "Phiếu " + ticket.getTicketCode() + " đã được giao thiết bị. Vui lòng đến nhận.",
                    ticketId, Constants.REF_BORROW
                );
            }
            view.onSuccess("Đã gán thiết bị. Phiếu chuyển sang 'Đang mượn'.");
        } else {
            view.onSuccess("Đã gán thiết bị cho dòng này.");
        }
    }

    // ─── OVERDUE TICKETS ───────────────────────────────────────────────────────

    public void loadOverdueTickets(BorrowListView view) {
        String today = DateUtils.getCurrentDate();
        List<BorrowTicket> list = borrowTicketDao.getOverdueTickets(today);

        // Tự động cập nhật status → overdue
        for (BorrowTicket t : list) {
            if (Constants.BORROW_BORROWED.equals(t.getStatus())) {
                borrowTicketDao.updateStatus(t.getId(), Constants.BORROW_OVERDUE);
                t.setStatus(Constants.BORROW_OVERDUE);
                // Notify user
                notificationDao.sendNotification(
                    Constants.ACCOUNT_USER, t.getUserId(),
                    Constants.NOTIF_OVERDUE,
                    "Phiếu mượn quá hạn",
                    "Phiếu " + t.getTicketCode() + " đã quá hạn trả. Vui lòng trả thiết bị ngay!",
                    t.getId(), Constants.REF_BORROW
                );
            }
        }
        view.onTicketsLoaded(list);
    }
}
