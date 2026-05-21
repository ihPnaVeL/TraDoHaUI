package com.haui.devicemanagement.view.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.Device;
import com.haui.devicemanagement.presenter.DevicePresenter;
import com.haui.devicemanagement.view.adapter.DeviceAdapter;

import java.util.ArrayList;
import java.util.List;

public class DeviceSearchActivity extends AppCompatActivity implements DevicePresenter.DeviceListView {

    private DevicePresenter presenter;
    private DeviceAdapter adapter;
    private List<Device> allDevicesList = new ArrayList<>();
    
    private SearchView searchView;
    private Spinner spinnerCategory;
    private RecyclerView rvDevices;
    private TextView tvEmpty;
    private LinearLayout layoutBottomBar;
    private TextView tvSelectedCount;
    private MaterialButton btnContinue;

    private String currentKeyword = "";
    private String currentCategory = "Tất cả";
    private List<Integer> preSelectedIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_search);

        preSelectedIds = getIntent().getIntegerArrayListExtra("pre_selected_device_ids");

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSpinner();
        setupSearchView();
        setupActions();

        presenter = new DevicePresenter(DatabaseHelper.getInstance(this));
        loadData();
    }

    private void initViews() {
        searchView = findViewById(R.id.searchView);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        rvDevices = findViewById(R.id.rvDevices);
        tvEmpty = findViewById(R.id.tvEmpty);
        layoutBottomBar = findViewById(R.id.layoutBottomBar);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        btnContinue = findViewById(R.id.btnContinue);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tìm kiếm thiết bị");
        }
    }

    private void setupRecyclerView() {
        rvDevices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceAdapter(true, new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onDeviceClick(Device device) {
                // Click xem chi tiết (nếu cần)
            }

            @Override
            public void onSelectionChanged(List<Integer> selectedDeviceIds) {
                updateBottomBar(selectedDeviceIds);
            }
        });
        rvDevices.setAdapter(adapter);
    }

    private void setupSpinner() {
        // Danh sách danh mục tĩnh dựa theo spec
        String[] categories = {"Tất cả", "Laptop", "Projector", "Audio", "Tablet"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCategory = categories[position];
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentKeyword = query.trim();
                applyFilter();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentKeyword = newText.trim();
                applyFilter();
                return true;
            }
        });
    }

    private void setupActions() {
        btnContinue.setOnClickListener(v -> {
            List<Integer> selectedIds = adapter.getSelectedDeviceIds();
            if (selectedIds.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 thiết bị", Toast.LENGTH_SHORT).show();
                return;
            }
            if (getIntent().getBooleanExtra("IS_SELECTION_MODE", false)) {
                Intent resultIntent = new Intent();
                resultIntent.putIntegerArrayListExtra("selected_device_ids", new ArrayList<>(selectedIds));
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Intent intent = new Intent(this, BorrowCreateActivity.class);
                intent.putIntegerArrayListExtra("selected_device_ids", new ArrayList<>(selectedIds));
                startActivity(intent);
            }
        });
    }

    private void loadData() {
        presenter.loadAllDevices(this);
    }

    private void applyFilter() {
        List<Device> filtered = new ArrayList<>();
        for (Device d : allDevicesList) {
            boolean matchesKeyword = currentKeyword.isEmpty() ||
                    d.getDeviceName().toLowerCase().contains(currentKeyword.toLowerCase()) ||
                    d.getDeviceCode().toLowerCase().contains(currentKeyword.toLowerCase()) ||
                    (d.getBrand() != null && d.getBrand().toLowerCase().contains(currentKeyword.toLowerCase()));

            boolean matchesCategory = currentCategory.equals("Tất cả") ||
                    currentCategory.equalsIgnoreCase(d.getCategory());

            if (matchesKeyword && matchesCategory) {
                filtered.add(d);
            }
        }
        
        adapter.setDevices(filtered);
        
        if (filtered.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvDevices.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvDevices.setVisibility(View.VISIBLE);
        }
    }

    private void updateBottomBar(List<Integer> selectedDeviceIds) {
        if (selectedDeviceIds.isEmpty()) {
            layoutBottomBar.setVisibility(View.GONE);
        } else {
            layoutBottomBar.setVisibility(View.VISIBLE);
            tvSelectedCount.setText("Đã chọn: " + selectedDeviceIds.size() + " loại thiết bị");
        }
    }

    @Override
    public void onDevicesLoaded(List<Device> devices) {
        allDevicesList.clear();
        if (devices != null) {
            allDevicesList.addAll(devices);
        }
        applyFilter();

        if (preSelectedIds != null && !preSelectedIds.isEmpty()) {
            adapter.setSelectedDeviceIds(preSelectedIds);
            updateBottomBar(preSelectedIds);
            preSelectedIds = null;
        }
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, "Lỗi: " + message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}