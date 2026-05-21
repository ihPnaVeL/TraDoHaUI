package com.haui.devicemanagement.view.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.haui.devicemanagement.view.auth.LoginActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.dao.AdminDao;
import com.haui.devicemanagement.data.dao.UserDao;
import com.haui.devicemanagement.data.entity.Admin;
import com.haui.devicemanagement.data.entity.User;
import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.SessionManager;
import com.haui.devicemanagement.view.auth.ChangePasswordActivity;

public class UserProfileActivity extends AppCompatActivity {

    private TextView tvCodeLabel;
    private TextView tvUserCode;
    private TextView tvFullName;
    private TextView tvRoleLabel;
    private TextView tvRoleOrClass;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private MaterialButton btnSave;
    private MaterialButton btnChangePassword;
    private MaterialButton btnLogout;
    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigation;

    private SessionManager session;
    private UserDao userDao;
    private AdminDao adminDao;

    private User currentUser;
    private Admin currentAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            finish();
            return;
        }

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userDao = new UserDao(dbHelper);
        adminDao = new AdminDao(dbHelper);

        initViews();
        setupListeners();
        loadProfileData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigation != null && session.isUser()) {
            bottomNavigation.setSelectedItemId(R.id.nav_profile);
        }
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Hồ sơ cá nhân");
        }

        tvCodeLabel = findViewById(R.id.tvCodeLabel);
        tvUserCode = findViewById(R.id.tvUserCode);
        tvFullName = findViewById(R.id.tvFullName);
        tvRoleLabel = findViewById(R.id.tvRoleLabel);
        tvRoleOrClass = findViewById(R.id.tvRoleOrClass);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnSave = findViewById(R.id.btnSave);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        if (session.isUser()) {
            setupBottomNavigation();
            applyDarkTheme();
        } else {
            if (bottomNavigation != null) {
                bottomNavigation.setVisibility(android.view.View.GONE);
            }
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                return true;
            }

            Intent intent = null;
            if (itemId == R.id.nav_home) {
                intent = new Intent(this, UserHomeActivity.class);
            } else if (itemId == R.id.nav_search) {
                intent = new Intent(this, DeviceSearchActivity.class);
            } else if (itemId == R.id.nav_history) {
                intent = new Intent(this, HistoryActivity.class);
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

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveProfileData());
        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });
        btnLogout.setOnClickListener(v -> confirmLogout());
    }

    private void loadProfileData() {
        if (session.isUser()) {
            currentUser = userDao.getById(session.getAccountId());
            if (currentUser != null) {
                tvCodeLabel.setText("Mã số sinh viên (MSSV):");
                tvUserCode.setText(currentUser.getMssv());
                tvFullName.setText(currentUser.getFullName());
                tvRoleLabel.setText("Khoa / Lớp:");
                tvRoleOrClass.setText(currentUser.getFaculty() + " — Lớp " + currentUser.getClassName());
                etEmail.setText(currentUser.getEmail());
                etPhone.setText(currentUser.getPhone());
            }
        } else {
            currentAdmin = adminDao.getById(session.getAccountId());
            if (currentAdmin != null) {
                tvCodeLabel.setText("Mã cán bộ:");
                tvUserCode.setText(currentAdmin.getAdminCode());
                tvFullName.setText(currentAdmin.getFullName());
                tvRoleLabel.setText("Cấp quyền:");
                tvRoleOrClass.setText(currentAdmin.getPermissionLevel().toUpperCase());
                etEmail.setText(currentAdmin.getEmail());
                etPhone.setText(currentAdmin.getPhone());
            }
        }
    }

    private void saveProfileData() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

        if (email.isEmpty()) {
            Toast.makeText(this, "Email không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        if (session.isUser()) {
            if (currentUser != null) {
                currentUser.setEmail(email);
                currentUser.setPhone(phone);
                int result = userDao.update(currentUser);
                if (result > 0) {
                    Toast.makeText(this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                    loadProfileData();
                } else {
                    Toast.makeText(this, "Cập nhật hồ sơ thất bại!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if (currentAdmin != null) {
                currentAdmin.setEmail(email);
                currentAdmin.setPhone(phone);
                int result = adminDao.update(currentAdmin);
                if (result > 0) {
                    Toast.makeText(this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                    loadProfileData();
                } else {
                    Toast.makeText(this, "Cập nhật hồ sơ thất bại!", Toast.LENGTH_SHORT).show();
                }
            }
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
        View root = findViewById(R.id.rootLayout);
        if (root != null) {
            root.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
        }
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(android.graphics.Color.parseColor("#161616"));
            if (toolbar instanceof Toolbar && ((Toolbar) toolbar).getNavigationIcon() != null) {
                androidx.core.graphics.drawable.DrawableCompat.setTint(
                    androidx.core.graphics.drawable.DrawableCompat.wrap(((Toolbar) toolbar).getNavigationIcon()), 
                    android.graphics.Color.WHITE
                );
            }
        }
        
        androidx.cardview.widget.CardView cardProfileInfo = findViewById(R.id.cardProfileInfo);
        androidx.cardview.widget.CardView cardContactSettings = findViewById(R.id.cardContactSettings);
        if (cardProfileInfo != null) {
            cardProfileInfo.setCardBackgroundColor(android.graphics.Color.parseColor("#1C1C1E"));
            applyDarkThemeToTextViews(cardProfileInfo);
        }
        if (cardContactSettings != null) {
            cardContactSettings.setCardBackgroundColor(android.graphics.Color.parseColor("#1C1C1E"));
            applyDarkThemeToTextViews(cardContactSettings);
        }
        
        if (etEmail != null) {
            etEmail.setTextColor(android.graphics.Color.WHITE);
            etEmail.setHintTextColor(android.graphics.Color.parseColor("#8E8E8E"));
        }
        if (etPhone != null) {
            etPhone.setTextColor(android.graphics.Color.WHITE);
            etPhone.setHintTextColor(android.graphics.Color.parseColor("#8E8E8E"));
        }

        com.google.android.material.textfield.TextInputLayout tilEmail = findViewById(R.id.tilEmail);
        com.google.android.material.textfield.TextInputLayout tilPhone = findViewById(R.id.tilPhone);
        int boxColor = android.graphics.Color.parseColor("#444446");
        int hintColorVal = android.graphics.Color.parseColor("#8E8E8E");
        android.content.res.ColorStateList stateList = android.content.res.ColorStateList.valueOf(hintColorVal);

        if (tilEmail != null) {
            tilEmail.setBoxStrokeColor(boxColor);
            tilEmail.setBoxStrokeColorStateList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1962D1")));
            tilEmail.setDefaultHintTextColor(stateList);
            tilEmail.setHintTextColor(stateList);
            tilEmail.setStartIconTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
        }
        if (tilPhone != null) {
            tilPhone.setBoxStrokeColor(boxColor);
            tilPhone.setBoxStrokeColorStateList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1962D1")));
            tilPhone.setDefaultHintTextColor(stateList);
            tilPhone.setHintTextColor(stateList);
            tilPhone.setStartIconTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
        }

        if (btnChangePassword != null) {
            btnChangePassword.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#3B82F6")));
            btnChangePassword.setTextColor(android.graphics.Color.parseColor("#3B82F6"));
        }
        if (btnLogout != null) {
            btnLogout.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EF4444")));
            btnLogout.setTextColor(android.graphics.Color.parseColor("#EF4444"));
        }
    }

    private void applyDarkThemeToTextViews(android.view.ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            android.view.View child = viewGroup.getChildAt(i);
            if (child instanceof android.widget.EditText) {
                continue;
            }
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                int id = tv.getId();
                if (id == R.id.tvUserCode || id == R.id.tvFullName || id == R.id.tvRoleOrClass) {
                    tv.setTextColor(android.graphics.Color.WHITE);
                } else {
                    tv.setTextColor(android.graphics.Color.parseColor("#B0B0B0"));
                }
            } else if (child instanceof android.view.ViewGroup) {
                applyDarkThemeToTextViews((android.view.ViewGroup) child);
            }
        }
    }
}