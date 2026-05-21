package com.haui.devicemanagement.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.Admin;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng `admin`.
 */
public class AdminDao {

    private final DatabaseHelper dbHelper;

    public AdminDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // ─── LOGIN ─────────────────────────────────────────────────────────────────

    /**
     * Đăng nhập admin bằng email hoặc admin_code + password.
     * Chỉ cho phép nếu is_active = 1.
     */
    public Admin login(String emailOrCode, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_ADMIN, null,
            "(email = ? OR admin_code = ?) AND password_hash = ? AND is_active = 1",
            new String[]{emailOrCode, emailOrCode, password},
            null, null, null
        );
        Admin admin = null;
        if (cursor.moveToFirst()) {
            admin = cursorToAdmin(cursor);
        }
        cursor.close();
        return admin;
    }

    // ─── GET BY ID ─────────────────────────────────────────────────────────────

    public Admin getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_ADMIN, null,
            "id = ?", new String[]{String.valueOf(id)},
            null, null, null
        );
        Admin admin = null;
        if (cursor.moveToFirst()) {
            admin = cursorToAdmin(cursor);
        }
        cursor.close();
        return admin;
    }

    // ─── GET ALL ───────────────────────────────────────────────────────────────

    public List<Admin> getAll() {
        List<Admin> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_ADMIN, null,
            null, null, null, null, "full_name ASC"
        );
        while (cursor.moveToNext()) {
            list.add(cursorToAdmin(cursor));
        }
        cursor.close();
        return list;
    }

    // ─── INSERT ────────────────────────────────────────────────────────────────

    public long insert(Admin admin) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(admin);
        return db.insert(DatabaseHelper.TABLE_ADMIN, null, cv);
    }

    // ─── UPDATE ────────────────────────────────────────────────────────────────

    public int update(Admin admin) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(admin);
        return db.update(
            DatabaseHelper.TABLE_ADMIN, cv,
            "id = ?", new String[]{String.valueOf(admin.getId())}
        );
    }

    // ─── SET ACTIVE ────────────────────────────────────────────────────────────

    public int setActive(int id, int isActive) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_active", isActive);
        return db.update(
            DatabaseHelper.TABLE_ADMIN, cv,
            "id = ?", new String[]{String.valueOf(id)}
        );
    }

    // ─── CHANGE PASSWORD ───────────────────────────────────────────────────────

    public int changePassword(int id, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password_hash", newPassword);
        return db.update(
            DatabaseHelper.TABLE_ADMIN, cv,
            "id = ?", new String[]{String.valueOf(id)}
        );
    }

    // ─── DELETE ────────────────────────────────────────────────────────────────

    public int delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
            DatabaseHelper.TABLE_ADMIN,
            "id = ?", new String[]{String.valueOf(id)}
        );
    }

    // ─── HELPERS ───────────────────────────────────────────────────────────────

    private Admin cursorToAdmin(Cursor cursor) {
        Admin admin = new Admin();
        admin.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        admin.setAdminCode(cursor.getString(cursor.getColumnIndexOrThrow("admin_code")));
        admin.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        admin.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        admin.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow("password_hash")));
        admin.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
        admin.setPermissionLevel(cursor.getString(cursor.getColumnIndexOrThrow("permission_level")));
        admin.setIsActive(cursor.getInt(cursor.getColumnIndexOrThrow("is_active")));
        admin.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return admin;
    }

    private ContentValues toContentValues(Admin admin) {
        ContentValues cv = new ContentValues();
        cv.put("admin_code",       admin.getAdminCode());
        cv.put("full_name",        admin.getFullName());
        cv.put("email",            admin.getEmail());
        cv.put("password_hash",    admin.getPasswordHash());
        cv.put("phone",            admin.getPhone());
        cv.put("permission_level", admin.getPermissionLevel());
        cv.put("is_active",        admin.getIsActive());
        return cv;
    }
}
