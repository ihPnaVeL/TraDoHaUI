package com.haui.devicemanagement.view.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.dao.BorrowTicketDao;
import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.DateUtils;
import com.haui.devicemanagement.util.SessionManager;
import com.haui.devicemanagement.view.auth.LoginActivity;
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
    private CardView       cardMyBorrow;
    private CardView       cardMyReturn;
    private CardView       cardSearchDevice;
    private CardView       cardProfile;

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
    }

    // ─── INIT ──────────────────────────────────────────────────────────────────

    private void initViews() {
        tvUserName      = findViewById(com.haui.devicemanagement.R.id.tvUserName);
        tvPendingCount  = findViewById(com.haui.devicemanagement.R.id.tvPendingCount);
        tvBorrowedCount = findViewById(com.haui.devicemanagement.R.id.tvBorrowedCount);
        tvOverdueCount  = findViewById(com.haui.devicemanagement.R.id.tvOverdueCount);
        btnCreateBorrow = findViewById(com.haui.devicemanagement.R.id.btnCreateBorrow);
        btnCreateReturn = findViewById(com.haui.devicemanagement.R.id.btnCreateReturn);
        cardMyBorrow    = findViewById(com.haui.devicemanagement.R.id.cardMyBorrow);
        cardMyReturn    = findViewById(com.haui.devicemanagement.R.id.cardMyReturn);
        cardSearchDevice = findViewById(com.haui.devicemanagement.R.id.cardSearchDevice);
        cardProfile     = findViewById(com.haui.devicemanagement.R.id.cardProfile);

        tvUserName.setText(session.getFullName());
    }

    // ─── LOAD DATA ─────────────────────────────────────────────────────────────

    private void loadDashboard() {
        int userId = session.getAccountId();
        String today = DateUtils.getCurrentDate();

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

        // Lịch sử mượn
        cardMyBorrow.setOnClickListener(v ->
            startActivity(new Intent(this, MyBorrowActivity.class))
        );

        // Lịch sử trả
        cardMyReturn.setOnClickListener(v ->
            startActivity(new Intent(this, MyReturnActivity.class))
        );

        // Tìm thiết bị
        cardSearchDevice.setOnClickListener(v ->
            startActivity(new Intent(this, DeviceSearchActivity.class))
        );

        // Hồ sơ cá nhân
        cardProfile.setOnClickListener(v ->
            startActivity(new Intent(this, UserProfileActivity.class))
        );

        // Thông báo
        findViewById(com.haui.devicemanagement.R.id.btnNotification).setOnClickListener(v ->
            startActivity(new Intent(this, NotificationActivity.class))
        );

        // Đăng xuất
        findViewById(com.haui.devicemanagement.R.id.btnLogout).setOnClickListener(v ->
            confirmLogout()
        );
    }

    // ─── LOGOUT ────────────────────────────────────────────────────────────────

    private void confirmLogout() {
        new AlertDialog.Builder(this)
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
    }

    @Override
    public void onBackPressed() {
        // Không back về login
        confirmLogout();
    }
}
