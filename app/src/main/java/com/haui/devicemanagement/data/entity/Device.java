package com.haui.devicemanagement.data.entity;

/**
 * Entity tương ứng bảng `devices`.
 * Lưu thông tin loại/dòng thiết bị (không phải thiết bị vật lý).
 */
public class Device {
    private int    id;
    private String deviceCode;
    private String deviceName;
    private String category;
    private String brand;
    private String model;
    private String description;
    private String createdAt;

    // Trường bổ sung (computed, không lưu DB)
    private int availableCount; // số lượng device_detail còn available

    // ─── Constructor rỗng ──────────────────────────────────────────────────────
    public Device() {}

    // ─── Constructor đầy đủ ────────────────────────────────────────────────────
    public Device(int id, String deviceCode, String deviceName, String category,
                  String brand, String model, String description, String createdAt) {
        this.id          = id;
        this.deviceCode  = deviceCode;
        this.deviceName  = deviceName;
        this.category    = category;
        this.brand       = brand;
        this.model       = model;
        this.description = description;
        this.createdAt   = createdAt;
    }

    // ─── Getters / Setters ─────────────────────────────────────────────────────
    public int    getId()         { return id; }
    public void   setId(int id)   { this.id = id; }

    public String getDeviceCode()                    { return deviceCode; }
    public void   setDeviceCode(String deviceCode)   { this.deviceCode = deviceCode; }

    public String getDeviceName()                    { return deviceName; }
    public void   setDeviceName(String deviceName)   { this.deviceName = deviceName; }

    public String getCategory()                  { return category; }
    public void   setCategory(String category)   { this.category = category; }

    public String getBrand()               { return brand; }
    public void   setBrand(String brand)   { this.brand = brand; }

    public String getModel()               { return model; }
    public void   setModel(String model)   { this.model = model; }

    public String getDescription()                   { return description; }
    public void   setDescription(String description) { this.description = description; }

    public String getCreatedAt()                   { return createdAt; }
    public void   setCreatedAt(String createdAt)   { this.createdAt = createdAt; }

    public int  getAvailableCount()                      { return availableCount; }
    public void setAvailableCount(int availableCount)    { this.availableCount = availableCount; }

    @Override
    public String toString() {
        return "Device{id=" + id + ", deviceCode='" + deviceCode
                + "', deviceName='" + deviceName + "', category='" + category + "'}";
    }
}
