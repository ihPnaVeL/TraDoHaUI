package com.haui.devicemanagement.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.Device;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng `devices` (loại thiết bị).
 */
public class DeviceDao {

    private final DatabaseHelper dbHelper;

    public DeviceDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // ─── GET ALL ───────────────────────────────────────────────────────────────

    public List<Device> getAll() {
        List<Device> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_DEVICES, null,
            null, null, null, null, "device_name ASC"
        );
        while (cursor.moveToNext()) {
            list.add(cursorToDevice(cursor));
        }
        cursor.close();
        return list;
    }

    // ─── GET BY ID ─────────────────────────────────────────────────────────────

    public Device getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_DEVICES, null,
            "id = ?", new String[]{String.valueOf(id)},
            null, null, null
        );
        Device device = null;
        if (cursor.moveToFirst()) {
            device = cursorToDevice(cursor);
        }
        cursor.close();
        return device;
    }

    // ─── SEARCH ────────────────────────────────────────────────────────────────

    public List<Device> search(String keyword) {
        List<Device> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String like = "%" + keyword + "%";
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_DEVICES, null,
            "device_name LIKE ? OR category LIKE ? OR brand LIKE ? OR device_code LIKE ?",
            new String[]{like, like, like, like},
            null, null, "device_name ASC"
        );
        while (cursor.moveToNext()) {
            list.add(cursorToDevice(cursor));
        }
        cursor.close();
        return list;
    }

    // ─── GET AVAILABLE COUNT ───────────────────────────────────────────────────

    /**
     * Đếm số thiết bị vật lý còn available cho một loại thiết bị.
     */
    public int getAvailableCountByDevice(int deviceId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_DEVICE_DETAIL +
            " WHERE device_id = ? AND availability_status = 'available'",
            new String[]{String.valueOf(deviceId)}
        );
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /**
     * Trả về danh sách devices kèm số lượng available.
     */
    public List<Device> getAllWithAvailableCount() {
        List<Device> list = getAll();
        for (Device d : list) {
            d.setAvailableCount(getAvailableCountByDevice(d.getId()));
        }
        return list;
    }

    /**
     * Lấy các loại thiết bị có ít nhất 1 thiết bị available.
     */
    public List<Device> getAvailableDeviceTypes() {
        List<Device> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
            "SELECT d.*, COUNT(dd.id) AS available_count " +
            "FROM " + DatabaseHelper.TABLE_DEVICES + " d " +
            "LEFT JOIN " + DatabaseHelper.TABLE_DEVICE_DETAIL + " dd " +
            "ON d.id = dd.device_id AND dd.availability_status = 'available' " +
            "GROUP BY d.id " +
            "HAVING available_count > 0 " +
            "ORDER BY d.device_name ASC";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            Device device = cursorToDevice(cursor);
            int countIdx = cursor.getColumnIndex("available_count");
            if (countIdx >= 0) device.setAvailableCount(cursor.getInt(countIdx));
            list.add(device);
        }
        cursor.close();
        return list;
    }

    // ─── INSERT ────────────────────────────────────────────────────────────────

    public long insert(Device device) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(device);
        return db.insert(DatabaseHelper.TABLE_DEVICES, null, cv);
    }

    // ─── UPDATE ────────────────────────────────────────────────────────────────

    public int update(Device device) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(device);
        return db.update(
            DatabaseHelper.TABLE_DEVICES, cv,
            "id = ?", new String[]{String.valueOf(device.getId())}
        );
    }

    // ─── DELETE ────────────────────────────────────────────────────────────────

    public int delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
            DatabaseHelper.TABLE_DEVICES,
            "id = ?", new String[]{String.valueOf(id)}
        );
    }

    // ─── HELPERS ───────────────────────────────────────────────────────────────

    private Device cursorToDevice(Cursor cursor) {
        Device device = new Device();
        device.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        device.setDeviceCode(cursor.getString(cursor.getColumnIndexOrThrow("device_code")));
        device.setDeviceName(cursor.getString(cursor.getColumnIndexOrThrow("device_name")));
        device.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
        device.setBrand(cursor.getString(cursor.getColumnIndexOrThrow("brand")));
        device.setModel(cursor.getString(cursor.getColumnIndexOrThrow("model")));
        device.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        device.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return device;
    }

    private ContentValues toContentValues(Device device) {
        ContentValues cv = new ContentValues();
        cv.put("device_code",  device.getDeviceCode());
        cv.put("device_name",  device.getDeviceName());
        cv.put("category",     device.getCategory());
        cv.put("brand",        device.getBrand());
        cv.put("model",        device.getModel());
        cv.put("description",  device.getDescription());
        return cv;
    }
}
