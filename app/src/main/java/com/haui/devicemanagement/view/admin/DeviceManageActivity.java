package com.haui.devicemanagement.view.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.Device;
import com.haui.devicemanagement.presenter.DevicePresenter;
import com.haui.devicemanagement.view.adapter.DeviceAdapter;

import java.util.List;

public class DeviceManageActivity extends AppCompatActivity 
        implements DevicePresenter.DeviceListView, DevicePresenter.DeviceActionView, DeviceAdapter.OnDeviceClickListener {

    private DevicePresenter presenter;
    private DeviceAdapter adapter;
    private RecyclerView rvDevices;
    private EditText etSearch;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manage);

        presenter = new DevicePresenter(DatabaseHelper.getInstance(this));

        initViews();
        setupRecyclerView();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDevices();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý loại thiết bị");
        }

        rvDevices = findViewById(R.id.rvDevices);
        etSearch = findViewById(R.id.etSearch);
        fabAdd = findViewById(R.id.fabAdd);
    }

    private void setupRecyclerView() {
        rvDevices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceAdapter(false, this);
        rvDevices.setAdapter(adapter);
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    loadDevices();
                } else {
                    presenter.searchDevices(keyword, DeviceManageActivity.this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fabAdd.setOnClickListener(v -> showDeviceDialog(null));
    }

    private void loadDevices() {
        presenter.loadAllDevices(this);
    }

    private void showDeviceDialog(Device device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_device, null);
        builder.setView(dialogView);

        EditText etCode = dialogView.findViewById(R.id.etDeviceCode);
        EditText etName = dialogView.findViewById(R.id.etDeviceName);
        EditText etCategory = dialogView.findViewById(R.id.etCategory);
        EditText etBrand = dialogView.findViewById(R.id.etBrand);
        EditText etModel = dialogView.findViewById(R.id.etModel);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        boolean isEdit = device != null;
        if (isEdit) {
            etCode.setText(device.getDeviceCode());
            etCode.setEnabled(false); // Do not allow editing device code
            etName.setText(device.getDeviceName());
            etCategory.setText(device.getCategory());
            etBrand.setText(device.getBrand());
            etModel.setText(device.getModel());
            etDescription.setText(device.getDescription());
            builder.setTitle("Sửa loại thiết bị");
        } else {
            builder.setTitle("Thêm loại thiết bị");
        }

        builder.setPositiveButton(isEdit ? "Cập nhật" : "Lưu", null);
        builder.setNegativeButton("Hủy", null);

        if (isEdit) {
            builder.setNeutralButton("Xóa", (dialog, which) -> {
                new AlertDialog.Builder(this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa loại thiết bị này không? Hành động này không thể hoàn tác nếu không có thiết bị vật lý liên quan.")
                        .setPositiveButton("Xóa", (d, w) -> presenter.deleteDevice(device.getId(), DeviceManageActivity.this))
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String code = etCode.getText().toString().trim();
                String name = etName.getText().toString().trim();
                String category = etCategory.getText().toString().trim();
                String brand = etBrand.getText().toString().trim();
                String model = etModel.getText().toString().trim();
                String desc = etDescription.getText().toString().trim();

                if (code.isEmpty() || name.isEmpty() || category.isEmpty()) {
                    Toast.makeText(DeviceManageActivity.this, "Vui lòng nhập đầy đủ mã, tên và danh mục", Toast.LENGTH_SHORT).show();
                    return;
                }

                Device d = isEdit ? device : new Device();
                d.setDeviceCode(code);
                d.setDeviceName(name);
                d.setCategory(category);
                d.setBrand(brand);
                d.setModel(model);
                d.setDescription(desc);

                if (isEdit) {
                    presenter.updateDevice(d, DeviceManageActivity.this);
                } else {
                    presenter.insertDevice(d, DeviceManageActivity.this);
                }
                alertDialog.dismiss();
            });
        });
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_manage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_physical_devices) {
            startActivity(new Intent(this, DeviceDetailManageActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ─── PRESENTER CALLBACKS ───────────────────────────────────────────────────

    @Override
    public void onDevicesLoaded(List<Device> devices) {
        adapter.setDevices(devices);
    }

    @Override
    public void onSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        loadDevices();
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // ─── ADAPTER CLICK CALLBACKS ───────────────────────────────────────────────

    @Override
    public void onDeviceClick(Device device) {
        // Clicking a category displays physical items belonging to it
        Intent intent = new Intent(this, DeviceDetailManageActivity.class);
        intent.putExtra("device_id", device.getId());
        startActivity(intent);
    }

    @Override
    public void onSelectionChanged(List<Integer> selectedDeviceIds) {
        // Not used here
    }
}