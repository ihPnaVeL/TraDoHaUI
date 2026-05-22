package com.haui.devicemanagement.view.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.dao.UserDao;
import com.haui.devicemanagement.data.entity.User;
import com.haui.devicemanagement.util.DateUtils;
import com.haui.devicemanagement.view.adapter.UserAdapter;

import java.util.List;

public class UserManageActivity extends AppCompatActivity implements UserAdapter.OnUserActionListener {

    private UserDao userDao;
    private UserAdapter adapter;
    private RecyclerView rvUsers;
    private EditText etSearch;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage);

        userDao = new UserDao(DatabaseHelper.getInstance(this));

        initViews();
        setupRecyclerView();
        setupListeners();

        com.haui.devicemanagement.util.ThemeHelper.applyDarkTheme(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý sinh viên");
        }

        rvUsers = findViewById(R.id.rvUsers);
        etSearch = findViewById(R.id.etSearch);
        fabAdd = findViewById(R.id.fabAdd);
    }

    private void setupRecyclerView() {
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(this);
        rvUsers.setAdapter(adapter);
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fabAdd.setOnClickListener(v -> showUserDialog(null));
    }

    private void loadUsers() {
        List<User> users = userDao.getAll();
        adapter.setItems(users);
    }

    private void searchUsers(String keyword) {
        if (keyword.isEmpty()) {
            loadUsers();
        } else {
            List<User> filtered = userDao.search(keyword);
            adapter.setItems(filtered);
        }
    }

    private void showUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user, null);
        builder.setView(dialogView);

        EditText etMssv = dialogView.findViewById(R.id.etMssv);
        EditText etFullName = dialogView.findViewById(R.id.etFullName);
        EditText etClassName = dialogView.findViewById(R.id.etClassName);
        EditText etFaculty = dialogView.findViewById(R.id.etFaculty);
        EditText etPhone = dialogView.findViewById(R.id.etPhone);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);

        boolean isEdit = user != null;
        if (isEdit) {
            builder.setTitle("Sửa thông tin sinh viên");
            etMssv.setText(user.getMssv());
            etMssv.setEnabled(false); // Do not edit MSSV
            etFullName.setText(user.getFullName());
            etClassName.setText(user.getClassName());
            etFaculty.setText(user.getFaculty());
            etPhone.setText(user.getPhone());
            etEmail.setText(user.getEmail());
        } else {
            builder.setTitle("Thêm sinh viên");
        }

        builder.setPositiveButton(isEdit ? "Cập nhật" : "Lưu", null);
        builder.setNegativeButton("Hủy", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        com.haui.devicemanagement.util.ThemeHelper.applyDarkThemeToDialog(alertDialog);

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String mssv = etMssv.getText().toString().trim();
                String name = etFullName.getText().toString().trim();
                String className = etClassName.getText().toString().trim();
                String faculty = etFaculty.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String email = etEmail.getText().toString().trim();

                if (mssv.isEmpty() || name.isEmpty() || className.isEmpty() || faculty.isEmpty()) {
                    Toast.makeText(UserManageActivity.this, "Vui lòng điền đầy đủ các trường bắt buộc", Toast.LENGTH_SHORT).show();
                    return;
                }

                User u = isEdit ? user : new User();
                u.setMssv(mssv);
                u.setFullName(name);
                u.setClassName(className);
                u.setFaculty(faculty);
                u.setPhone(phone);
                u.setEmail(email);

                long result;
                if (isEdit) {
                    result = userDao.update(u);
                } else {
                    u.setIsActive(1);
                    u.setPasswordHash("123456"); // Default password
                    u.setCreatedAt(DateUtils.getCurrentDateTime());
                    result = userDao.insert(u);
                }

                if (result > 0) {
                    Toast.makeText(UserManageActivity.this, isEdit ? "Cập nhật thành công" : "Thêm sinh viên thành công", Toast.LENGTH_SHORT).show();
                    loadUsers();
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(UserManageActivity.this, isEdit ? "Cập nhật thất bại" : "Thêm thất bại (MSSV đã tồn tại?)", Toast.LENGTH_SHORT).show();
                }
            });
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
        if (userOrAdmin instanceof User) {
            User user = (User) userOrAdmin;
            int newStatus = user.isActive() ? 0 : 1;
            userDao.setActive(user.getId(), newStatus);
            Toast.makeText(this, newStatus == 1 ? "Đã mở khóa tài khoản" : "Đã khóa tài khoản", Toast.LENGTH_SHORT).show();
            loadUsers();
        }
    }

    @Override
    public void onEdit(Object userOrAdmin) {
        if (userOrAdmin instanceof User) {
            showUserDialog((User) userOrAdmin);
        }
    }
}