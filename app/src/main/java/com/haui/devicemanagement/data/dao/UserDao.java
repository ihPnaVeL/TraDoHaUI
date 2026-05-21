package com.haui.devicemanagement.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.User;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng `users`.
 */
public class UserDao {

    private final DatabaseHelper dbHelper;

    public UserDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // ─── LOGIN ─────────────────────────────────────────────────────────────────

    /**
     * Đăng nhập sinh viên bằng MSSV + password.
     * Chỉ cho phép nếu is_active = 1.
     */
    public User login(String mssv, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "mssv = ? AND password_hash = ? AND is_active = 1",
            new String[]{mssv, password},
            null, null, null
        );
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    // ─── GET BY ID ─────────────────────────────────────────────────────────────

    public User getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_USERS, null,
            "id = ?", new String[]{String.valueOf(id)},
            null, null, null
        );
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    // ─── GET ALL ───────────────────────────────────────────────────────────────

    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_USERS, null,
            null, null, null, null, "full_name ASC"
        );
        while (cursor.moveToNext()) {
            list.add(cursorToUser(cursor));
        }
        cursor.close();
        return list;
    }

    // ─── SEARCH ────────────────────────────────────────────────────────────────

    public List<User> search(String keyword) {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String like = "%" + keyword + "%";
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_USERS, null,
            "mssv LIKE ? OR full_name LIKE ? OR email LIKE ?",
            new String[]{like, like, like},
            null, null, "full_name ASC"
        );
        while (cursor.moveToNext()) {
            list.add(cursorToUser(cursor));
        }
        cursor.close();
        return list;
    }

    // ─── INSERT ────────────────────────────────────────────────────────────────

    public long insert(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(user);
        return db.insert(DatabaseHelper.TABLE_USERS, null, cv);
    }

    // ─── UPDATE ────────────────────────────────────────────────────────────────

    public int update(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(user);
        return db.update(
            DatabaseHelper.TABLE_USERS, cv,
            "id = ?", new String[]{String.valueOf(user.getId())}
        );
    }

    // ─── SET ACTIVE ────────────────────────────────────────────────────────────

    public int setActive(int id, int isActive) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_active", isActive);
        return db.update(
            DatabaseHelper.TABLE_USERS, cv,
            "id = ?", new String[]{String.valueOf(id)}
        );
    }

    // ─── CHANGE PASSWORD ───────────────────────────────────────────────────────

    public int changePassword(int id, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password_hash", newPassword);
        return db.update(
            DatabaseHelper.TABLE_USERS, cv,
            "id = ?", new String[]{String.valueOf(id)}
        );
    }

    // ─── COUNT ─────────────────────────────────────────────────────────────────

    public int getCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USERS, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // ─── HELPERS ───────────────────────────────────────────────────────────────

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        user.setMssv(cursor.getString(cursor.getColumnIndexOrThrow("mssv")));
        user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow("password_hash")));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        user.setClassName(cursor.getString(cursor.getColumnIndexOrThrow("class_name")));
        user.setFaculty(cursor.getString(cursor.getColumnIndexOrThrow("faculty")));
        user.setIsActive(cursor.getInt(cursor.getColumnIndexOrThrow("is_active")));
        user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return user;
    }

    private ContentValues toContentValues(User user) {
        ContentValues cv = new ContentValues();
        cv.put("mssv",          user.getMssv());
        cv.put("full_name",     user.getFullName());
        cv.put("password_hash", user.getPasswordHash());
        cv.put("phone",         user.getPhone());
        cv.put("email",         user.getEmail());
        cv.put("class_name",    user.getClassName());
        cv.put("faculty",       user.getFaculty());
        cv.put("is_active",     user.getIsActive());
        return cv;
    }
}
