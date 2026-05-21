package com.haui.devicemanagement.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager — quản lý trạng thái đăng nhập qua SharedPreferences.
 *
 * Lưu: account_type (user/admin), account_id, full_name, is_logged_in, permission_level.
 */
public class SessionManager {

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ─── SAVE SESSION ──────────────────────────────────────────────────────────

    /**
     * Lưu session sau khi đăng nhập thành công.
     */
    public void saveSession(String accountType, int accountId, String fullName,
                             String permissionLevel) {
        editor.putString(Constants.KEY_ACCOUNT_TYPE, accountType);
        editor.putInt(Constants.KEY_ACCOUNT_ID, accountId);
        editor.putString(Constants.KEY_FULL_NAME, fullName);
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.putString(Constants.KEY_PERMISSION, permissionLevel != null ? permissionLevel : "");
        editor.apply();
    }

    // ─── CLEAR SESSION ─────────────────────────────────────────────────────────

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    // ─── CHECK LOGIN ───────────────────────────────────────────────────────────

    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    // ─── GETTERS ───────────────────────────────────────────────────────────────

    public String getAccountType() {
        return prefs.getString(Constants.KEY_ACCOUNT_TYPE, "");
    }

    public int getAccountId() {
        return prefs.getInt(Constants.KEY_ACCOUNT_ID, -1);
    }

    public String getFullName() {
        return prefs.getString(Constants.KEY_FULL_NAME, "");
    }

    public String getPermissionLevel() {
        return prefs.getString(Constants.KEY_PERMISSION, "");
    }

    // ─── HELPERS ───────────────────────────────────────────────────────────────

    public boolean isUser() {
        return Constants.ACCOUNT_USER.equals(getAccountType());
    }

    public boolean isAdmin() {
        return Constants.ACCOUNT_ADMIN.equals(getAccountType());
    }

    public boolean isManager() {
        return isAdmin() && Constants.PERM_MANAGER.equals(getPermissionLevel());
    }
}
