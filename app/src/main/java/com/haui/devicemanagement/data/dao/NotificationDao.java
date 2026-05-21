package com.haui.devicemanagement.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng `notifications`.
 */
public class NotificationDao {

    private final DatabaseHelper dbHelper;

    public NotificationDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // ─── INSERT ────────────────────────────────────────────────────────────────

    public long insert(Notification notification) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("receiver_type", notification.getReceiverType());
        cv.put("receiver_id",   notification.getReceiverId());
        cv.put("type",          notification.getType());
        cv.put("title",         notification.getTitle());
        cv.put("message",       notification.getMessage());
        cv.put("ref_id",        notification.getRefId());
        cv.put("ref_type",      notification.getRefType());
        cv.put("is_read",       0);
        return db.insert(DatabaseHelper.TABLE_NOTIFICATIONS, null, cv);
    }

    // ─── GET BY RECEIVER ───────────────────────────────────────────────────────

    public List<Notification> getByReceiver(String receiverType, int receiverId) {
        List<Notification> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_NOTIFICATIONS, null,
            "receiver_type = ? AND receiver_id = ?",
            new String[]{receiverType, String.valueOf(receiverId)},
            null, null, "created_at DESC"
        );
        while (cursor.moveToNext()) list.add(cursorToNotification(cursor));
        cursor.close();
        return list;
    }

    // ─── GET UNREAD COUNT ──────────────────────────────────────────────────────

    public int getUnreadCount(String receiverType, int receiverId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_NOTIFICATIONS +
            " WHERE receiver_type = ? AND receiver_id = ? AND is_read = 0",
            new String[]{receiverType, String.valueOf(receiverId)}
        );
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // ─── MARK AS READ ──────────────────────────────────────────────────────────

    public int markAsRead(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_read", 1);
        return db.update(
            DatabaseHelper.TABLE_NOTIFICATIONS, cv,
            "id = ?", new String[]{String.valueOf(id)}
        );
    }

    // ─── MARK ALL AS READ ──────────────────────────────────────────────────────

    public int markAllAsRead(String receiverType, int receiverId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_read", 1);
        return db.update(
            DatabaseHelper.TABLE_NOTIFICATIONS, cv,
            "receiver_type = ? AND receiver_id = ?",
            new String[]{receiverType, String.valueOf(receiverId)}
        );
    }

    // ─── HELPERS ───────────────────────────────────────────────────────────────

    /**
     * Tiện ích: tạo notification nhanh.
     */
    public void sendNotification(String receiverType, int receiverId, String type,
                                  String title, String message, int refId, String refType) {
        Notification n = new Notification();
        n.setReceiverType(receiverType);
        n.setReceiverId(receiverId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setRefId(refId);
        n.setRefType(refType);
        insert(n);
    }

    private Notification cursorToNotification(Cursor cursor) {
        Notification n = new Notification();
        n.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        n.setReceiverType(cursor.getString(cursor.getColumnIndexOrThrow("receiver_type")));
        n.setReceiverId(cursor.getInt(cursor.getColumnIndexOrThrow("receiver_id")));
        n.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
        n.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        n.setMessage(cursor.getString(cursor.getColumnIndexOrThrow("message")));
        n.setRefId(cursor.getInt(cursor.getColumnIndexOrThrow("ref_id")));
        n.setRefType(cursor.getString(cursor.getColumnIndexOrThrow("ref_type")));
        n.setIsRead(cursor.getInt(cursor.getColumnIndexOrThrow("is_read")));
        n.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return n;
    }
}
