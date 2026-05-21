package com.haui.devicemanagement.view.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveProfileData());
        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });
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
}