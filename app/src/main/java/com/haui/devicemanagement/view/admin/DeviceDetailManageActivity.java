package com.haui.devicemanagement.view.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.dao.DeviceDao;
import com.haui.devicemanagement.data.entity.Device;
import com.haui.devicemanagement.data.entity.DeviceDetail;
import com.haui.devicemanagement.presenter.DevicePresenter;
import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.DateUtils;
import com.haui.devicemanagement.view.adapter.DeviceDetailAdapter;

import java.util.ArrayList;
import java.util.List;

public class DeviceDetailManageActivity extends AppCompatActivity
        implements DevicePresenter.DeviceDetailListView, DevicePresenter.DeviceActionView, DeviceDetailAdapter.OnDeviceDetailClickListener {

    private DevicePresenter presenter;
    private DeviceDetailAdapter adapter;
    private RecyclerView rvDeviceDetails;
    private Spinner spFilterStatus;
    private Spinner spFilterCondition;
    private FloatingActionButton fabAdd;

    private int filterDeviceId = -1; // -1 means all
    private List<DeviceDetail> allDetails = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail_manage);

        filterDeviceId = getIntent().getIntExtra("device_id", -1);
        presenter = new DevicePresenter(DatabaseHelper.getInstance(this));

        initViews();
        setupSpinners();
        setupRecyclerView();
        setupListeners();

        com.haui.devicemanagement.util.ThemeHelper.applyDarkTheme(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDetails();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(filterDeviceId != -1 ? "Thiết bị chi tiết" : "Quản lý thiết bị vật lý");
        }

        rvDeviceDetails = findViewById(R.id.rvDeviceDetails);
        spFilterStatus = findViewById(R.id.spFilterStatus);
        spFilterCondition = findViewById(R.id.spFilterCondition);
        fabAdd = findViewById(R.id.fabAdd);
    }

    private void setupSpinners() {
        // Status filter values
        String[] statusOptions = {"Tất cả trạng thái", "Sẵn sàng (Available)", "Đang mượn (Borrowed)", "Bảo trì (Maintenance)", "Mất (Lost)"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterStatus.setAdapter(statusAdapter);

        // Condition filter values
        String[] conditionOptions = {"Tất cả tình trạng", "Tốt (Good)", "Trung bình (Fair)", "Hỏng (Damaged)"};
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, conditionOptions);
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterCondition.setAdapter(conditionAdapter);
    }

    private void setupRecyclerView() {
        rvDeviceDetails.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceDetailAdapter(this);
        rvDeviceDetails.setAdapter(adapter);
    }

    private void setupListeners() {
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spFilterStatus.setOnItemSelectedListener(filterListener);
        spFilterCondition.setOnItemSelectedListener(filterListener);

        fabAdd.setOnClickListener(v -> showDeviceDetailDialog(null));
    }

    private void loadDetails() {
        if (filterDeviceId != -1) {
            presenter.loadDeviceDetailsByType(filterDeviceId, this);
        } else {
            presenter.loadAllDeviceDetails(this);
        }
    }

    private void applyFilters() {
        int statusPos = spFilterStatus.getSelectedItemPosition();
        int condPos = spFilterCondition.getSelectedItemPosition();

        String targetStatus = null;
        switch (statusPos) {
            case 1: targetStatus = Constants.DEVICE_AVAILABLE; break;
            case 2: targetStatus = Constants.DEVICE_BORROWED; break;
            case 3: targetStatus = Constants.DEVICE_MAINTENANCE; break;
            case 4: targetStatus = Constants.DEVICE_LOST; break;
        }

        String targetCondition = null;
        switch (condPos) {
            case 1: targetCondition = Constants.CONDITION_GOOD; break;
            case 2: targetCondition = Constants.CONDITION_FAIR; break;
            case 3: targetCondition = Constants.CONDITION_DAMAGED; break;
        }

        List<DeviceDetail> filtered = new ArrayList<>();
        for (DeviceDetail d : allDetails) {
            boolean matchesStatus = targetStatus == null || targetStatus.equals(d.getAvailabilityStatus());
            boolean matchesCondition = targetCondition == null || targetCondition.equals(d.getConditionStatus());

            if (matchesStatus && matchesCondition) {
                filtered.add(d);
            }
        }
        adapter.setDetails(filtered);
    }

    private void showDeviceDetailDialog(DeviceDetail detail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_device_detail, null);
        builder.setView(dialogView);

        Spinner spDeviceType = dialogView.findViewById(R.id.spDeviceType);
        EditText etAssetCode = dialogView.findViewById(R.id.etAssetCode);
        EditText etSerialNumber = dialogView.findViewById(R.id.etSerialNumber);
        EditText etRoomLocation = dialogView.findViewById(R.id.etRoomLocation);
        Spinner spAvailabilityStatus = dialogView.findViewById(R.id.spAvailabilityStatus);
        Spinner spConditionStatus = dialogView.findViewById(R.id.spConditionStatus);
        EditText etNote = dialogView.findViewById(R.id.etNote);

        // Load and setup Device Type Spinner
        DeviceDao deviceDao = new DeviceDao(DatabaseHelper.getInstance(this));
        List<Device> devices = deviceDao.getAll();
        List<String> deviceNames = new ArrayList<>();
        for (Device d : devices) {
            deviceNames.add(d.getDeviceName() + " (" + d.getDeviceCode() + ")");
        }
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deviceNames);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDeviceType.setAdapter(typeAdapter);

        // Setup Availability Status Spinner
        String[] statusOptions = {Constants.DEVICE_AVAILABLE, Constants.DEVICE_BORROWED, Constants.DEVICE_MAINTENANCE, Constants.DEVICE_LOST};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAvailabilityStatus.setAdapter(statusAdapter);

        // Setup Condition Status Spinner
        String[] conditionOptions = {Constants.CONDITION_GOOD, Constants.CONDITION_FAIR, Constants.CONDITION_DAMAGED};
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, conditionOptions);
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spConditionStatus.setAdapter(conditionAdapter);

        boolean isEdit = detail != null;
        if (isEdit) {
            builder.setTitle("Sửa thiết bị vật lý");
            etAssetCode.setText(detail.getAssetCode());
            etAssetCode.setEnabled(false); // Do not edit asset code
            etSerialNumber.setText(detail.getSerialNumber());
            etRoomLocation.setText(detail.getRoomLocation());
            etNote.setText(detail.getNote());

            // Select active type
            for (int i = 0; i < devices.size(); i++) {
                if (devices.get(i).getId() == detail.getDeviceId()) {
                    spDeviceType.setSelection(i);
                    break;
                }
            }

            // Select active status
            for (int i = 0; i < statusOptions.length; i++) {
                if (statusOptions[i].equals(detail.getAvailabilityStatus())) {
                    spAvailabilityStatus.setSelection(i);
                    break;
                }
            }

            // Select active condition
            for (int i = 0; i < conditionOptions.length; i++) {
                if (conditionOptions[i].equals(detail.getConditionStatus())) {
                    spConditionStatus.setSelection(i);
                    break;
                }
            }
        } else {
            builder.setTitle("Thêm thiết bị vật lý");
            // If opened from a filtered category, default spinner to that category
            if (filterDeviceId != -1) {
                for (int i = 0; i < devices.size(); i++) {
                    if (devices.get(i).getId() == filterDeviceId) {
                        spDeviceType.setSelection(i);
                        break;
                    }
                }
            }
        }

        builder.setPositiveButton(isEdit ? "Cập nhật" : "Lưu", null);
        builder.setNegativeButton("Hủy", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        com.haui.devicemanagement.util.ThemeHelper.applyDarkThemeToDialog(alertDialog);

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String assetCode = etAssetCode.getText().toString().trim();
                String serial = etSerialNumber.getText().toString().trim();
                String room = etRoomLocation.getText().toString().trim();
                String note = etNote.getText().toString().trim();

                if (assetCode.isEmpty()) {
                    Toast.makeText(DeviceDetailManageActivity.this, "Vui lòng nhập mã tài sản", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (devices.isEmpty()) {
                    Toast.makeText(DeviceDetailManageActivity.this, "Cần có loại thiết bị gốc trước", Toast.LENGTH_SHORT).show();
                    return;
                }

                int deviceId = devices.get(spDeviceType.getSelectedItemPosition()).getId();
                String status = statusOptions[spAvailabilityStatus.getSelectedItemPosition()];
                String cond = conditionOptions[spConditionStatus.getSelectedItemPosition()];

                DeviceDetail dd = isEdit ? detail : new DeviceDetail();
                dd.setDeviceId(deviceId);
                dd.setAssetCode(assetCode);
                dd.setSerialNumber(serial);
                dd.setRoomLocation(room);
                dd.setAvailabilityStatus(status);
                dd.setConditionStatus(cond);
                dd.setNote(note);

                if (!isEdit) {
                    dd.setPurchaseDate(DateUtils.getCurrentDate());
                    presenter.insertDeviceDetail(dd, DeviceDetailManageActivity.this);
                } else {
                    dd.setPurchaseDate(detail.getPurchaseDate());
                    presenter.updateDeviceDetail(dd, DeviceDetailManageActivity.this);
                }
                alertDialog.dismiss();
            });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ─── PRESENTER CALLBACKS ───────────────────────────────────────────────────

    @Override
    public void onDeviceDetailsLoaded(List<DeviceDetail> details) {
        allDetails.clear();
        if (details != null) {
            allDetails.addAll(details);
        }
        applyFilters();
    }

    @Override
    public void onSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        loadDetails();
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // ─── ADAPTER CLICK CALLBACKS ───────────────────────────────────────────────

    @Override
    public void onEditClick(DeviceDetail detail) {
        showDeviceDetailDialog(detail);
    }

    @Override
    public void onDeleteClick(DeviceDetail detail) {
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa thiết bị vật lý này không?")
                .setPositiveButton("Xóa", (dialog, which) -> presenter.deleteDeviceDetail(detail.getId(), DeviceDetailManageActivity.this))
                .setNegativeButton("Hủy", null)
                .show();
        com.haui.devicemanagement.util.ThemeHelper.applyDarkThemeToDialog(d);
    }
}