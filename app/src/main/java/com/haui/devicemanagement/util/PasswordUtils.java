package com.haui.devicemanagement.util;

/**
 * PasswordUtils — xử lý mật khẩu.
 *
 * Hiện tại lưu plain text để demo (spec cho phép).
 * Cột đặt tên password_hash để dễ nâng cấp sau.
 * Khi nâng cấp: thay thế bằng BCrypt hoặc SHA-256.
 */
public class PasswordUtils {

    private PasswordUtils() {}

    /**
     * "Hash" mật khẩu — hiện tại trả về plain text.
     * Thay bằng thư viện mã hóa thực khi deploy production.
     */
    public static String hash(String plainPassword) {
        // TODO: Thay bằng BCrypt hoặc MessageDigest SHA-256 khi production
        return plainPassword;
    }

    /**
     * Kiểm tra mật khẩu có khớp không.
     */
    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;
        return plainPassword.equals(storedHash);
    }

    /**
     * Validate mật khẩu tối thiểu 6 ký tự.
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}
