package com.haui.devicemanagement.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.DeviceDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng `device_detail` (thiết bị vật lý).
 */
public class DeviceDetailDao {

    private final DatabaseHelper dbHelper;

    public DeviceDetailDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // ─── GET ALL ───────────────────────────────────────────────────────────────

    public List<DeviceDetail> getAll() {
        List<DeviceDetail> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT dd.*, d.device_name FROM " + DatabaseHelper.TABLE_DEVICE_DETAIL + " dd " +
            "LEFT JOIN " + DatabaseHelper.TABLE_DEVICES + " d ON dd.device_id = d.id " +
            "ORDER BY dd.asset_code ASC";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            list.add(cursorToDeviceDetail(cursor));
        }
        cursor.close();
        return list;
    }

    // ─── GET BY ID ─────────────────────────────────────────────────────────────

    public DeviceDetail getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT dd.*, d.device_name FROM " + DatabaseHelper.TABLE_DEVICE_DETAIL + " dd " +
            "LEFT JOIN " + DatabaseHelper.TABLE_DEVICES + " d ON dd.device_id = d.id " +
            "WHERE dd.id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)});
        DeviceDetail detail = null;
        if (cursor.moveToFirst()) {
            detail = cursorToDeviceDetail(cursor);
        }
        cursor.close();
        return detail;
    }

    // ─── GET BY DEVICE ID ──────────────────────────────────────────────────────

    public List<DeviceDetail> getByDeviceId(int deviceId) {
        List<DeviceDetail> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT dd.*, d.device_name FROM " + DatabaseHelper.TABLE_DEVICE_DETAIL + " dd " +
            "LEFT JOIN " + DatabaseHelper.TABLE_DEVICES + " d ON dd.device_id = d.id " +
            "WHERE dd.device_id = ? ORDER BY dd.asset_code ASC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(deviceId)});
        while (cursor.moveToNext()) {
            list.add(cursorToDeviceDetail(cursor));
        }
        cursor.close();
        return list;
    }

    // ─── GET AVAILABLE BY DEVICE ID ────────────────────────────────────────────

    public List<DeviceDetail> getAvailableByDeviceId(int deviceId) {
        List<DeviceDetail> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT dd.*, d.device_name FROM " + DatabaseHelper.TABLE_DEVICE_DETAIL + " dd " +
            "LEFT JOIN " + DatabaseHelper.TABLE_DEVICES + " d ON dd.device_id = d.id " +
            "WHERE dd.device_id = ? AND dd.availability_status = 'available' " +
            "ORDER BY dd.condition_status ASC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(deviceId)});
        while (cursor.moveToNext()) {
            list.add(cursorToDeviceDetail(cursor));
        }
        cursor.close();
        return list;
    }

    // ─── FILTER BY STATUS ──────────────────────────────────────────────────────

    public List<DeviceDetail> getByAvailabilityStatus(String status) {
        List<DeviceDetail> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT dd.*, d.device_name FROM " + DatabaseHelper.TABLE_DEVICE_DETAIL + " dd " +
            "LEFT JOIN " + DatabaseHelper.TABLE_DEVICES + " d ON dd.device_id = d.id " +
            "WHERE dd.availability_status = ? ORDER BY dd.asset_code ASC";
        Cursor cursor = db.rawQuery(sql, new String[]{status});
        while (cursor.moveToNext()) {
            list.add(cursorToDeviceDetail(cursor));
        }
        cursor.close();
        return list;
    }

    // ─── INSERT ────────────────────────────────────────────────────────────────

    public long insert(DeviceDetail detail) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(detail);
        return db.insert(DatabaseHelper.TABLE_DEVICE_DETAIL, null, cv);
    }

    // ─── UPDATE ────────────────────────────────────────────────────────────────

    public int update(DeviceDetail detail) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(detail);
        return db.update(
            DatabaseHelper.TABLE_DEVICE_DETAIL, cv,
            "id = ?", new String[]{String.valueOf(detail.getId())}
        );
    }

    // ─── UPDATE AVAILABILITY STATUS ────────────────────────────────────────────

    public int updateAvailabilityStatus(int id, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("availability_status", status);
        return db.update(
            DatabaseHelper.TABLE_DEVICE_DETAIL, cv,
            "id = ?", new String[]{String.valueOf(id)}
        );
    }

    // ─── UPDATE CONDITION STATUS ───────────────────────────────────────────────

    public int updateConditionStatus(int id, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("condition_status", status);
        return db.update(
            DatabaseHelper.TABLE_DEVICE_DETAIL, cv,
            "id = ?", new String[]{String.valueOf(id)}
        );
    }

    // ─── DELETE ────────────────────────────────────────────────────────────────

    public int delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
            DatabaseHelper.TABLE_DEVICE_DETAIL,
            "id = ?", new String[]{String.valueOf(id)}
        );
    }

    // ─── COUNT BORROWED ────────────────────────────────────────────────────────

    public int countByAvailabilityStatus(String status) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_DEVICE_DETAIL +
            " WHERE availability_status = ?",
            new String[]{status}
        );
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // ─── HELPERS ───────────────────────────────────────────────────────────────

    private DeviceDetail cursorToDeviceDetail(Cursor cursor) {
        DeviceDetail d = new DeviceDetail();
        d.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        d.setDeviceId(cursor.getInt(cursor.getColumnIndexOrThrow("device_id")));
        d.setAssetCode(cursor.getString(cursor.getColumnIndexOrThrow("asset_code")));
        d.setSerialNumber(cursor.getString(cursor.getColumnIndexOrThrow("serial_number")));
        d.setRoomLocation(cursor.getString(cursor.getColumnIndexOrThrow("room_location")));
        d.setConditionStatus(cursor.getString(cursor.getColumnIndexOrThrow("condition_status")));
        d.setAvailabilityStatus(cursor.getString(cursor.getColumnIndexOrThrow("availability_status")));
        d.setPurchaseDate(cursor.getString(cursor.getColumnIndexOrThrow("purchase_date")));
        d.setNote(cursor.getString(cursor.getColumnIndexOrThrow("note")));
        int nameIdx = cursor.getColumnIndex("device_name");
        if (nameIdx >= 0) d.setDeviceName(cursor.getString(nameIdx));
        return d;
    }

    private ContentValues toContentValues(DeviceDetail d) {
        ContentValues cv = new ContentValues();
        cv.put("device_id",           d.getDeviceId());
        cv.put("asset_code",          d.getAssetCode());
        cv.put("serial_number",       d.getSerialNumber());
        cv.put("room_location",       d.getRoomLocation());
        cv.put("condition_status",    d.getConditionStatus());
        cv.put("availability_status", d.getAvailabilityStatus());
        cv.put("purchase_date",       d.getPurchaseDate());
        cv.put("note",                d.getNote());
        return cv;
    }
}
