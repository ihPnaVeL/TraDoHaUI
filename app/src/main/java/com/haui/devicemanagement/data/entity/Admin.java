package com.haui.devicemanagement.data.entity;

/**
 * Entity tương ứng bảng `admin`.
 * Lưu tài khoản cán bộ quản lý.
 */
public class Admin {
    private int    id;
    private String adminCode;
    private String fullName;
    private String email;
    private String passwordHash;
    private String phone;
    private String permissionLevel; // "manager" hoặc "staff"
    private int    isActive;
    private String createdAt;

    // ─── Constructor rỗng ──────────────────────────────────────────────────────
    public Admin() {}

    // ─── Constructor đầy đủ ────────────────────────────────────────────────────
    public Admin(int id, String adminCode, String fullName, String email,
                 String passwordHash, String phone, String permissionLevel,
                 int isActive, String createdAt) {
        this.id              = id;
        this.adminCode       = adminCode;
        this.fullName        = fullName;
        this.email           = email;
        this.passwordHash    = passwordHash;
        this.phone           = phone;
        this.permissionLevel = permissionLevel;
        this.isActive        = isActive;
        this.createdAt       = createdAt;
    }

    // ─── Getters / Setters ─────────────────────────────────────────────────────
    public int    getId()         { return id; }
    public void   setId(int id)   { this.id = id; }

    public String getAdminCode()                   { return adminCode; }
    public void   setAdminCode(String adminCode)   { this.adminCode = adminCode; }

    public String getFullName()                   { return fullName; }
    public void   setFullName(String fullName)     { this.fullName = fullName; }

    public String getEmail()               { return email; }
    public void   setEmail(String email)   { this.email = email; }

    public String getPasswordHash()                         { return passwordHash; }
    public void   setPasswordHash(String passwordHash)       { this.passwordHash = passwordHash; }

    public String getPhone()               { return phone; }
    public void   setPhone(String phone)   { this.phone = phone; }

    public String getPermissionLevel()                         { return permissionLevel; }
    public void   setPermissionLevel(String permissionLevel)   { this.permissionLevel = permissionLevel; }

    public int  getIsActive()              { return isActive; }
    public void setIsActive(int isActive)  { this.isActive = isActive; }

    public boolean isActive()   { return isActive == 1; }
    public boolean isManager()  { return "manager".equals(permissionLevel); }

    public String getCreatedAt()                   { return createdAt; }
    public void   setCreatedAt(String createdAt)   { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Admin{id=" + id + ", adminCode='" + adminCode + "', fullName='" + fullName
                + "', permissionLevel='" + permissionLevel + "'}";
    }
}
