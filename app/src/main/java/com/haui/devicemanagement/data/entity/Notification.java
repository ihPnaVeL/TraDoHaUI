package com.haui.devicemanagement.data.entity;

/**
 * Entity tương ứng bảng `notifications`.
 * Lưu thông báo cho user hoặc admin.
 *
 * receiver_type: "user" | "admin"
 * type: "borrow" | "return" | "overdue" | "system"
 * ref_type: "borrow" | "return" | "device"
 */
public class Notification {
    private int    id;
    private String receiverType;  // user | admin
    private int    receiverId;
    private String type;          // borrow | return | overdue | system
    private String title;
    private String message;
    private int    refId;         // ID phiếu liên quan
    private String refType;       // borrow | return | device
    private int    isRead;        // 0 | 1
    private String createdAt;

    // ─── Constructor rỗng ──────────────────────────────────────────────────────
    public Notification() {}

    // ─── Constructor đầy đủ ────────────────────────────────────────────────────
    public Notification(int id, String receiverType, int receiverId, String type,
                        String title, String message, int refId, String refType,
                        int isRead, String createdAt) {
        this.id           = id;
        this.receiverType = receiverType;
        this.receiverId   = receiverId;
        this.type         = type;
        this.title        = title;
        this.message      = message;
        this.refId        = refId;
        this.refType      = refType;
        this.isRead       = isRead;
        this.createdAt    = createdAt;
    }

    // ─── Getters / Setters ─────────────────────────────────────────────────────
    public int  getId()         { return id; }
    public void setId(int id)   { this.id = id; }

    public String getReceiverType()                      { return receiverType; }
    public void   setReceiverType(String receiverType)   { this.receiverType = receiverType; }

    public int  getReceiverId()                { return receiverId; }
    public void setReceiverId(int receiverId)  { this.receiverId = receiverId; }

    public String getType()             { return type; }
    public void   setType(String type)  { this.type = type; }

    public String getTitle()               { return title; }
    public void   setTitle(String title)   { this.title = title; }

    public String getMessage()                 { return message; }
    public void   setMessage(String message)   { this.message = message; }

    public int  getRefId()           { return refId; }
    public void setRefId(int refId)  { this.refId = refId; }

    public String getRefType()                 { return refType; }
    public void   setRefType(String refType)   { this.refType = refType; }

    public int  getIsRead()            { return isRead; }
    public void setIsRead(int isRead)  { this.isRead = isRead; }

    public boolean isRead() { return isRead == 1; }

    public String getCreatedAt()                   { return createdAt; }
    public void   setCreatedAt(String createdAt)   { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Notification{id=" + id + ", receiverType='" + receiverType
                + "', receiverId=" + receiverId + ", title='" + title
                + "', isRead=" + isRead + "}";
    }
}
