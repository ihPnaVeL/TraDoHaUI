package com.haui.devicemanagement.presenter;

import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.dao.DeviceDao;
import com.haui.devicemanagement.data.dao.DeviceDetailDao;
import com.haui.devicemanagement.data.entity.Device;
import com.haui.devicemanagement.data.entity.DeviceDetail;

import java.util.List;

/**
 * DevicePresenter — xử lý logic quản lý thiết bị.
 */
public class DevicePresenter {

    public interface DeviceListView {
        void onDevicesLoaded(List<Device> devices);
        void onError(String message);
    }

    public interface DeviceDetailListView {
        void onDeviceDetailsLoaded(List<DeviceDetail> details);
        void onError(String message);
    }

    public interface DeviceActionView {
        void onSuccess(String message);
        void onError(String message);
    }

    private final DeviceDao       deviceDao;
    private final DeviceDetailDao deviceDetailDao;

    public DevicePresenter(DatabaseHelper dbHelper) {
        this.deviceDao       = new DeviceDao(dbHelper);
        this.deviceDetailDao = new DeviceDetailDao(dbHelper);
    }

    // ─── DEVICE TYPE ───────────────────────────────────────────────────────────

    public void loadAllDevices(DeviceListView view) {
        List<Device> list = deviceDao.getAllWithAvailableCount();
        view.onDevicesLoaded(list);
    }

    public void loadAvailableDeviceTypes(DeviceListView view) {
        List<Device> list = deviceDao.getAvailableDeviceTypes();
        view.onDevicesLoaded(list);
    }

    public void searchDevices(String keyword, DeviceListView view) {
        List<Device> list = deviceDao.search(keyword);
        for (Device d : list) {
            d.setAvailableCount(deviceDao.getAvailableCountByDevice(d.getId()));
        }
        view.onDevicesLoaded(list);
    }

    public void insertDevice(Device device, DeviceActionView view) {
        if (device.getDeviceCode() == null || device.getDeviceCode().isEmpty()) {
            view.onError("Mã thiết bị không được trống");
            return;
        }
        if (device.getDeviceName() == null || device.getDeviceName().isEmpty()) {
            view.onError("Tên thiết bị không được trống");
            return;
        }
        long id = deviceDao.insert(device);
        if (id > 0) view.onSuccess("Thêm thiết bị thành công");
        else view.onError("Thêm thiết bị thất bại (mã đã tồn tại?)");
    }

    public void updateDevice(Device device, DeviceActionView view) {
        int rows = deviceDao.update(device);
        if (rows > 0) view.onSuccess("Cập nhật thành công");
        else view.onError("Cập nhật thất bại");
    }

    public void deleteDevice(int id, DeviceActionView view) {
        int rows = deviceDao.delete(id);
        if (rows > 0) view.onSuccess("Xóa thành công");
        else view.onError("Xóa thất bại. Có thể thiết bị này đang được sử dụng.");
    }

    // ─── DEVICE DETAIL ─────────────────────────────────────────────────────────

    public void loadAllDeviceDetails(DeviceDetailListView view) {
        List<DeviceDetail> list = deviceDetailDao.getAll();
        view.onDeviceDetailsLoaded(list);
    }

    public void loadDeviceDetailsByType(int deviceId, DeviceDetailListView view) {
        List<DeviceDetail> list = deviceDetailDao.getByDeviceId(deviceId);
        view.onDeviceDetailsLoaded(list);
    }

    public void loadAvailableDeviceDetails(int deviceId, DeviceDetailListView view) {
        List<DeviceDetail> list = deviceDetailDao.getAvailableByDeviceId(deviceId);
        view.onDeviceDetailsLoaded(list);
    }

    public void loadDeviceDetailsByStatus(String status, DeviceDetailListView view) {
        List<DeviceDetail> list = deviceDetailDao.getByAvailabilityStatus(status);
        view.onDeviceDetailsLoaded(list);
    }

    public void insertDeviceDetail(DeviceDetail detail, DeviceActionView view) {
        if (detail.getAssetCode() == null || detail.getAssetCode().isEmpty()) {
            view.onError("Mã tài sản không được trống");
            return;
        }
        long id = deviceDetailDao.insert(detail);
        if (id > 0) view.onSuccess("Thêm thiết bị vật lý thành công");
        else view.onError("Thêm thất bại (mã tài sản đã tồn tại?)");
    }

    public void updateDeviceDetail(DeviceDetail detail, DeviceActionView view) {
        int rows = deviceDetailDao.update(detail);
        if (rows > 0) view.onSuccess("Cập nhật thành công");
        else view.onError("Cập nhật thất bại");
    }

    public void deleteDeviceDetail(int id, DeviceActionView view) {
        int rows = deviceDetailDao.delete(id);
        if (rows > 0) view.onSuccess("Xóa thành công");
        else view.onError("Xóa thất bại");
    }
}
