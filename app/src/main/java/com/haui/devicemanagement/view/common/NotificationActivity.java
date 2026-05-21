package com.haui.devicemanagement.view.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.Notification;
import com.haui.devicemanagement.presenter.NotificationPresenter;
import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.SessionManager;
import com.haui.devicemanagement.view.adapter.NotificationAdapter;
import com.haui.devicemanagement.view.user.TicketDetailActivity;

import java.util.List;

public class NotificationActivity extends AppCompatActivity implements NotificationPresenter.NotificationView, NotificationAdapter.OnNotificationClickListener {

    private TextView tvUnreadStatus;
    private MaterialButton btnMarkAllRead;
    private RecyclerView rvNotifications;

    private SessionManager session;
    private NotificationPresenter presenter;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupListeners();

        if (session.isUser()) {
            applyDarkTheme();
        }

        presenter = new NotificationPresenter(DatabaseHelper.getInstance(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thông báo");
        }

        tvUnreadStatus = findViewById(R.id.tvUnreadStatus);
        btnMarkAllRead = findViewById(R.id.btnMarkAllRead);
        rvNotifications = findViewById(R.id.rvNotifications);
    }

    private void setupRecyclerView() {
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this);
        rvNotifications.setAdapter(adapter);
    }

    private void setupListeners() {
        btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
    }

    private void loadNotifications() {
        presenter.loadNotifications(session.getAccountType(), session.getAccountId(), this);
    }

    private void markAllAsRead() {
        presenter.markAllAsRead(session.getAccountType(), session.getAccountId());
        Toast.makeText(this, "Đã đánh dấu đọc tất cả thông báo", Toast.LENGTH_SHORT).show();
        loadNotifications();
    }

    @Override
    public void onNotificationsLoaded(List<Notification> notifications, int unreadCount) {
        adapter.setNotifications(notifications);
        tvUnreadStatus.setText("Có " + unreadCount + " thông báo chưa đọc");
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, "Lỗi khi tải thông báo: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNotificationClick(Notification notification) {
        if (!notification.isRead()) {
            presenter.markAsRead(notification.getId());
        }

        // Navigate based on reference type
        String refType = notification.getRefType();
        int refId = notification.getRefId();

        if (refId > 0 && (Constants.REF_BORROW.equals(refType) || "borrow".equals(refType))) {
            Intent intent = new Intent(this, TicketDetailActivity.class);
            intent.putExtra("TICKET_TYPE", "borrow");
            intent.putExtra("TICKET_ID", refId);
            startActivity(intent);
        } else if (refId > 0 && (Constants.REF_RETURN.equals(refType) || "return".equals(refType))) {
            Intent intent = new Intent(this, TicketDetailActivity.class);
            intent.putExtra("TICKET_TYPE", "return");
            intent.putExtra("TICKET_ID", refId);
            startActivity(intent);
        } else {
            // General notification: just refresh to show it's read
            loadNotifications();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void applyDarkTheme() {
        android.view.View root = findViewById(R.id.rootLayout);
        if (root != null) {
            root.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(android.graphics.Color.parseColor("#161616"));
            if (toolbar.getNavigationIcon() != null) {
                androidx.core.graphics.drawable.DrawableCompat.setTint(
                    androidx.core.graphics.drawable.DrawableCompat.wrap(toolbar.getNavigationIcon()), 
                    android.graphics.Color.WHITE
                );
            }
        }
        if (tvUnreadStatus != null) {
            tvUnreadStatus.setTextColor(android.graphics.Color.parseColor("#B0B0B0"));
        }
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setTextColor(android.graphics.Color.parseColor("#1962D1"));
        }
        android.view.View divider = findViewById(R.id.dividerLine);
        if (divider != null) {
            divider.setBackgroundColor(android.graphics.Color.parseColor("#2C2C2E"));
        }
    }
}