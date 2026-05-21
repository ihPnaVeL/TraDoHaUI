package com.haui.devicemanagement.data.entity;

/**
 * Entity tương ứng bảng `device_detail`.
 * Lưu từng thiết bị vật lý cụ thể (mã tài sản, serial, tình trạng).
 */
public class DeviceDetail {
    private int    id;
    private int    deviceId;       // FK → devices.id
    private String assetCode;
    private String serialNumber;
    private String roomLocation;
    private String conditionStatus;     // good | fair | damaged
    private String availabilityStatus;  // available | borrowed | maintenance | lost
    private String purchaseDate;
    private String note;

    // Trường bổ sung (computed, không lưu DB)
    private String deviceName; // join với devices.device_name để hiển thị

    // ─── Constructor rỗng ──────────────────────────────────────────────────────
    public DeviceDetail() {}

    // ─── Constructor đầy đủ ────────────────────────────────────────────────────
    public DeviceDetail(int id, int deviceId, String assetCode, String serialNumber,
                        String roomLocation, String conditionStatus,
                        String availabilityStatus, String purchaseDate, String note) {
        this.id                  = id;
        this.deviceId            = deviceId;
        this.assetCode           = assetCode;
        this.serialNumber        = serialNumber;
        this.roomLocation        = roomLocation;
        this.conditionStatus     = conditionStatus;
        this.availabilityStatus  = availabilityStatus;
        this.purchaseDate        = purchaseDate;
        this.note                = note;
    }

    // ─── Getters / Setters ─────────────────────────────────────────────────────
    public int  getId()         { return id; }
    public void setId(int id)   { this.id = id; }

    public int  getDeviceId()            { return deviceId; }
    public void setDeviceId(int deviceId){ this.deviceId = deviceId; }

    public String getAssetCode()                   { return assetCode; }
    public void   setAssetCode(String assetCode)   { this.assetCode = assetCode; }

    public String getSerialNumber()                      { return serialNumber; }
    public void   setSerialNumber(String serialNumber)   { this.serialNumber = serialNumber; }

    public String getRoomLocation()                      { return roomLocation; }
    public void   setRoomLocation(String roomLocation)   { this.roomLocation = roomLocation; }

    public String getConditionStatus()                         { return conditionStatus; }
    public void   setConditionStatus(String conditionStatus)   { this.conditionStatus = conditionStatus; }

    public String getAvailabilityStatus()                              { return availabilityStatus; }
    public void   setAvailabilityStatus(String availabilityStatus)     { this.availabilityStatus = availabilityStatus; }

    public String getPurchaseDate()                      { return purchaseDate; }
    public void   setPurchaseDate(String purchaseDate)   { this.purchaseDate = purchaseDate; }

    public String getNote()             { return note; }
    public void   setNote(String note)  { this.note = note; }

    public String getDeviceName()                    { return deviceName; }
    public void   setDeviceName(String deviceName)   { this.deviceName = deviceName; }

    // ─── Helpers ───────────────────────────────────────────────────────────────
    public boolean isAvailable()   { return "available".equals(availabilityStatus); }
    public boolean isBorrowed()    { return "borrowed".equals(availabilityStatus); }
    public boolean isMaintenance() { return "maintenance".equals(availabilityStatus); }
    public boolean isLost()        { return "lost".equals(availabilityStatus); }

    @Override
    public String toString() {
        return "DeviceDetail{id=" + id + ", assetCode='" + assetCode
                + "', condition='" + conditionStatus
                + "', availability='" + availabilityStatus + "'}";
    }
}
