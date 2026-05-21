package com.haui.devicemanagement.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.BorrowItem;
import com.haui.devicemanagement.data.entity.BorrowTicket;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng `borrow_tickets` và phối hợp với `borrow_items`.
 */
public class BorrowTicketDao {

    private final DatabaseHelper dbHelper;

    public BorrowTicketDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // ─── CREATE BORROW TICKET (transaction) ────────────────────────────────────

    /**
     * Tạo phiếu mượn + danh sách borrow_items trong cùng 1 transaction.
     * @return ID phiếu mượn mới, -1 nếu thất bại.
     */
    public long createBorrowTicket(BorrowTicket ticket, List<BorrowItem> items) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = toContentValues(ticket);
            long ticketId = db.insert(DatabaseHelper.TABLE_BORROW_TICKETS, null, cv);
            if (ticketId == -1) return -1;

            for (BorrowItem item : items) {
                item.setTicketId((int) ticketId);
                ContentValues itemCv = new ContentValues();
                itemCv.put("ticket_id",  item.getTicketId());
                // device_detail_id có thể null khi sinh viên tạo
                if (item.getDeviceDetailId() > 0) {
                    itemCv.put("device_detail_id", item.getDeviceDetailId());
                }
                itemCv.put("note", item.getNote());
                db.insert(DatabaseHelper.TABLE_BORROW_ITEMS, null, itemCv);
            }

            db.setTransactionSuccessful();
            return ticketId;
        } finally {
            db.endTransaction();
        }
    }

    // ─── GET BY ID ─────────────────────────────────────────────────────────────

    public BorrowTicket getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT bt.*, u.full_name AS user_full_name, u.mssv AS user_mssv, " +
            "a.full_name AS approved_by_name " +
            "FROM " + DatabaseHelper.TABLE_BORROW_TICKETS + " bt " +
            "LEFT JOIN " + DatabaseHelper.TABLE_USERS + " u ON bt.user_id = u.id " +
            "LEFT JOIN " + DatabaseHelper.TABLE_ADMIN + " a ON bt.approved_by = a.id " +
            "WHERE bt.id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)});
        BorrowTicket ticket = null;
        if (cursor.moveToFirst()) {
            ticket = cursorToTicket(cursor);
        }
        cursor.close();
        return ticket;
    }

    // ─── GET BY USER ID ────────────────────────────────────────────────────────

    public List<BorrowTicket> getByUserId(int userId) {
        List<BorrowTicket> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT bt.*, u.full_name AS user_full_name, u.mssv AS user_mssv, " +
            "a.full_name AS approved_by_name " +
            "FROM " + DatabaseHelper.TABLE_BORROW_TICKETS + " bt " +
            "LEFT JOIN " + DatabaseHelper.TABLE_USERS + " u ON bt.user_id = u.id " +
            "LEFT JOIN " + DatabaseHelper.TABLE_ADMIN + " a ON bt.approved_by = a.id " +
            "WHERE bt.user_id = ? ORDER BY bt.created_at DESC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        while (cursor.moveToNext()) {
            list.add(cursorToTicket(cursor));
        }
        cursor.close();
        return list;
    }

    // ─── GET PENDING TICKETS ───────────────────────────────────────────────────

    public List<BorrowTicket> getPendingTickets() {
        return getByStatus("pending");
    }

    // ─── GET BORROWED TICKETS BY USER ──────────────────────────────────────────

    public List<BorrowTicket> getBorrowedTicketsByUser(int userId) {
        List<BorrowTicket> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT bt.*, u.full_name AS user_full_name, u.mssv AS user_mssv, " +
            "a.full_name AS approved_by_name " +
            "FROM " + DatabaseHelper.TABLE_BORROW_TICKETS + " bt " +
            "LEFT JOIN " + DatabaseHelper.TABLE_USERS + " u ON bt.user_id = u.id " +
            "LEFT JOIN " + DatabaseHelper.TABLE_ADMIN + " a ON bt.approved_by = a.id " +
            "WHERE bt.user_id = ? AND bt.status IN ('borrowed', 'overdue') " +
            "ORDER BY bt.created_at DESC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        while (cursor.moveToNext()) {
            list.add(cursorToTicket(cursor));
        }
        cursor.close();
        return list;
    }

    // ─── GET BY STATUS ─────────────────────────────────────────────────────────

    public List<BorrowTicket> getByStatus(String status) {
        List<BorrowTicket> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT bt.*, u.full_name AS user_full_name, u.mssv AS user_mssv, " +
            "a.full_name AS approved_by_name " +
            "FROM " + DatabaseHelper.TABLE_BORROW_TICKETS + " bt " +
            "LEFT JOIN " + DatabaseHelper.TABLE_USERS + " u ON bt.user_id = u.id " +
            "LEFT JOIN " + DatabaseHelper.TABLE_ADMIN + " a ON bt.approved_by = a.id " +
            "WHERE bt.status = ? ORDER BY bt.created_at DESC";
        Cursor cursor = db.rawQuery(sql, new String[]{status});
        while (cursor.moveToNext()) {
            list.add(cursorToTicket(cursor));
        }
        cursor.close();
        return list;
    }

    // ─── APPROVE TICKET ────────────────────────────────────────────────────────

    public int approveTicket(int ticketId, int adminId, String note) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status",      "approved");
        cv.put("approved_by", adminId);
        cv.put("approved_at", com.haui.devicemanagement.util.DateUtils.getCurrentDateTime());
        if (note != null) cv.put("admin_note", note);
        return db.update(
            DatabaseHelper.TABLE_BORROW_TICKETS, cv,
            "id = ?", new String[]{String.valueOf(ticketId)}
        );
    }

    // ─── REJECT TICKET ─────────────────────────────────────────────────────────

    public int rejectTicket(int ticketId, int adminId, String note) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status",      "rejected");
        cv.put("approved_by", adminId);
        cv.put("approved_at", com.haui.devicemanagement.util.DateUtils.getCurrentDateTime());
        if (note != null) cv.put("admin_note", note);
        return db.update(
            DatabaseHelper.TABLE_BORROW_TICKETS, cv,
            "id = ?", new String[]{String.valueOf(ticketId)}
        );
    }

    // ─── UPDATE STATUS ─────────────────────────────────────────────────────────

    public int updateStatus(int ticketId, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        return db.update(
            DatabaseHelper.TABLE_BORROW_TICKETS, cv,
            "id = ?", new String[]{String.valueOf(ticketId)}
        );
    }

    // ─── GET OVERDUE TICKETS ───────────────────────────────────────────────────

    public List<BorrowTicket> getOverdueTickets(String currentDate) {
        List<BorrowTicket> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT bt.*, u.full_name AS user_full_name, u.mssv AS user_mssv, " +
            "a.full_name AS approved_by_name " +
            "FROM " + DatabaseHelper.TABLE_BORROW_TICKETS + " bt " +
            "LEFT JOIN " + DatabaseHelper.TABLE_USERS + " u ON bt.user_id = u.id " +
            "LEFT JOIN " + DatabaseHelper.TABLE_ADMIN + " a ON bt.approved_by = a.id " +
            "WHERE bt.status IN ('borrowed', 'overdue') " +
            "AND bt.expected_return_date < ? " +
            "ORDER BY bt.expected_return_date ASC";
        Cursor cursor = db.rawQuery(sql, new String[]{currentDate});
        while (cursor.moveToNext()) {
            list.add(cursorToTicket(cursor));
        }
        cursor.close();
        return list;
    }

    // ─── COUNT ─────────────────────────────────────────────────────────────────

    public int countByStatus(String status) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_BORROW_TICKETS +
            " WHERE status = ?",
            new String[]{status}
        );
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int countOverdueByUser(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String today = com.haui.devicemanagement.util.DateUtils.getCurrentDate();
        Cursor cursor = db.rawQuery(
            "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_BORROW_TICKETS +
            " WHERE user_id = ? AND status IN ('borrowed','overdue') AND expected_return_date < ?",
            new String[]{String.valueOf(userId), today}
        );
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // ─── HELPERS ───────────────────────────────────────────────────────────────

    private BorrowTicket cursorToTicket(Cursor cursor) {
        BorrowTicket t = new BorrowTicket();
        t.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        t.setTicketCode(cursor.getString(cursor.getColumnIndexOrThrow("ticket_code")));
        t.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        t.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        t.setBorrowReason(cursor.getString(cursor.getColumnIndexOrThrow("borrow_reason")));
        t.setExpectedReturnDate(cursor.getString(cursor.getColumnIndexOrThrow("expected_return_date")));
        t.setApprovedBy(cursor.getInt(cursor.getColumnIndexOrThrow("approved_by")));
        t.setApprovedAt(cursor.getString(cursor.getColumnIndexOrThrow("approved_at")));
        t.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        t.setAdminNote(cursor.getString(cursor.getColumnIndexOrThrow("admin_note")));
        int unIdx = cursor.getColumnIndex("user_full_name");
        if (unIdx >= 0) t.setUserFullName(cursor.getString(unIdx));
        int umIdx = cursor.getColumnIndex("user_mssv");
        if (umIdx >= 0) t.setUserMssv(cursor.getString(umIdx));
        int anIdx = cursor.getColumnIndex("approved_by_name");
        if (anIdx >= 0) t.setApprovedByName(cursor.getString(anIdx));
        return t;
    }

    private ContentValues toContentValues(BorrowTicket t) {
        ContentValues cv = new ContentValues();
        cv.put("ticket_code",          t.getTicketCode());
        cv.put("user_id",              t.getUserId());
        cv.put("status",               t.getStatus());
        cv.put("borrow_reason",        t.getBorrowReason());
        cv.put("expected_return_date", t.getExpectedReturnDate());
        cv.put("admin_note",           t.getAdminNote());
        return cv;
    }
}
