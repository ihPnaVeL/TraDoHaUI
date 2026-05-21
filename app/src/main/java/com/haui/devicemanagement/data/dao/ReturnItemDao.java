package com.haui.devicemanagement.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.ReturnItem;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng `return_items`.
 */
public class ReturnItemDao {

    private final DatabaseHelper dbHelper;

    public ReturnItemDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // ─── GET BY RETURN TICKET ID ───────────────────────────────────────────────

    public List<ReturnItem> getByReturnTicketId(int returnTicketId) {
        List<ReturnItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT ri.*, dd.asset_code, dd.serial_number, d.device_name " +
            "FROM " + DatabaseHelper.TABLE_RETURN_ITEMS + " ri " +
            "LEFT JOIN " + DatabaseHelper.TABLE_DEVICE_DETAIL + " dd ON ri.device_detail_id = dd.id " +
            "LEFT JOIN " + DatabaseHelper.TABLE_DEVICES + " d ON dd.device_id = d.id " +
            "WHERE ri.return_ticket_id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(returnTicketId)});
        while (cursor.moveToNext()) list.add(cursorToReturnItem(cursor));
        cursor.close();
        return list;
    }

    // ─── GET BY ID ─────────────────────────────────────────────────────────────

    public ReturnItem getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT ri.*, dd.asset_code, dd.serial_number, d.device_name " +
            "FROM " + DatabaseHelper.TABLE_RETURN_ITEMS + " ri " +
            "LEFT JOIN " + DatabaseHelper.TABLE_DEVICE_DETAIL + " dd ON ri.device_detail_id = dd.id " +
            "LEFT JOIN " + DatabaseHelper.TABLE_DEVICES + " d ON dd.device_id = d.id " +
            "WHERE ri.id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)});
        ReturnItem item = null;
        if (cursor.moveToFirst()) item = cursorToReturnItem(cursor);
        cursor.close();
        return item;
    }

    // ─── INSERT ────────────────────────────────────────────────────────────────

    public long insert(ReturnItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("return_ticket_id", item.getReturnTicketId());
        cv.put("borrow_item_id",   item.getBorrowItemId());
        cv.put("device_detail_id", item.getDeviceDetailId());
        cv.put("condition_in",     item.getConditionIn());
        cv.put("accessories_in",   item.getAccessoriesIn());
        cv.put("damage_note",      item.getDamageNote());
        cv.put("penalty_amount",   item.getPenaltyAmount());
        cv.put("is_completed",     item.getIsCompleted());
        return db.insert(DatabaseHelper.TABLE_RETURN_ITEMS, null, cv);
    }

    // ─── UPDATE CHECK RESULT ───────────────────────────────────────────────────

    /**
     * Admin cập nhật kết quả kiểm tra từng return_item.
     */
    public int updateCheckResult(int returnItemId, String conditionIn, String accessoriesIn,
                                  String damageNote, int penaltyAmount, int isCompleted) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("condition_in",   conditionIn);
        cv.put("accessories_in", accessoriesIn);
        cv.put("damage_note",    damageNote);
        cv.put("penalty_amount", penaltyAmount);
        cv.put("is_completed",   isCompleted);
        return db.update(
            DatabaseHelper.TABLE_RETURN_ITEMS, cv,
            "id = ?", new String[]{String.valueOf(returnItemId)}
        );
    }

    // ─── CHECK ALL COMPLETED ───────────────────────────────────────────────────

    /**
     * Kiểm tra tất cả return_items của một phiếu trả đã hoàn tất chưa.
     */
    public boolean allItemsCompleted(int returnTicketId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_RETURN_ITEMS +
            " WHERE return_ticket_id = ? AND is_completed = 0",
            new String[]{String.valueOf(returnTicketId)}
        );
        int remaining = 0;
        if (cursor.moveToFirst()) remaining = cursor.getInt(0);
        cursor.close();
        return remaining == 0;
    }

    /**
     * Kiểm tra có item nào bị hỏng/mất không.
     */
    public boolean hasDamagedOrLostItems(int returnTicketId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_RETURN_ITEMS +
            " WHERE return_ticket_id = ? AND condition_in IN ('damaged', 'lost')",
            new String[]{String.valueOf(returnTicketId)}
        );
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    public boolean hasLostItems(int returnTicketId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_RETURN_ITEMS +
            " WHERE return_ticket_id = ? AND condition_in = 'lost'",
            new String[]{String.valueOf(returnTicketId)}
        );
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    // ─── HELPERS ───────────────────────────────────────────────────────────────

    private ReturnItem cursorToReturnItem(Cursor cursor) {
        ReturnItem item = new ReturnItem();
        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        item.setReturnTicketId(cursor.getInt(cursor.getColumnIndexOrThrow("return_ticket_id")));
        item.setBorrowItemId(cursor.getInt(cursor.getColumnIndexOrThrow("borrow_item_id")));
        item.setDeviceDetailId(cursor.getInt(cursor.getColumnIndexOrThrow("device_detail_id")));
        item.setConditionIn(cursor.getString(cursor.getColumnIndexOrThrow("condition_in")));
        item.setAccessoriesIn(cursor.getString(cursor.getColumnIndexOrThrow("accessories_in")));
        item.setDamageNote(cursor.getString(cursor.getColumnIndexOrThrow("damage_note")));
        item.setPenaltyAmount(cursor.getInt(cursor.getColumnIndexOrThrow("penalty_amount")));
        item.setIsCompleted(cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")));
        int acIdx = cursor.getColumnIndex("asset_code");
        if (acIdx >= 0) item.setAssetCode(cursor.getString(acIdx));
        int snIdx = cursor.getColumnIndex("serial_number");
        if (snIdx >= 0) item.setSerialNumber(cursor.getString(snIdx));
        int dnIdx = cursor.getColumnIndex("device_name");
        if (dnIdx >= 0) item.setDeviceName(cursor.getString(dnIdx));
        return item;
    }
}
