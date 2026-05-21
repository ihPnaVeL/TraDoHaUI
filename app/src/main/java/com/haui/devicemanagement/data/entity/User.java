package com.haui.devicemanagement.data.entity;

/**
 * Entity tương ứng bảng `users`.
 * Lưu tài khoản sinh viên/người mượn thiết bị.
 */
public class User {
    private int    id;
    private String mssv;
    private String fullName;
    private String passwordHash;
    private String phone;
    private String email;
    private String className;
    private String faculty;
    private int    isActive;
    private String createdAt;

    // ─── Constructor rỗng ──────────────────────────────────────────────────────
    public User() {}

    // ─── Constructor đầy đủ ────────────────────────────────────────────────────
    public User(int id, String mssv, String fullName, String passwordHash,
                String phone, String email, String className, String faculty,
                int isActive, String createdAt) {
        this.id           = id;
        this.mssv         = mssv;
        this.fullName     = fullName;
        this.passwordHash = passwordHash;
        this.phone        = phone;
        this.email        = email;
        this.className    = className;
        this.faculty      = faculty;
        this.isActive     = isActive;
        this.createdAt    = createdAt;
    }

    // ─── Getters / Setters ─────────────────────────────────────────────────────
    public int    getId()           { return id; }
    public void   setId(int id)     { this.id = id; }

    public String getMssv()              { return mssv; }
    public void   setMssv(String mssv)   { this.mssv = mssv; }

    public String getFullName()                   { return fullName; }
    public void   setFullName(String fullName)     { this.fullName = fullName; }

    public String getPasswordHash()                         { return passwordHash; }
    public void   setPasswordHash(String passwordHash)       { this.passwordHash = passwordHash; }

    public String getPhone()               { return phone; }
    public void   setPhone(String phone)   { this.phone = phone; }

    public String getEmail()               { return email; }
    public void   setEmail(String email)   { this.email = email; }

    public String getClassName()                   { return className; }
    public void   setClassName(String className)   { this.className = className; }

    public String getFaculty()                 { return faculty; }
    public void   setFaculty(String faculty)   { this.faculty = faculty; }

    public int  getIsActive()              { return isActive; }
    public void setIsActive(int isActive)  { this.isActive = isActive; }

    public boolean isActive() { return isActive == 1; }

    public String getCreatedAt()                   { return createdAt; }
    public void   setCreatedAt(String createdAt)   { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{id=" + id + ", mssv='" + mssv + "', fullName='" + fullName + "'}";
    }
}
