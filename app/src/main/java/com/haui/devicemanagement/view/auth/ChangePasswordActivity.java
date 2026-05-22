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

        if (session.isUser()) {
            applyDarkTheme();
        }
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

    private void applyDarkTheme() {
        if (!com.haui.devicemanagement.util.ThemeManager.isDarkMode(this)) return;
        View root = findViewById(com.haui.devicemanagement.R.id.rootLayout);
        if (root != null) {
            root.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
        }
        View toolbar = findViewById(com.haui.devicemanagement.R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(android.graphics.Color.parseColor("#161616"));
            if (toolbar instanceof Toolbar && ((Toolbar) toolbar).getNavigationIcon() != null) {
                androidx.core.graphics.drawable.DrawableCompat.setTint(
                    androidx.core.graphics.drawable.DrawableCompat.wrap(((Toolbar) toolbar).getNavigationIcon()), 
                    android.graphics.Color.WHITE
                );
            }
        }
        androidx.cardview.widget.CardView cardChangePassword = findViewById(com.haui.devicemanagement.R.id.cardChangePassword);
        if (cardChangePassword != null) {
            cardChangePassword.setCardBackgroundColor(android.graphics.Color.parseColor("#1C1C1E"));
        }

        TextView tvOldPasswordLabel = findViewById(com.haui.devicemanagement.R.id.tvOldPasswordLabel);
        TextView tvNewPasswordLabel = findViewById(com.haui.devicemanagement.R.id.tvNewPasswordLabel);
        TextView tvConfirmPasswordLabel = findViewById(com.haui.devicemanagement.R.id.tvConfirmPasswordLabel);
        if (tvOldPasswordLabel != null) tvOldPasswordLabel.setTextColor(android.graphics.Color.parseColor("#B0B0B0"));
        if (tvNewPasswordLabel != null) tvNewPasswordLabel.setTextColor(android.graphics.Color.parseColor("#B0B0B0"));
        if (tvConfirmPasswordLabel != null) tvConfirmPasswordLabel.setTextColor(android.graphics.Color.parseColor("#B0B0B0"));

        if (etOldPassword != null) {
            etOldPassword.setTextColor(android.graphics.Color.WHITE);
            etOldPassword.setHintTextColor(android.graphics.Color.parseColor("#8E8E8E"));
        }
        if (etNewPassword != null) {
            etNewPassword.setTextColor(android.graphics.Color.WHITE);
            etNewPassword.setHintTextColor(android.graphics.Color.parseColor("#8E8E8E"));
        }
        if (etConfirmPassword != null) {
            etConfirmPassword.setTextColor(android.graphics.Color.WHITE);
            etConfirmPassword.setHintTextColor(android.graphics.Color.parseColor("#8E8E8E"));
        }

        com.google.android.material.textfield.TextInputLayout tilOldPassword = findViewById(com.haui.devicemanagement.R.id.tilOldPassword);
        com.google.android.material.textfield.TextInputLayout tilNewPassword = findViewById(com.haui.devicemanagement.R.id.tilNewPassword);
        com.google.android.material.textfield.TextInputLayout tilConfirmPassword = findViewById(com.haui.devicemanagement.R.id.tilConfirmPassword);
        
        int boxColor = android.graphics.Color.parseColor("#444446");
        int hintColorVal = android.graphics.Color.parseColor("#8E8E8E");
        android.content.res.ColorStateList stateList = android.content.res.ColorStateList.valueOf(hintColorVal);

        if (tilOldPassword != null) {
            tilOldPassword.setBoxStrokeColor(boxColor);
            tilOldPassword.setBoxStrokeColorStateList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1962D1")));
            tilOldPassword.setDefaultHintTextColor(stateList);
            tilOldPassword.setHintTextColor(stateList);
        }
        if (tilNewPassword != null) {
            tilNewPassword.setBoxStrokeColor(boxColor);
            tilNewPassword.setBoxStrokeColorStateList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1962D1")));
            tilNewPassword.setDefaultHintTextColor(stateList);
            tilNewPassword.setHintTextColor(stateList);
        }
        if (tilConfirmPassword != null) {
            tilConfirmPassword.setBoxStrokeColor(boxColor);
            tilConfirmPassword.setBoxStrokeColorStateList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1962D1")));
            tilConfirmPassword.setDefaultHintTextColor(stateList);
            tilConfirmPassword.setHintTextColor(stateList);
        }
    }
}
