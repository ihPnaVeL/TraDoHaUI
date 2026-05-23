package com.haui.devicemanagement.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.presenter.AuthPresenter;
import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.SessionManager;
import com.haui.devicemanagement.view.admin.AdminDashboardActivity;
import com.haui.devicemanagement.view.user.UserHomeActivity;

/**
 * LoginActivity — Màn hình đăng nhập.
 *
 * - TabLayout chọn Sinh viên / Quản trị.
 * - Sinh viên: nhập MSSV + password.
 * - Admin: nhập email/mã cán bộ + password.
 * - Sau đăng nhập: lưu session → điều hướng đến màn hình tương ứng.
 */
public class LoginActivity extends AppCompatActivity implements AuthPresenter.LoginView {

    // ─── Views ─────────────────────────────────────────────────────────────────
    private TabLayout      tabLayout;
    private TextView       tvInputLabel;
    private TextInputLayout tilUsername;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private TextView       tvError;
    private ProgressBar    progressBar;

    // ─── Logic ─────────────────────────────────────────────────────────────────
    private AuthPresenter presenter;
    private SessionManager session;
    private boolean isAdminMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.haui.devicemanagement.R.layout.activity_login);

        // Khởi tạo presenter và session
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        presenter = new AuthPresenter(dbHelper);
        session   = new SessionManager(this);

        // Nếu đã đăng nhập rồi → bỏ qua login
        if (session.isLoggedIn()) {
            navigateAfterLogin(session.getAccountType());
            return;
        }

        initViews();
        setupTabListener();
        setupLoginButton();
        applyDynamicTheme(com.haui.devicemanagement.util.ThemeManager.isDarkMode(this));
    }

    // ─── INIT ──────────────────────────────────────────────────────────────────

    private void initViews() {
        tabLayout    = findViewById(com.haui.devicemanagement.R.id.tabLayout);
        tvInputLabel = findViewById(com.haui.devicemanagement.R.id.tvInputLabel);
        tilUsername  = findViewById(com.haui.devicemanagement.R.id.tilUsername);
        etUsername   = findViewById(com.haui.devicemanagement.R.id.etUsername);
        etPassword   = findViewById(com.haui.devicemanagement.R.id.etPassword);
        btnLogin     = findViewById(com.haui.devicemanagement.R.id.btnLogin);
        tvError      = findViewById(com.haui.devicemanagement.R.id.tvError);
        progressBar  = findViewById(com.haui.devicemanagement.R.id.progressBar);
    }

    // ─── TAB LISTENER ──────────────────────────────────────────────────────────

    private void setupTabListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isAdminMode = (tab.getPosition() == 1);
                updateFormForMode();
                clearError();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void updateFormForMode() {
        if (isAdminMode) {
            tvInputLabel.setText("Email hoặc Mã cán bộ");
            etUsername.setHint("Nhập email hoặc mã cán bộ");
            etUsername.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    | android.text.InputType.TYPE_CLASS_TEXT);
        } else {
            tvInputLabel.setText("Mã số sinh viên (MSSV)");
            etUsername.setHint("Nhập MSSV");
            etUsername.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        }
        etUsername.setText("");
        etPassword.setText("");
    }

    // ─── LOGIN BUTTON ──────────────────────────────────────────────────────────

    private void setupLoginButton() {
        btnLogin.setOnClickListener(v -> performLogin());

        // Cho phép nhấn Enter trên bàn phím để đăng nhập
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performLogin();
                return true;
            }
            return false;
        });
    }

    private void performLogin() {
        clearError();
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        if (isAdminMode) {
            presenter.loginAdmin(username, password, this);
        } else {
            presenter.loginUser(username, password, this);
        }
    }

    // ─── LoginView callbacks ───────────────────────────────────────────────────

    @Override
    public void onLoginSuccess(String accountType, int accountId, String fullName, String permission) {
        // Lưu session
        session.saveSession(accountType, accountId, fullName, permission);

        // Điều hướng
        navigateAfterLogin(accountType);
    }

    @Override
    public void onLoginFailed(String message) {
        showError(message);
    }

    @Override
    public void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
    }

    // ─── NAVIGATION ────────────────────────────────────────────────────────────

    private void navigateAfterLogin(String accountType) {
        Intent intent;
        if (Constants.ACCOUNT_ADMIN.equals(accountType)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, UserHomeActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ─── UI HELPERS ────────────────────────────────────────────────────────────

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void clearError() {
        tvError.setText("");
        tvError.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        // Không cho phép back từ màn hình login
        finishAffinity();
    }

    private void applyDynamicTheme(boolean isDark) {
        View root = findViewById(R.id.rootScrollView);
        View cardForm = findViewById(R.id.cardLoginForm);
        TextView tvPasswordLabel = findViewById(R.id.tvPasswordLabel);
        TextView tvFooter = findViewById(R.id.tvFooter);
        TextInputLayout tilUsername = findViewById(R.id.tilUsername);
        TextInputLayout tilPassword = findViewById(R.id.tilPassword);

        if (isDark) {
            if (root != null) {
                root.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
            }
            if (tabLayout != null) {
                tabLayout.setTabTextColors(android.graphics.Color.parseColor("#8E8E8E"), android.graphics.Color.WHITE);
            }
            if (cardForm != null && cardForm instanceof androidx.cardview.widget.CardView) {
                ((androidx.cardview.widget.CardView) cardForm).setCardBackgroundColor(android.graphics.Color.parseColor("#1C1C1E"));
            }
            if (tvInputLabel != null) {
                tvInputLabel.setTextColor(android.graphics.Color.parseColor("#B0B0B0"));
            }
            if (tvPasswordLabel != null) {
                tvPasswordLabel.setTextColor(android.graphics.Color.parseColor("#B0B0B0"));
            }
            if (etUsername != null) {
                etUsername.setTextColor(android.graphics.Color.WHITE);
                etUsername.setHintTextColor(android.graphics.Color.parseColor("#8E8E8E"));
            }
            if (etPassword != null) {
                etPassword.setTextColor(android.graphics.Color.WHITE);
                etPassword.setHintTextColor(android.graphics.Color.parseColor("#8E8E8E"));
            }
            if (tvFooter != null) {
                tvFooter.setTextColor(android.graphics.Color.parseColor("#8E8E8E"));
            }
            
            int boxColor = android.graphics.Color.parseColor("#444446");
            int hintColorVal = android.graphics.Color.parseColor("#8E8E8E");
            android.content.res.ColorStateList stateList = android.content.res.ColorStateList.valueOf(hintColorVal);

            if (tilUsername != null) {
                tilUsername.setBoxStrokeColor(boxColor);
                tilUsername.setBoxStrokeColorStateList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1962D1")));
                tilUsername.setDefaultHintTextColor(stateList);
                tilUsername.setHintTextColor(stateList);
            }
            if (tilPassword != null) {
                tilPassword.setBoxStrokeColor(boxColor);
                tilPassword.setBoxStrokeColorStateList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1962D1")));
                tilPassword.setDefaultHintTextColor(stateList);
                tilPassword.setHintTextColor(stateList);
            }
        } else {
            // Light theme
            if (root != null) {
                root.setBackgroundColor(getResources().getColor(R.color.background));
            }
            if (tabLayout != null) {
                tabLayout.setTabTextColors(getResources().getColor(R.color.primary), android.graphics.Color.WHITE);
            }
            if (cardForm != null && cardForm instanceof androidx.cardview.widget.CardView) {
                ((androidx.cardview.widget.CardView) cardForm).setCardBackgroundColor(getResources().getColor(R.color.white));
            }
            if (tvInputLabel != null) {
                tvInputLabel.setTextColor(getResources().getColor(R.color.text_secondary));
            }
            if (tvPasswordLabel != null) {
                tvPasswordLabel.setTextColor(getResources().getColor(R.color.text_secondary));
            }
            if (etUsername != null) {
                etUsername.setTextColor(getResources().getColor(R.color.text_primary));
                etUsername.setHintTextColor(getResources().getColor(R.color.text_secondary));
            }
            if (etPassword != null) {
                etPassword.setTextColor(getResources().getColor(R.color.text_primary));
                etPassword.setHintTextColor(getResources().getColor(R.color.text_secondary));
            }
            if (tvFooter != null) {
                tvFooter.setTextColor(getResources().getColor(R.color.grey));
            }
            
            int boxColor = getResources().getColor(R.color.divider);
            int hintColorVal = getResources().getColor(R.color.text_secondary);
            android.content.res.ColorStateList stateList = android.content.res.ColorStateList.valueOf(hintColorVal);

            if (tilUsername != null) {
                tilUsername.setBoxStrokeColor(boxColor);
                tilUsername.setBoxStrokeColorStateList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary)));
                tilUsername.setDefaultHintTextColor(stateList);
                tilUsername.setHintTextColor(stateList);
            }
            if (tilPassword != null) {
                tilPassword.setBoxStrokeColor(boxColor);
                tilPassword.setBoxStrokeColorStateList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary)));
                tilPassword.setDefaultHintTextColor(stateList);
                tilPassword.setHintTextColor(stateList);
            }
        }
    }
}
