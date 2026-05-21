package com.haui.devicemanagement.data.entity;

/**
 * Entity tương ứng bảng `return_tickets`.
 * Lưu phiếu trả cấp tổng.
 *
 * status: pending | completed | damaged | lost
 */
public class ReturnTicket {
    private int    id;
    private String ticketCode;
    private int    borrowTicketId;    // FK → borrow_tickets.id
    private int    userId;            // FK → users.id
    private String status;
    private String returnedAt;
    private int    confirmedBy;       // FK → admin.id (0 nếu chưa xác nhận)
    private String confirmedAt;
    private String overallCondition;
    private String note;

    // Trường bổ sung (computed, không lưu DB)
    private String userFullName;
    private String userMssv;
    private String borrowTicketCode;
    private String confirmedByName;

    // ─── Constructor rỗng ──────────────────────────────────────────────────────
    public ReturnTicket() {}

    // ─── Constructor đầy đủ ────────────────────────────────────────────────────
    public ReturnTicket(int id, String ticketCode, int borrowTicketId, int userId,
                        String status, String returnedAt, int confirmedBy,
                        String confirmedAt, String overallCondition, String note) {
        this.id               = id;
        this.ticketCode       = ticketCode;
        this.borrowTicketId   = borrowTicketId;
        this.userId           = userId;
        this.status           = status;
        this.returnedAt       = returnedAt;
        this.confirmedBy      = confirmedBy;
        this.confirmedAt      = confirmedAt;
        this.overallCondition = overallCondition;
        this.note             = note;
    }

    // ─── Getters / Setters ─────────────────────────────────────────────────────
    public int  getId()         { return id; }
    public void setId(int id)   { this.id = id; }

    public String getTicketCode()                    { return ticketCode; }
    public void   setTicketCode(String ticketCode)   { this.ticketCode = ticketCode; }

    public int  getBorrowTicketId()                    { return borrowTicketId; }
    public void setBorrowTicketId(int borrowTicketId)  { this.borrowTicketId = borrowTicketId; }

    public int  getUserId()            { return userId; }
    public void setUserId(int userId)  { this.userId = userId; }

    public String getStatus()               { return status; }
    public void   setStatus(String status)  { this.status = status; }

    public String getReturnedAt()                    { return returnedAt; }
    public void   setReturnedAt(String returnedAt)   { this.returnedAt = returnedAt; }

    public int  getConfirmedBy()                   { return confirmedBy; }
    public void setConfirmedBy(int confirmedBy)     { this.confirmedBy = confirmedBy; }

    public String getConfirmedAt()                     { return confirmedAt; }
    public void   setConfirmedAt(String confirmedAt)   { this.confirmedAt = confirmedAt; }

    public String getOverallCondition()                          { return overallCondition; }
    public void   setOverallCondition(String overallCondition)   { this.overallCondition = overallCondition; }

    public String getNote()             { return note; }
    public void   setNote(String note)  { this.note = note; }

    public String getUserFullName()                        { return userFullName; }
    public void   setUserFullName(String userFullName)     { this.userFullName = userFullName; }

    public String getUserMssv()                  { return userMssv; }
    public void   setUserMssv(String userMssv)   { this.userMssv = userMssv; }

    public String getBorrowTicketCode()                          { return borrowTicketCode; }
    public void   setBorrowTicketCode(String borrowTicketCode)   { this.borrowTicketCode = borrowTicketCode; }

    public String getConfirmedByName()                         { return confirmedByName; }
    public void   setConfirmedByName(String confirmedByName)   { this.confirmedByName = confirmedByName; }

    // ─── Helpers ───────────────────────────────────────────────────────────────
    public boolean isPending()    { return "pending".equals(status); }
    public boolean isCompleted()  { return "completed".equals(status); }
    public boolean isDamaged()    { return "damaged".equals(status); }
    public boolean isLost()       { return "lost".equals(status); }

    @Override
    public String toString() {
        return "ReturnTicket{id=" + id + ", ticketCode='" + ticketCode
                + "', status='" + status + "', borrowTicketId=" + borrowTicketId + "}";
    }
}
