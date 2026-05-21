package com.haui.devicemanagement.data.entity;

/**
 * Entity tương ứng bảng `borrow_items`.
 * Lưu từng dòng thiết bị trong một phiếu mượn.
 *
 * Lưu ý: device_detail_id có thể null khi sinh viên mới tạo phiếu.
 * Admin sẽ gán device_detail_id khi duyệt phiếu.
 */
public class BorrowItem {
    private int    id;
    private int    ticketId;         // FK → borrow_tickets.id
    private int    deviceDetailId;   // FK → device_detail.id (0 nếu chưa gán)
    private String conditionOut;     // tình trạng lúc giao
    private String accessoriesOut;   // phụ kiện đi kèm khi giao
    private String note;

    // Trường bổ sung (computed, không lưu DB)
    private String assetCode;        // join với device_detail.asset_code
    private String deviceName;       // join với devices.device_name
    private String serialNumber;     // join với device_detail.serial_number

    // ─── Constructor rỗng ──────────────────────────────────────────────────────
    public BorrowItem() {}

    // ─── Constructor đầy đủ ────────────────────────────────────────────────────
    public BorrowItem(int id, int ticketId, int deviceDetailId,
                      String conditionOut, String accessoriesOut, String note) {
        this.id             = id;
        this.ticketId       = ticketId;
        this.deviceDetailId = deviceDetailId;
        this.conditionOut   = conditionOut;
        this.accessoriesOut = accessoriesOut;
        this.note           = note;
    }

    // ─── Getters / Setters ─────────────────────────────────────────────────────
    public int  getId()         { return id; }
    public void setId(int id)   { this.id = id; }

    public int  getTicketId()              { return ticketId; }
    public void setTicketId(int ticketId)  { this.ticketId = ticketId; }

    public int  getDeviceDetailId()                    { return deviceDetailId; }
    public void setDeviceDetailId(int deviceDetailId)  { this.deviceDetailId = deviceDetailId; }

    public String getConditionOut()                      { return conditionOut; }
    public void   setConditionOut(String conditionOut)   { this.conditionOut = conditionOut; }

    public String getAccessoriesOut()                        { return accessoriesOut; }
    public void   setAccessoriesOut(String accessoriesOut)   { this.accessoriesOut = accessoriesOut; }

    public String getNote()             { return note; }
    public void   setNote(String note)  { this.note = note; }

    public String getAssetCode()                   { return assetCode; }
    public void   setAssetCode(String assetCode)   { this.assetCode = assetCode; }

    public String getDeviceName()                    { return deviceName; }
    public void   setDeviceName(String deviceName)   { this.deviceName = deviceName; }

    public String getSerialNumber()                      { return serialNumber; }
    public void   setSerialNumber(String serialNumber)   { this.serialNumber = serialNumber; }

    public boolean isAssigned() { return deviceDetailId > 0; }

    @Override
    public String toString() {
        return "BorrowItem{id=" + id + ", ticketId=" + ticketId
                + ", deviceDetailId=" + deviceDetailId + "}";
    }
}
