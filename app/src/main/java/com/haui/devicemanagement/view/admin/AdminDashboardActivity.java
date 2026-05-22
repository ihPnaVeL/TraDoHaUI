package com.haui.devicemanagement.view.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.dao.BorrowTicketDao;
import com.haui.devicemanagement.data.dao.DeviceDetailDao;
import com.haui.devicemanagement.data.dao.ReturnTicketDao;
import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.DateUtils;
import com.haui.devicemanagement.util.SessionManager;
import com.haui.devicemanagement.util.ThemeHelper;
import com.haui.devicemanagement.util.ThemeManager;
import com.haui.devicemanagement.view.auth.LoginActivity;
import com.haui.devicemanagement.view.common.NotificationActivity;

import androidx.appcompat.widget.AppCompatImageButton;

/**
 * AdminDashboardActivity — Trang tổng quan Admin.
 *
 * Hiển thị:
 * - Số phiếu mượn chờ duyệt.
 * - Số phiếu trả chờ xác nhận.
 * - Số thiết bị đang mượn.
 * - Số thiết bị hỏng/mất.
 * - Số phiếu quá hạn.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private TextView       tvAdminName;
    private TextView       tvPendingBorrow;
    private TextView       tvPendingReturn;
    private TextView       tvDeviceBorrowed;
    private TextView       tvDeviceDamaged;
    private TextView       tvOverdue;
    private MaterialButton btnBorrowApproval;
    private MaterialButton btnReturnApproval;
    private CardView       cardDeviceManage;
    private CardView       cardUserManage;
    private CardView       cardReport;
    private CardView       cardOverdueWarning;
    private AppCompatImageButton btnThemeToggle;

    private DrawerLayout   drawerLayout;
    private NavigationView navigationView;

    private SessionManager   session;
    private BorrowTicketDao  borrowTicketDao;
    private ReturnTicketDao  returnTicketDao;
    private DeviceDetailDao  deviceDetailDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.haui.devicemanagement.R.layout.activity_admin_dashboard);

        session         = new SessionManager(this);
        borrowTicketDao = new BorrowTicketDao(DatabaseHelper.getInstance(this));
        returnTicketDao = new ReturnTicketDao(DatabaseHelper.getInstance(this));
        deviceDetailDao = new DeviceDetailDao(DatabaseHelper.getInstance(this));

        initViews();
        setupClickListeners();
        
        ThemeHelper.applyDarkTheme(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboard();
        if (navigationView != null) {
            navigationView.setCheckedItem(com.haui.devicemanagement.R.id.nav_admin_dashboard);
        }
    }

    // ─── INIT ──────────────────────────────────────────────────────────────────

    private void initViews() {
        tvAdminName      = findViewById(com.haui.devicemanagement.R.id.tvAdminName);
        tvPendingBorrow  = findViewById(com.haui.devicemanagement.R.id.tvPendingBorrow);
        tvPendingReturn  = findViewById(com.haui.devicemanagement.R.id.tvPendingReturn);
        tvDeviceBorrowed = findViewById(com.haui.devicemanagement.R.id.tvDeviceBorrowed);
        tvDeviceDamaged  = findViewById(com.haui.devicemanagement.R.id.tvDeviceDamaged);
        tvOverdue        = findViewById(com.haui.devicemanagement.R.id.tvOverdue);
        btnBorrowApproval = findViewById(com.haui.devicemanagement.R.id.btnBorrowApproval);
        btnReturnApproval = findViewById(com.haui.devicemanagement.R.id.btnReturnApproval);
        cardDeviceManage  = findViewById(com.haui.devicemanagement.R.id.cardDeviceManage);
        cardUserManage    = findViewById(com.haui.devicemanagement.R.id.cardUserManage);
        cardReport        = findViewById(com.haui.devicemanagement.R.id.cardReport);
        cardOverdueWarning = findViewById(com.haui.devicemanagement.R.id.cardOverdueWarning);
        btnThemeToggle     = findViewById(com.haui.devicemanagement.R.id.btnThemeToggle);

        drawerLayout      = findViewById(com.haui.devicemanagement.R.id.drawerLayout);
        navigationView    = findViewById(com.haui.devicemanagement.R.id.navigationView);

        tvAdminName.setText(session.getFullName());
        updateThemeToggleIcon();

        // Setup Nav Header Admin Info
        if (navigationView != null && navigationView.getHeaderCount() > 0) {
            android.view.View headerView = navigationView.getHeaderView(0);
            TextView tvAdminHeaderName = headerView.findViewById(com.haui.devicemanagement.R.id.tvAdminHeaderName);
            TextView tvAdminHeaderPermission = headerView.findViewById(com.haui.devicemanagement.R.id.tvAdminHeaderPermission);
            if (tvAdminHeaderName != null) {
                tvAdminHeaderName.setText(session.getFullName());
            }
            if (tvAdminHeaderPermission != null) {
                tvAdminHeaderPermission.setText("Quyền: " + session.getPermissionLevel());
            }
        }
    }

    // ─── LOAD DASHBOARD DATA ───────────────────────────────────────────────────

    private void loadDashboard() {
        String today = DateUtils.getCurrentDate();

        int pendingBorrow  = borrowTicketDao.countByStatus(Constants.BORROW_PENDING);
        int pendingReturn  = returnTicketDao.countByStatus(Constants.RETURN_PENDING);
        int deviceBorrowed = deviceDetailDao.countByAvailabilityStatus(Constants.DEVICE_BORROWED);
        int deviceMaint    = deviceDetailDao.countByAvailabilityStatus(Constants.DEVICE_MAINTENANCE);
        int deviceLost     = deviceDetailDao.countByAvailabilityStatus(Constants.DEVICE_LOST);
        int overdue        = borrowTicketDao.getOverdueTickets(today).size();

        tvPendingBorrow.setText(String.valueOf(pendingBorrow));
        tvPendingReturn.setText(String.valueOf(pendingReturn));
        tvDeviceBorrowed.setText(String.valueOf(deviceBorrowed));
        tvDeviceDamaged.setText(String.valueOf(deviceMaint + deviceLost));
        tvOverdue.setText(String.valueOf(overdue));
    }

    // ─── CLICK LISTENERS ───────────────────────────────────────────────────────

    private void setupClickListeners() {
        btnBorrowApproval.setOnClickListener(v ->
            startActivity(new Intent(this, BorrowApprovalActivity.class))
        );

        btnReturnApproval.setOnClickListener(v ->
            startActivity(new Intent(this, ReturnApprovalActivity.class))
        );

        cardDeviceManage.setOnClickListener(v ->
            startActivity(new Intent(this, DeviceManageActivity.class))
        );

        cardUserManage.setOnClickListener(v ->
            startActivity(new Intent(this, UserManageActivity.class))
        );

        cardReport.setOnClickListener(v ->
            startActivity(new Intent(this, ReportActivity.class))
        );

        cardOverdueWarning.setOnClickListener(v ->
            startActivity(new Intent(this, OverdueActivity.class))
        );

        findViewById(com.haui.devicemanagement.R.id.btnNotification).setOnClickListener(v ->
            startActivity(new Intent(this, NotificationActivity.class))
        );

        btnThemeToggle.setOnClickListener(v -> {
            boolean isDark = ThemeManager.isDarkMode(this);
            ThemeManager.setDarkMode(this, !isDark);
            recreate();
        });

        findViewById(com.haui.devicemanagement.R.id.btnMenu).setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == com.haui.devicemanagement.R.id.nav_admin_dashboard) {
                    // Already here
                } else if (itemId == com.haui.devicemanagement.R.id.nav_devices) {
                    startActivity(new Intent(this, DeviceManageActivity.class));
                } else if (itemId == com.haui.devicemanagement.R.id.nav_students) {
                    startActivity(new Intent(this, UserManageActivity.class));
                } else if (itemId == com.haui.devicemanagement.R.id.nav_reports) {
                    startActivity(new Intent(this, ReportActivity.class));
                } else if (itemId == com.haui.devicemanagement.R.id.nav_logout) {
                    confirmLogout();
                }
                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            });
        }
    }

    // ─── LOGOUT ────────────────────────────────────────────────────────────────

    private void confirmLogout() {
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc muốn đăng xuất không?")
            .setPositiveButton("Đăng xuất", (d, w) -> {
                session.clearSession();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            })
            .setNegativeButton("Hủy", null)
            .show();
        ThemeHelper.applyDarkThemeToDialog(dialog);
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
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            confirmLogout();
        }
    }
}
