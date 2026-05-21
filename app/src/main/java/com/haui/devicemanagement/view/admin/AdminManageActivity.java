package com.haui.devicemanagement.view.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.dao.AdminDao;
import com.haui.devicemanagement.data.entity.Admin;
import com.haui.devicemanagement.util.DateUtils;
import com.haui.devicemanagement.util.SessionManager;
import com.haui.devicemanagement.util.ThemeHelper;
import com.haui.devicemanagement.view.adapter.UserAdapter;

import java.util.List;

public class AdminManageActivity extends AppCompatActivity implements UserAdapter.OnUserActionListener {

    private AdminDao adminDao;
    private UserAdapter adapter;
    private RecyclerView rvAdmins;
    private FloatingActionButton fabAdd;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);
        if (!session.isManager()) {
            Toast.makeText(this, "Chỉ Quản lý (Manager) mới có quyền truy cập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_manage);
        ThemeHelper.applyDarkTheme(this);

        adminDao = new AdminDao(DatabaseHelper.getInstance(this));

        initViews();
        setupRecyclerView();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAdmins();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý cán bộ");
        }

        rvAdmins = findViewById(R.id.rvAdmins);
        fabAdd = findViewById(R.id.fabAdd);
    }

    private void setupRecyclerView() {
        rvAdmins.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(this);
        rvAdmins.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> showAdminDialog(null));
    }

    private void loadAdmins() {
        List<Admin> admins = adminDao.getAll();
        adapter.setItems(admins);
    }

    private void showAdminDialog(Admin admin) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin, null);
        builder.setView(dialogView);

        EditText etAdminCode = dialogView.findViewById(R.id.etAdminCode);
        EditText etFullName = dialogView.findViewById(R.id.etFullName);
        EditText etPhone = dialogView.findViewById(R.id.etPhone);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        Spinner spPermissionLevel = dialogView.findViewById(R.id.spPermissionLevel);

        // Populate Spinner
        String[] roles = {"staff", "manager"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, com.haui.devicemanagement.R.layout.spinner_item, roles);
        spinnerAdapter.setDropDownViewResource(com.haui.devicemanagement.R.layout.spinner_dropdown_item);
        spPermissionLevel.setAdapter(spinnerAdapter);

        boolean isEdit = admin != null;
        if (isEdit) {
            builder.setTitle("Sửa thông tin cán bộ");
            etAdminCode.setText(admin.getAdminCode());
            etAdminCode.setEnabled(false); // Do not edit code
            etFullName.setText(admin.getFullName());
            etPhone.setText(admin.getPhone());
            etEmail.setText(admin.getEmail());
            int index = "manager".equals(admin.getPermissionLevel()) ? 1 : 0;
            spPermissionLevel.setSelection(index);
        } else {
            builder.setTitle("Thêm cán bộ");
        }

        builder.setPositiveButton(isEdit ? "Cập nhật" : "Lưu", null);
        builder.setNegativeButton("Hủy", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String code = etAdminCode.getText().toString().trim();
                String name = etFullName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String permLevel = spPermissionLevel.getSelectedItem().toString();

                if (code.isEmpty() || name.isEmpty() || email.isEmpty()) {
                    Toast.makeText(AdminManageActivity.this, "Mã, Họ tên và Email không được để trống", Toast.LENGTH_SHORT).show();
                    return;
                }

                Admin a = isEdit ? admin : new Admin();
                a.setAdminCode(code);
                a.setFullName(name);
                a.setPhone(phone);
                a.setEmail(email);
                a.setPermissionLevel(permLevel);

                long result;
                if (isEdit) {
                    result = adminDao.update(a);
                } else {
                    a.setIsActive(1);
                    a.setPasswordHash("123456"); // Mật khẩu mặc định
                    a.setCreatedAt(DateUtils.getCurrentDateTime());
                    result = adminDao.insert(a);
                }

                if (result > 0) {
                    Toast.makeText(AdminManageActivity.this, isEdit ? "Cập nhật thành công" : "Thêm cán bộ thành công", Toast.LENGTH_SHORT).show();
                    loadAdmins();
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(AdminManageActivity.this, isEdit ? "Cập nhật thất bại" : "Thêm thất bại (Mã cán bộ hoặc Email đã tồn tại?)", Toast.LENGTH_SHORT).show();
                }
            });
        });
        alertDialog.show();
        ThemeHelper.applyDarkThemeToDialog(alertDialog);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ─── ADAPTER CALLBACKS ─────────────────────────────────────────────────────

    @Override
    public void onToggleActive(Object userOrAdmin) {
        if (userOrAdmin instanceof Admin) {
            Admin admin = (Admin) userOrAdmin;
            if (admin.getId() == session.getAccountId()) {
                Toast.makeText(this, "Không thể tự khóa tài khoản của chính mình!", Toast.LENGTH_SHORT).show();
                return;
            }
            int newStatus = admin.isActive() ? 0 : 1;
            adminDao.setActive(admin.getId(), newStatus);
            Toast.makeText(this, newStatus == 1 ? "Đã mở khóa tài khoản" : "Đã khóa tài khoản", Toast.LENGTH_SHORT).show();
            loadAdmins();
        }
    }

    @Override
    public void onEdit(Object userOrAdmin) {
        if (userOrAdmin instanceof Admin) {
            showAdminDialog((Admin) userOrAdmin);
        }
    }
}