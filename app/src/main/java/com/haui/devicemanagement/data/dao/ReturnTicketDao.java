package com.haui.devicemanagement.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.ReturnItem;
import com.haui.devicemanagement.data.entity.ReturnTicket;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng `return_tickets` và phối hợp với `return_items`.
 */
public class ReturnTicketDao {

    private final DatabaseHelper dbHelper;

    public ReturnTicketDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // ─── CREATE RETURN TICKET (transaction) ────────────────────────────────────

    /**
     * Tạo phiếu trả + danh sách return_items trong cùng 1 transaction.
     * @return ID phiếu trả mới, -1 nếu thất bại.
     */
    public long createReturnTicket(ReturnTicket returnTicket, List<ReturnItem> returnItems) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = toContentValues(returnTicket);
            long returnTicketId = db.insert(DatabaseHelper.TABLE_RETURN_TICKETS, null, cv);
            if (returnTicketId == -1) return -1;

            for (ReturnItem item : returnItems) {
                item.setReturnTicketId((int) returnTicketId);
                ContentValues itemCv = new ContentValues();
                itemCv.put("return_ticket_id", item.getReturnTicketId());
                itemCv.put("borrow_item_id",   item.getBorrowItemId());
                itemCv.put("device_detail_id", item.getDeviceDetailId());
                itemCv.put("is_completed",     0);
                db.insert(DatabaseHelper.TABLE_RETURN_ITEMS, null, itemCv);
            }

            db.setTransactionSuccessful();
            return returnTicketId;
        } finally {
            db.endTransaction();
        }
    }

    // ─── GET BY ID ─────────────────────────────────────────────────────────────

    public ReturnTicket getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT rt.*, u.full_name AS user_full_name, u.mssv AS user_mssv, " +
            "a.full_name AS confirmed_by_name, bt.ticket_code AS borrow_ticket_code " +
            "FROM " + DatabaseHelper.TABLE_RETURN_TICKETS + " rt " +
            "LEFT JOIN " + DatabaseHelper.TABLE_USERS + " u ON rt.user_id = u.id " +
            "LEFT JOIN " + DatabaseHelper.TABLE_ADMIN + " a ON rt.confirmed_by = a.id " +
            "LEFT JOIN " + DatabaseHelper.TABLE_BORROW_TICKETS + " bt ON rt.borrow_ticket_id = bt.id " +
            "WHERE rt.id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)});
        ReturnTicket ticket = null;
        if (cursor.moveToFirst()) ticket = cursorToReturnTicket(cursor);
        cursor.close();
        return ticket;
    }

    // ─── GET BY USER ID ────────────────────────────────────────────────────────

    public List<ReturnTicket> getByUserId(int userId) {
        List<ReturnTicket> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT rt.*, u.full_name AS user_full_name, u.mssv AS user_mssv, " +
            "a.full_name AS confirmed_by_name, bt.ticket_code AS borrow_ticket_code " +
            "FROM " + DatabaseHelper.TABLE_RETURN_TICKETS + " rt " +
            "LEFT JOIN " + DatabaseHelper.TABLE_USERS + " u ON rt.user_id = u.id " +
            "LEFT JOIN " + DatabaseHelper.TABLE_ADMIN + " a ON rt.confirmed_by = a.id " +
            "LEFT JOIN " + DatabaseHelper.TABLE_BORROW_TICKETS + " bt ON rt.borrow_ticket_id = bt.id " +
            "WHERE rt.user_id = ? ORDER BY rt.returned_at DESC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        while (cursor.moveToNext()) list.add(cursorToReturnTicket(cursor));
        cursor.close();
        return list;
    }

    // ─── GET PENDING RETURNS ───────────────────────────────────────────────────

    public List<ReturnTicket> getPendingReturns() {
        return getByStatus("pending");
    }

    // ─── GET BY STATUS ─────────────────────────────────────────────────────────

    public List<ReturnTicket> getByStatus(String status) {
        List<ReturnTicket> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT rt.*, u.full_name AS user_full_name, u.mssv AS user_mssv, " +
            "a.full_name AS confirmed_by_name, bt.ticket_code AS borrow_ticket_code " +
            "FROM " + DatabaseHelper.TABLE_RETURN_TICKETS + " rt " +
            "LEFT JOIN " + DatabaseHelper.TABLE_USERS + " u ON rt.user_id = u.id " +
            "LEFT JOIN " + DatabaseHelper.TABLE_ADMIN + " a ON rt.confirmed_by = a.id " +
            "LEFT JOIN " + DatabaseHelper.TABLE_BORROW_TICKETS + " bt ON rt.borrow_ticket_id = bt.id " +
            "WHERE rt.status = ? ORDER BY rt.returned_at DESC";
        Cursor cursor = db.rawQuery(sql, new String[]{status});
        while (cursor.moveToNext()) list.add(cursorToReturnTicket(cursor));
        cursor.close();
        return list;
    }

    // ─── CONFIRM RETURN ────────────────────────────────────────────────────────

    public int confirmReturn(int returnTicketId, int adminId, String status, String note) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status",           status);
        cv.put("confirmed_by",     adminId);
        cv.put("confirmed_at",     com.haui.devicemanagement.util.DateUtils.getCurrentDateTime());
        if (note != null) cv.put("note", note);
        return db.update(
            DatabaseHelper.TABLE_RETURN_TICKETS, cv,
            "id = ?", new String[]{String.valueOf(returnTicketId)}
        );
    }

    // ─── UPDATE STATUS ─────────────────────────────────────────────────────────

    public int updateStatus(int returnTicketId, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        return db.update(
            DatabaseHelper.TABLE_RETURN_TICKETS, cv,
            "id = ?", new String[]{String.valueOf(returnTicketId)}
        );
    }

    // ─── COUNT ─────────────────────────────────────────────────────────────────

    public int countByStatus(String status) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_RETURN_TICKETS +
            " WHERE status = ?",
            new String[]{status}
        );
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // ─── HELPERS ───────────────────────────────────────────────────────────────

    private ReturnTicket cursorToReturnTicket(Cursor cursor) {
        ReturnTicket t = new ReturnTicket();
        t.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        t.setTicketCode(cursor.getString(cursor.getColumnIndexOrThrow("ticket_code")));
        t.setBorrowTicketId(cursor.getInt(cursor.getColumnIndexOrThrow("borrow_ticket_id")));
        t.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        t.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        t.setReturnedAt(cursor.getString(cursor.getColumnIndexOrThrow("returned_at")));
        t.setConfirmedBy(cursor.getInt(cursor.getColumnIndexOrThrow("confirmed_by")));
        t.setConfirmedAt(cursor.getString(cursor.getColumnIndexOrThrow("confirmed_at")));
        t.setOverallCondition(cursor.getString(cursor.getColumnIndexOrThrow("overall_condition")));
        t.setNote(cursor.getString(cursor.getColumnIndexOrThrow("note")));
        int unIdx = cursor.getColumnIndex("user_full_name");
        if (unIdx >= 0) t.setUserFullName(cursor.getString(unIdx));
        int umIdx = cursor.getColumnIndex("user_mssv");
        if (umIdx >= 0) t.setUserMssv(cursor.getString(umIdx));
        int cnIdx = cursor.getColumnIndex("confirmed_by_name");
        if (cnIdx >= 0) t.setConfirmedByName(cursor.getString(cnIdx));
        int btcIdx = cursor.getColumnIndex("borrow_ticket_code");
        if (btcIdx >= 0) t.setBorrowTicketCode(cursor.getString(btcIdx));
        return t;
    }

    private ContentValues toContentValues(ReturnTicket t) {
        ContentValues cv = new ContentValues();
        cv.put("ticket_code",      t.getTicketCode());
        cv.put("borrow_ticket_id", t.getBorrowTicketId());
        cv.put("user_id",          t.getUserId());
        cv.put("status",           t.getStatus());
        cv.put("note",             t.getNote());
        return cv;
    }
}
