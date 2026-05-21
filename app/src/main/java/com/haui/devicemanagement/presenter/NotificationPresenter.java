package com.haui.devicemanagement.presenter;

import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.dao.NotificationDao;
import com.haui.devicemanagement.data.entity.Notification;

import java.util.List;

/**
 * NotificationPresenter — quản lý thông báo.
 */
public class NotificationPresenter {

    public interface NotificationView {
        void onNotificationsLoaded(List<Notification> notifications, int unreadCount);
        void onError(String message);
    }

    private final NotificationDao notificationDao;

    public NotificationPresenter(DatabaseHelper dbHelper) {
        this.notificationDao = new NotificationDao(dbHelper);
    }

    public void loadNotifications(String receiverType, int receiverId, NotificationView view) {
        List<Notification> list = notificationDao.getByReceiver(receiverType, receiverId);
        int unread = notificationDao.getUnreadCount(receiverType, receiverId);
        view.onNotificationsLoaded(list, unread);
    }

    public void markAsRead(int id) {
        notificationDao.markAsRead(id);
    }

    public void markAllAsRead(String receiverType, int receiverId) {
        notificationDao.markAllAsRead(receiverType, receiverId);
    }

    public int getUnreadCount(String receiverType, int receiverId) {
        return notificationDao.getUnreadCount(receiverType, receiverId);
    }
}
