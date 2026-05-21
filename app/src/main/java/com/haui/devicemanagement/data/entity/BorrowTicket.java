package com.haui.devicemanagement.data.entity;

/**
 * Entity tương ứng bảng `borrow_tickets`.
 * Lưu phiếu mượn cấp tổng.
 *
 * status: pending | approved | borrowed | returned | rejected | overdue | partially_returned
 */
public class BorrowTicket {
    private int    id;
    private String ticketCode;
    private int    userId;              // FK → users.id
    private String status;
    private String borrowReason;
    private String expectedReturnDate;
    private int    approvedBy;          // FK → admin.id (0 nếu chưa duyệt)
    private String approvedAt;
    private String createdAt;
    private String adminNote;

    // Trường bổ sung (computed, không lưu DB)
    private String userFullName;        // join với users.full_name
    private String userMssv;            // join với users.mssv
    private String approvedByName;      // join với admin.full_name

    // ─── Constructor rỗng ──────────────────────────────────────────────────────
    public BorrowTicket() {}

    // ─── Constructor đầy đủ ────────────────────────────────────────────────────
    public BorrowTicket(int id, String ticketCode, int userId, String status,
                        String borrowReason, String expectedReturnDate,
                        int approvedBy, String approvedAt,
                        String createdAt, String adminNote) {
        this.id                 = id;
        this.ticketCode         = ticketCode;
        this.userId             = userId;
        this.status             = status;
        this.borrowReason       = borrowReason;
        this.expectedReturnDate = expectedReturnDate;
        this.approvedBy         = approvedBy;
        this.approvedAt         = approvedAt;
        this.createdAt          = createdAt;
        this.adminNote          = adminNote;
    }

    // ─── Getters / Setters ─────────────────────────────────────────────────────
    public int  getId()         { return id; }
    public void setId(int id)   { this.id = id; }

    public String getTicketCode()                    { return ticketCode; }
    public void   setTicketCode(String ticketCode)   { this.ticketCode = ticketCode; }

    public int  getUserId()            { return userId; }
    public void setUserId(int userId)  { this.userId = userId; }

    public String getStatus()               { return status; }
    public void   setStatus(String status)  { this.status = status; }

    public String getBorrowReason()                      { return borrowReason; }
    public void   setBorrowReason(String borrowReason)   { this.borrowReason = borrowReason; }

    public String getExpectedReturnDate()                              { return expectedReturnDate; }
    public void   setExpectedReturnDate(String expectedReturnDate)     { this.expectedReturnDate = expectedReturnDate; }

    public int  getApprovedBy()                { return approvedBy; }
    public void setApprovedBy(int approvedBy)  { this.approvedBy = approvedBy; }

    public String getApprovedAt()                    { return approvedAt; }
    public void   setApprovedAt(String approvedAt)   { this.approvedAt = approvedAt; }

    public String getCreatedAt()                   { return createdAt; }
    public void   setCreatedAt(String createdAt)   { this.createdAt = createdAt; }

    public String getAdminNote()                   { return adminNote; }
    public void   setAdminNote(String adminNote)   { this.adminNote = adminNote; }

    public String getUserFullName()                        { return userFullName; }
    public void   setUserFullName(String userFullName)     { this.userFullName = userFullName; }

    public String getUserMssv()                  { return userMssv; }
    public void   setUserMssv(String userMssv)   { this.userMssv = userMssv; }

    public String getApprovedByName()                        { return approvedByName; }
    public void   setApprovedByName(String approvedByName)   { this.approvedByName = approvedByName; }

    // ─── Helpers ───────────────────────────────────────────────────────────────
    public boolean isPending()            { return "pending".equals(status); }
    public boolean isApproved()           { return "approved".equals(status); }
    public boolean isBorrowed()           { return "borrowed".equals(status); }
    public boolean isReturned()           { return "returned".equals(status); }
    public boolean isRejected()           { return "rejected".equals(status); }
    public boolean isOverdue()            { return "overdue".equals(status); }
    public boolean isPartiallyReturned()  { return "partially_returned".equals(status); }

    @Override
    public String toString() {
        return "BorrowTicket{id=" + id + ", ticketCode='" + ticketCode
                + "', status='" + status + "', userId=" + userId + "}";
    }
}
