package com.haui.devicemanagement.presenter;

import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.dao.AdminDao;
import com.haui.devicemanagement.data.dao.UserDao;
import com.haui.devicemanagement.data.entity.Admin;
import com.haui.devicemanagement.data.entity.User;
import com.haui.devicemanagement.util.Constants;

/**
 * AuthPresenter — xử lý logic đăng nhập và đổi mật khẩu.
 * Tuân theo MVP: Presenter gọi DAO, trả kết quả về View qua callback.
 */
public class AuthPresenter {

    // ─── View Interface ────────────────────────────────────────────────────────
    public interface LoginView {
        void onLoginSuccess(String accountType, int accountId, String fullName, String permission);
        void onLoginFailed(String message);
        void showLoading(boolean show);
    }

    public interface ChangePasswordView {
        void onChangePasswordSuccess();
        void onChangePasswordFailed(String message);
    }

    // ─── Fields ────────────────────────────────────────────────────────────────
    private final UserDao  userDao;
    private final AdminDao adminDao;

    public AuthPresenter(DatabaseHelper dbHelper) {
        this.userDao  = new UserDao(dbHelper);
        this.adminDao = new AdminDao(dbHelper);
    }

    // ─── LOGIN USER ────────────────────────────────────────────────────────────

    /**
     * Đăng nhập sinh viên bằng MSSV + password.
     */
    public void loginUser(String mssv, String password, LoginView view) {
        view.showLoading(true);

        if (mssv == null || mssv.trim().isEmpty()) {
            view.showLoading(false);
            view.onLoginFailed("Vui lòng nhập MSSV");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            view.showLoading(false);
            view.onLoginFailed("Vui lòng nhập mật khẩu");
            return;
        }

        User user = userDao.login(mssv.trim(), password);
        view.showLoading(false);

        if (user != null) {
            view.onLoginSuccess(
                Constants.ACCOUNT_USER,
                user.getId(),
                user.getFullName(),
                "" // user không có permission level
            );
        } else {
            // Phân biệt: sai mật khẩu hay tài khoản bị khóa?
            // Kiểm tra MSSV tồn tại không
            User existUser = getUserByMssv(mssv.trim());
            if (existUser != null && !existUser.isActive()) {
                view.onLoginFailed("Tài khoản đã bị khóa. Liên hệ quản trị viên.");
            } else {
                view.onLoginFailed("MSSV hoặc mật khẩu không đúng");
            }
        }
    }

    // ─── LOGIN ADMIN ───────────────────────────────────────────────────────────

    /**
     * Đăng nhập admin bằng email hoặc admin_code + password.
     */
    public void loginAdmin(String emailOrCode, String password, LoginView view) {
        view.showLoading(true);

        if (emailOrCode == null || emailOrCode.trim().isEmpty()) {
            view.showLoading(false);
            view.onLoginFailed("Vui lòng nhập email hoặc mã cán bộ");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            view.showLoading(false);
            view.onLoginFailed("Vui lòng nhập mật khẩu");
            return;
        }

        Admin admin = adminDao.login(emailOrCode.trim(), password);
        view.showLoading(false);

        if (admin != null) {
            view.onLoginSuccess(
                Constants.ACCOUNT_ADMIN,
                admin.getId(),
                admin.getFullName(),
                admin.getPermissionLevel()
            );
        } else {
            view.onLoginFailed("Email/mã cán bộ hoặc mật khẩu không đúng");
        }
    }

    // ─── CHANGE PASSWORD ───────────────────────────────────────────────────────

    public void changePasswordUser(int userId, String oldPassword, String newPassword,
                                    String confirmPassword, ChangePasswordView view) {
        if (!validatePasswordChange(oldPassword, newPassword, confirmPassword, view)) return;

        User user = userDao.getById(userId);
        if (user == null) {
            view.onChangePasswordFailed("Không tìm thấy tài khoản");
            return;
        }
        if (!oldPassword.equals(user.getPasswordHash())) {
            view.onChangePasswordFailed("Mật khẩu cũ không đúng");
            return;
        }

        int rows = userDao.changePassword(userId, newPassword);
        if (rows > 0) view.onChangePasswordSuccess();
        else view.onChangePasswordFailed("Đổi mật khẩu thất bại");
    }

    public void changePasswordAdmin(int adminId, String oldPassword, String newPassword,
                                     String confirmPassword, ChangePasswordView view) {
        if (!validatePasswordChange(oldPassword, newPassword, confirmPassword, view)) return;

        Admin admin = adminDao.getById(adminId);
        if (admin == null) {
            view.onChangePasswordFailed("Không tìm thấy tài khoản");
            return;
        }
        if (!oldPassword.equals(admin.getPasswordHash())) {
            view.onChangePasswordFailed("Mật khẩu cũ không đúng");
            return;
        }

        int rows = adminDao.changePassword(adminId, newPassword);
        if (rows > 0) view.onChangePasswordSuccess();
        else view.onChangePasswordFailed("Đổi mật khẩu thất bại");
    }

    // ─── PRIVATE HELPERS ───────────────────────────────────────────────────────

    private boolean validatePasswordChange(String oldPw, String newPw, String confirmPw,
                                            ChangePasswordView view) {
        if (oldPw == null || oldPw.isEmpty()) {
            view.onChangePasswordFailed("Vui lòng nhập mật khẩu cũ");
            return false;
        }
        if (newPw == null || newPw.length() < 6) {
            view.onChangePasswordFailed("Mật khẩu mới phải có ít nhất 6 ký tự");
            return false;
        }
        if (!newPw.equals(confirmPw)) {
            view.onChangePasswordFailed("Mật khẩu xác nhận không khớp");
            return false;
        }
        return true;
    }

    private User getUserByMssv(String mssv) {
        // Dùng để kiểm tra tài khoản có tồn tại không (tách với login)
        for (User u : userDao.getAll()) {
            if (mssv.equals(u.getMssv())) return u;
        }
        return null;
    }
}
