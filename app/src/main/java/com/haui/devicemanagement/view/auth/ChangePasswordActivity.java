package com.haui.devicemanagement.view.auth;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.presenter.AuthPresenter;
import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.SessionManager;

/**
 * ChangePasswordActivity — Màn hình đổi mật khẩu.
 * Hoạt động cho cả user và admin dựa trên session account_type.
 */
public class ChangePasswordActivity extends AppCompatActivity
        implements AuthPresenter.ChangePasswordView {

    private TextInputEditText etOldPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton    btnChangePassword;
    private TextView          tvError;

    private AuthPresenter presenter;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.haui.devicemanagement.R.layout.activity_change_password);

        session   = new SessionManager(this);
        presenter = new AuthPresenter(DatabaseHelper.getInstance(this));

        setupToolbar();
        initViews();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(com.haui.devicemanagement.R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Đổi mật khẩu");
        }
    }

    private void initViews() {
        etOldPassword     = findViewById(com.haui.devicemanagement.R.id.etOldPassword);
        etNewPassword     = findViewById(com.haui.devicemanagement.R.id.etNewPassword);
        etConfirmPassword = findViewById(com.haui.devicemanagement.R.id.etConfirmPassword);
        btnChangePassword = findViewById(com.haui.devicemanagement.R.id.btnChangePassword);
        tvError           = findViewById(com.haui.devicemanagement.R.id.tvError);

        btnChangePassword.setOnClickListener(v -> performChangePassword());
    }

    private void performChangePassword() {
        String oldPw  = getText(etOldPassword);
        String newPw  = getText(etNewPassword);
        String confPw = getText(etConfirmPassword);
        int accountId = session.getAccountId();

        clearError();

        if (Constants.ACCOUNT_ADMIN.equals(session.getAccountType())) {
            presenter.changePasswordAdmin(accountId, oldPw, newPw, confPw, this);
        } else {
            presenter.changePasswordUser(accountId, oldPw, newPw, confPw, this);
        }
    }

    // ─── ChangePasswordView callbacks ─────────────────────────────────────────

    @Override
    public void onChangePasswordSuccess() {
        Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onChangePasswordFailed(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString() : "";
    }

    private void clearError() {
        tvError.setText("");
        tvError.setVisibility(View.GONE);
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
