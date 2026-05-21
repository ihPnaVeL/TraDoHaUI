package com.haui.devicemanagement.data.entity;

/**
 * Entity tương ứng bảng `return_items`.
 * Lưu từng dòng thiết bị trả trong một phiếu trả.
 *
 * condition_in: good | damaged | lost
 */
public class ReturnItem {
    private int    id;
    private int    returnTicketId;   // FK → return_tickets.id
    private int    borrowItemId;     // FK → borrow_items.id
    private int    deviceDetailId;   // FK → device_detail.id
    private String conditionIn;      // good | damaged | lost
    private String accessoriesIn;
    private String damageNote;
    private int    penaltyAmount;
    private int    isCompleted;      // 0 | 1

    // Trường bổ sung (computed, không lưu DB)
    private String assetCode;
    private String deviceName;
    private String serialNumber;

    // ─── Constructor rỗng ──────────────────────────────────────────────────────
    public ReturnItem() {}

    // ─── Constructor đầy đủ ────────────────────────────────────────────────────
    public ReturnItem(int id, int returnTicketId, int borrowItemId, int deviceDetailId,
                      String conditionIn, String accessoriesIn, String damageNote,
                      int penaltyAmount, int isCompleted) {
        this.id              = id;
        this.returnTicketId  = returnTicketId;
        this.borrowItemId    = borrowItemId;
        this.deviceDetailId  = deviceDetailId;
        this.conditionIn     = conditionIn;
        this.accessoriesIn   = accessoriesIn;
        this.damageNote      = damageNote;
        this.penaltyAmount   = penaltyAmount;
        this.isCompleted     = isCompleted;
    }

    // ─── Getters / Setters ─────────────────────────────────────────────────────
    public int  getId()         { return id; }
    public void setId(int id)   { this.id = id; }

    public int  getReturnTicketId()                    { return returnTicketId; }
    public void setReturnTicketId(int returnTicketId)  { this.returnTicketId = returnTicketId; }

    public int  getBorrowItemId()                  { return borrowItemId; }
    public void setBorrowItemId(int borrowItemId)  { this.borrowItemId = borrowItemId; }

    public int  getDeviceDetailId()                    { return deviceDetailId; }
    public void setDeviceDetailId(int deviceDetailId)  { this.deviceDetailId = deviceDetailId; }

    public String getConditionIn()                     { return conditionIn; }
    public void   setConditionIn(String conditionIn)   { this.conditionIn = conditionIn; }

    public String getAccessoriesIn()                       { return accessoriesIn; }
    public void   setAccessoriesIn(String accessoriesIn)   { this.accessoriesIn = accessoriesIn; }

    public String getDamageNote()                    { return damageNote; }
    public void   setDamageNote(String damageNote)   { this.damageNote = damageNote; }

    public int  getPenaltyAmount()                   { return penaltyAmount; }
    public void setPenaltyAmount(int penaltyAmount)  { this.penaltyAmount = penaltyAmount; }

    public int  getIsCompleted()                 { return isCompleted; }
    public void setIsCompleted(int isCompleted)  { this.isCompleted = isCompleted; }

    public boolean isCompleted() { return isCompleted == 1; }

    public String getAssetCode()                   { return assetCode; }
    public void   setAssetCode(String assetCode)   { this.assetCode = assetCode; }

    public String getDeviceName()                    { return deviceName; }
    public void   setDeviceName(String deviceName)   { this.deviceName = deviceName; }

    public String getSerialNumber()                      { return serialNumber; }
    public void   setSerialNumber(String serialNumber)   { this.serialNumber = serialNumber; }

    // ─── Helpers ───────────────────────────────────────────────────────────────
    public boolean isConditionGood()    { return "good".equals(conditionIn); }
    public boolean isConditionDamaged() { return "damaged".equals(conditionIn); }
    public boolean isConditionLost()    { return "lost".equals(conditionIn); }

    @Override
    public String toString() {
        return "ReturnItem{id=" + id + ", returnTicketId=" + returnTicketId
                + ", deviceDetailId=" + deviceDetailId
                + ", conditionIn='" + conditionIn + "', isCompleted=" + isCompleted + "}";
    }
}
