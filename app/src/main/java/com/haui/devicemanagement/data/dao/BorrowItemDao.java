package com.haui.devicemanagement.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.BorrowItem;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng `borrow_items`.
 */
public class BorrowItemDao {

    private final DatabaseHelper dbHelper;

    public BorrowItemDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // ─── GET BY TICKET ID ──────────────────────────────────────────────────────

    public List<BorrowItem> getByTicketId(int ticketId) {
        List<BorrowItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT bi.*, dd.asset_code, dd.serial_number, d.device_name " +
            "FROM " + DatabaseHelper.TABLE_BORROW_ITEMS + " bi " +
            "LEFT JOIN " + DatabaseHelper.TABLE_DEVICE_DETAIL + " dd ON bi.device_detail_id = dd.id " +
            "LEFT JOIN " + DatabaseHelper.TABLE_DEVICES + " d ON dd.device_id = d.id " +
            "WHERE bi.ticket_id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(ticketId)});
        while (cursor.moveToNext()) {
            list.add(cursorToBorrowItem(cursor));
        }
        cursor.close();
        return list;
    }

    // ─── GET BY ID ─────────────────────────────────────────────────────────────

    public BorrowItem getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT bi.*, dd.asset_code, dd.serial_number, d.device_name " +
            "FROM " + DatabaseHelper.TABLE_BORROW_ITEMS + " bi " +
            "LEFT JOIN " + DatabaseHelper.TABLE_DEVICE_DETAIL + " dd ON bi.device_detail_id = dd.id " +
            "LEFT JOIN " + DatabaseHelper.TABLE_DEVICES + " d ON dd.device_id = d.id " +
            "WHERE bi.id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)});
        BorrowItem item = null;
        if (cursor.moveToFirst()) item = cursorToBorrowItem(cursor);
        cursor.close();
        return item;
    }

    // ─── INSERT ────────────────────────────────────────────────────────────────

    public long insert(BorrowItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ticket_id", item.getTicketId());
        if (item.getDeviceDetailId() > 0) {
            cv.put("device_detail_id", item.getDeviceDetailId());
        }
        cv.put("condition_out",   item.getConditionOut());
        cv.put("accessories_out", item.getAccessoriesOut());
        cv.put("note",            item.getNote());
        return db.insert(DatabaseHelper.TABLE_BORROW_ITEMS, null, cv);
    }

    // ─── ASSIGN DEVICE DETAIL ──────────────────────────────────────────────────

    /**
     * Admin gán thiết bị cụ thể vào borrow_item.
     * Cập nhật device_detail_id, condition_out, accessories_out.
     */
    public int assignDeviceDetail(int borrowItemId, int deviceDetailId,
                                   String conditionOut, String accessoriesOut) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("device_detail_id", deviceDetailId);
        cv.put("condition_out",    conditionOut);
        cv.put("accessories_out",  accessoriesOut);
        return db.update(
            DatabaseHelper.TABLE_BORROW_ITEMS, cv,
            "id = ?", new String[]{String.valueOf(borrowItemId)}
        );
    }

    // ─── HELPERS ───────────────────────────────────────────────────────────────

    private BorrowItem cursorToBorrowItem(Cursor cursor) {
        BorrowItem item = new BorrowItem();
        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        item.setTicketId(cursor.getInt(cursor.getColumnIndexOrThrow("ticket_id")));
        item.setDeviceDetailId(cursor.getInt(cursor.getColumnIndexOrThrow("device_detail_id")));
        item.setConditionOut(cursor.getString(cursor.getColumnIndexOrThrow("condition_out")));
        item.setAccessoriesOut(cursor.getString(cursor.getColumnIndexOrThrow("accessories_out")));
        item.setNote(cursor.getString(cursor.getColumnIndexOrThrow("note")));
        int acIdx = cursor.getColumnIndex("asset_code");
        if (acIdx >= 0) item.setAssetCode(cursor.getString(acIdx));
        int snIdx = cursor.getColumnIndex("serial_number");
        if (snIdx >= 0) item.setSerialNumber(cursor.getString(snIdx));
        int dnIdx = cursor.getColumnIndex("device_name");
        if (dnIdx >= 0) item.setDeviceName(cursor.getString(dnIdx));
        return item;
    }
}
