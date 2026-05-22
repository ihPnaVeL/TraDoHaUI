package com.haui.devicemanagement.view.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.button.MaterialButton;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.dao.BorrowTicketDao;
import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.DateUtils;
import com.haui.devicemanagement.util.SessionManager;
import com.haui.devicemanagement.util.ThemeManager;
import com.haui.devicemanagement.view.common.NotificationActivity;

/**
 * UserHomeActivity — Dashboard sinh viên.
 *
 * Hiển thị:
 * - Số phiếu đang chờ duyệt.
 * - Số phiếu đang mượn.
 * - Số phiếu quá hạn.
 * - Nút tạo phiếu mượn, tạo phiếu trả.
 * - Link đến lịch sử, tìm thiết bị, hồ sơ.
 */
public class UserHomeActivity extends AppCompatActivity {

    private TextView       tvUserName;
    private TextView       tvPendingCount;
    private TextView       tvBorrowedCount;
    private TextView       tvOverdueCount;
    private MaterialButton btnCreateBorrow;
    private MaterialButton btnCreateReturn;
    private AppCompatImageButton btnThemeToggle;
    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigation;

    private SessionManager  session;
    private BorrowTicketDao borrowTicketDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.haui.devicemanagement.R.layout.activity_user_home);

        session         = new SessionManager(this);
        borrowTicketDao = new BorrowTicketDao(DatabaseHelper.getInstance(this));

        initViews();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboard();
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(com.haui.devicemanagement.R.id.nav_home);
        }
    }

    // ─── INIT ──────────────────────────────────────────────────────────────────

    private void initViews() {
        tvUserName      = findViewById(com.haui.devicemanagement.R.id.tvUserName);
        tvPendingCount  = findViewById(com.haui.devicemanagement.R.id.tvPendingCount);
        tvBorrowedCount = findViewById(com.haui.devicemanagement.R.id.tvBorrowedCount);
        tvOverdueCount  = findViewById(com.haui.devicemanagement.R.id.tvOverdueCount);
        btnCreateBorrow = findViewById(com.haui.devicemanagement.R.id.btnCreateBorrow);
        btnCreateReturn = findViewById(com.haui.devicemanagement.R.id.btnCreateReturn);
        btnThemeToggle  = findViewById(com.haui.devicemanagement.R.id.btnThemeToggle);
        bottomNavigation = findViewById(com.haui.devicemanagement.R.id.bottomNavigation);

        tvUserName.setText(session.getFullName());
        updateThemeToggleIcon();
        setupBottomNavigation();
    }

    // ─── LOAD DATA ─────────────────────────────────────────────────────────────

    private void loadDashboard() {
        int userId = session.getAccountId();

        // Đếm phiếu pending của user này
        long pending  = borrowTicketDao.getByUserId(userId)
                        .stream().filter(t -> Constants.BORROW_PENDING.equals(t.getStatus())).count();
        long borrowed = borrowTicketDao.getByUserId(userId)
                        .stream().filter(t -> Constants.BORROW_BORROWED.equals(t.getStatus())).count();
        long overdue  = borrowTicketDao.countOverdueByUser(userId);

        tvPendingCount.setText(String.valueOf(pending));
        tvBorrowedCount.setText(String.valueOf(borrowed));
        tvOverdueCount.setText(String.valueOf(overdue));
    }

    // ─── CLICK LISTENERS ───────────────────────────────────────────────────────

    private void setupClickListeners() {
        // Tạo phiếu mượn
        btnCreateBorrow.setOnClickListener(v ->
            startActivity(new Intent(this, BorrowCreateActivity.class))
        );

        // Tạo phiếu trả
        btnCreateReturn.setOnClickListener(v ->
            startActivity(new Intent(this, ReturnCreateActivity.class))
        );

        // Thông báo
        findViewById(com.haui.devicemanagement.R.id.btnNotification).setOnClickListener(v ->
            startActivity(new Intent(this, NotificationActivity.class))
        );

        btnThemeToggle.setOnClickListener(v -> {
            boolean isDark = ThemeManager.isDarkMode(this);
            ThemeManager.setDarkMode(this, !isDark);
            recreate();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == com.haui.devicemanagement.R.id.nav_home) {
                return true;
            }

            Intent intent = null;
            if (itemId == com.haui.devicemanagement.R.id.nav_search) {
                intent = new Intent(this, DeviceSearchActivity.class);
            } else if (itemId == com.haui.devicemanagement.R.id.nav_history) {
                intent = new Intent(this, HistoryActivity.class);
            } else if (itemId == com.haui.devicemanagement.R.id.nav_profile) {
                intent = new Intent(this, UserProfileActivity.class);
            }

            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    // ─── LOGOUT ────────────────────────────────────────────────────────────────

    private void confirmLogout() {
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc muốn đăng xuất không?")
            .setPositiveButton("Đăng xuất", (d, w) -> {
                session.clearSession();
                Intent intent = new Intent(this, com.haui.devicemanagement.view.auth.LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            })
            .setNegativeButton("Hủy", null)
            .show();
        com.haui.devicemanagement.util.ThemeHelper.applyDarkThemeToDialog(dialog);
    }

    private void updateThemeToggleIcon() {
        if (ThemeManager.isDarkMode(this)) {
            btnThemeToggle.setImageResource(com.haui.devicemanagement.R.drawable.ic_sun);
        } else {
            btnThemeToggle.setImageResource(com.haui.devicemanagement.R.drawable.ic_moon);
        }
    }

    @Override
    public void onBackPressed() {
        // Không back về login
        confirmLogout();
    }
}
