package com.haui.devicemanagement.presenter;

import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.dao.BorrowItemDao;
import com.haui.devicemanagement.data.dao.BorrowTicketDao;
import com.haui.devicemanagement.data.dao.DeviceDetailDao;
import com.haui.devicemanagement.data.dao.NotificationDao;
import com.haui.devicemanagement.data.dao.ReturnItemDao;
import com.haui.devicemanagement.data.dao.ReturnTicketDao;
import com.haui.devicemanagement.data.entity.BorrowItem;
import com.haui.devicemanagement.data.entity.ReturnItem;
import com.haui.devicemanagement.data.entity.ReturnTicket;
import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.DateUtils;
import com.haui.devicemanagement.util.TicketCodeGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * ReturnPresenter — xử lý toàn bộ logic nghiệp vụ trả thiết bị.
 */
public class ReturnPresenter {

    public interface ReturnListView {
        void onTicketsLoaded(List<ReturnTicket> tickets);
        void onError(String message);
    }

    public interface ReturnDetailView {
        void onTicketLoaded(ReturnTicket ticket, List<ReturnItem> items);
        void onError(String message);
    }

    public interface ReturnActionView {
        void onSuccess(String message);
        void onError(String message);
    }

    private final ReturnTicketDao  returnTicketDao;
    private final ReturnItemDao    returnItemDao;
    private final BorrowTicketDao  borrowTicketDao;
    private final BorrowItemDao    borrowItemDao;
    private final DeviceDetailDao  deviceDetailDao;
    private final NotificationDao  notificationDao;

    public ReturnPresenter(DatabaseHelper dbHelper) {
        this.returnTicketDao = new ReturnTicketDao(dbHelper);
        this.returnItemDao   = new ReturnItemDao(dbHelper);
        this.borrowTicketDao = new BorrowTicketDao(dbHelper);
        this.borrowItemDao   = new BorrowItemDao(dbHelper);
        this.deviceDetailDao = new DeviceDetailDao(dbHelper);
        this.notificationDao = new NotificationDao(dbHelper);
    }

    // ─── LOAD BY USER ──────────────────────────────────────────────────────────

    public void loadUserReturnTickets(int userId, ReturnListView view) {
        List<ReturnTicket> list = returnTicketDao.getByUserId(userId);
        view.onTicketsLoaded(list);
    }

    // ─── LOAD PENDING (admin) ─────────────────────────────────────────────────

    public void loadPendingReturns(ReturnListView view) {
        List<ReturnTicket> list = returnTicketDao.getPendingReturns();
        view.onTicketsLoaded(list);
    }

    // ─── LOAD DETAIL ───────────────────────────────────────────────────────────

    public void loadReturnDetail(int returnTicketId, ReturnDetailView view) {
        ReturnTicket ticket = returnTicketDao.getById(returnTicketId);
        if (ticket == null) {
            view.onError("Không tìm thấy phiếu trả");
            return;
        }
        List<ReturnItem> items = returnItemDao.getByReturnTicketId(returnTicketId);
        view.onTicketLoaded(ticket, items);
    }

    // ─── CREATE RETURN TICKET ──────────────────────────────────────────────────

    /**
     * Sinh viên tạo phiếu trả từ phiếu mượn đang borrowed/overdue.
     */
    public void createReturnTicket(int userId, int borrowTicketId, ReturnActionView view) {
        // Lấy borrow_items
        List<BorrowItem> borrowItems = borrowItemDao.getByTicketId(borrowTicketId);
        if (borrowItems.isEmpty()) {
            view.onError("Phiếu mượn không có thiết bị nào");
            return;
        }

        // Kiểm tra chưa có phiếu trả pending/completed cho phiếu này
        List<ReturnTicket> existing = returnTicketDao.getByUserId(userId);
        for (ReturnTicket rt : existing) {
            if (rt.getBorrowTicketId() == borrowTicketId
                    && (Constants.RETURN_PENDING.equals(rt.getStatus())
                        || Constants.RETURN_COMPLETED.equals(rt.getStatus()))) {
                view.onError("Đã có phiếu trả đang xử lý cho phiếu mượn này");
                return;
            }
        }

        // Tạo ReturnTicket
        ReturnTicket returnTicket = new ReturnTicket();
        returnTicket.setTicketCode(TicketCodeGenerator.generateReturnCode());
        returnTicket.setBorrowTicketId(borrowTicketId);
        returnTicket.setUserId(userId);
        returnTicket.setStatus(Constants.RETURN_PENDING);
        returnTicket.setReturnedAt(DateUtils.getCurrentDateTime());

        // Tạo ReturnItems từ BorrowItems
        List<ReturnItem> returnItems = new ArrayList<>();
        for (BorrowItem bi : borrowItems) {
            if (bi.isAssigned()) {
                ReturnItem ri = new ReturnItem();
                ri.setBorrowItemId(bi.getId());
                ri.setDeviceDetailId(bi.getDeviceDetailId());
                ri.setIsCompleted(0);
                returnItems.add(ri);
            }
        }

        if (returnItems.isEmpty()) {
            view.onError("Không có thiết bị nào đã được gán để trả");
            return;
        }

        long id = returnTicketDao.createReturnTicket(returnTicket, returnItems);
        if (id == -1) {
            view.onError("Tạo phiếu trả thất bại");
            return;
        }

        // Notify admin
        notificationDao.sendNotification(
            Constants.ACCOUNT_ADMIN, 1,
            Constants.NOTIF_RETURN,
            "Phiếu trả mới",
            "Sinh viên đã gửi phiếu trả " + returnTicket.getTicketCode() + ". Cần xác nhận.",
            (int) id, Constants.REF_RETURN
        );

        view.onSuccess("Tạo phiếu trả " + returnTicket.getTicketCode() + " thành công!");
    }

    // ─── CONFIRM RETURN ITEM ───────────────────────────────────────────────────

    /**
     * Admin kiểm tra và xác nhận từng return_item.
     * Cập nhật device_detail theo tình trạng thiết bị.
     */
    public void confirmReturnItem(int returnItemId, int returnTicketId,
                                   String conditionIn, String accessoriesIn,
                                   String damageNote, int penaltyAmount,
                                   int adminId, ReturnActionView view) {
        // Cập nhật return_item
        returnItemDao.updateCheckResult(returnItemId, conditionIn, accessoriesIn,
                damageNote, penaltyAmount, 1);

        // Lấy return_item để biết device_detail_id
        ReturnItem ri = returnItemDao.getById(returnItemId);
        if (ri != null) {
            int deviceDetailId = ri.getDeviceDetailId();
            switch (conditionIn) {
                case Constants.CONDITION_GOOD:
                    deviceDetailDao.updateAvailabilityStatus(deviceDetailId, Constants.DEVICE_AVAILABLE);
                    break;
                case Constants.CONDITION_DAMAGED:
                    deviceDetailDao.updateAvailabilityStatus(deviceDetailId, Constants.DEVICE_MAINTENANCE);
                    deviceDetailDao.updateConditionStatus(deviceDetailId, Constants.CONDITION_DAMAGED);
                    break;
                case "lost":
                    deviceDetailDao.updateAvailabilityStatus(deviceDetailId, Constants.DEVICE_LOST);
                    break;
            }
        }

        // Kiểm tra toàn bộ items của return_ticket đã hoàn thành chưa
        if (returnItemDao.allItemsCompleted(returnTicketId)) {
            // Xác định final status của return_ticket
            String finalStatus;
            if (returnItemDao.hasLostItems(returnTicketId)) {
                finalStatus = Constants.RETURN_LOST;
            } else if (returnItemDao.hasDamagedOrLostItems(returnTicketId)) {
                finalStatus = Constants.RETURN_DAMAGED;
            } else {
                finalStatus = Constants.RETURN_COMPLETED;
            }

            returnTicketDao.confirmReturn(returnTicketId, adminId, finalStatus, null);

            // Cập nhật borrow_ticket
            ReturnTicket rt = returnTicketDao.getById(returnTicketId);
            if (rt != null) {
                List<ReturnItem> allItems = returnItemDao.getByReturnTicketId(returnTicketId);
                boolean allBorrowItemsReturned = checkAllBorrowItemsReturned(rt.getBorrowTicketId());
                if (allBorrowItemsReturned) {
                    borrowTicketDao.updateStatus(rt.getBorrowTicketId(), Constants.BORROW_RETURNED);
                } else {
                    borrowTicketDao.updateStatus(rt.getBorrowTicketId(), Constants.BORROW_PARTIALLY_RETURNED);
                }
                // Notify user
                notificationDao.sendNotification(
                    Constants.ACCOUNT_USER, rt.getUserId(),
                    Constants.NOTIF_RETURN,
                    "Phiếu trả đã xác nhận",
                    "Phiếu trả " + rt.getTicketCode() + " đã được xác nhận. Trạng thái: " + finalStatus,
                    returnTicketId, Constants.REF_RETURN
                );
            }
            view.onSuccess("Đã xác nhận toàn bộ. Trạng thái phiếu trả hoàn tất.");
        } else {
            view.onSuccess("Đã xác nhận thiết bị này.");
        }
    }

    // ─── PRIVATE HELPERS ───────────────────────────────────────────────────────

    private boolean checkAllBorrowItemsReturned(int borrowTicketId) {
        List<BorrowItem> borrowItems = borrowItemDao.getByTicketId(borrowTicketId);
        if (borrowItems.isEmpty()) return false;
        for (BorrowItem bi : borrowItems) {
            if (!bi.isAssigned()) return false;
        }
        return true;
    }
}
