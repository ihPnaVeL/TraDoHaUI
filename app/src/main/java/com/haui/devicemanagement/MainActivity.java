package com.haui.devicemanagement;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.SessionManager;
import com.haui.devicemanagement.util.ThemeManager;
import com.haui.devicemanagement.view.auth.LoginActivity;
import com.haui.devicemanagement.view.admin.AdminDashboardActivity;
import com.haui.devicemanagement.view.user.UserHomeActivity;

/**
 * MainActivity — Entry point của ứng dụng.
 *
 * Kiểm tra session SharedPreferences:
 * - Nếu đã đăng nhập → điều hướng thẳng đến màn hình home tương ứng.
 * - Nếu chưa → chuyển đến LoginActivity.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);

        SessionManager session = new SessionManager(this);

        if (session.isLoggedIn()) {
            String accountType = session.getAccountType();
            if (Constants.ACCOUNT_ADMIN.equals(accountType)) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
            } else {
                startActivity(new Intent(this, UserHomeActivity.class));
            }
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish(); // MainActivity không giữ trong back stack
    }
}
