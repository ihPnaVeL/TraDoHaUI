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
    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigation;

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

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_search);
        }
    }

    private void initViews() {
        searchView = findViewById(R.id.searchView);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        rvDevices = findViewById(R.id.rvDevices);
        tvEmpty = findViewById(R.id.tvEmpty);
        layoutBottomBar = findViewById(R.id.layoutBottomBar);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        btnContinue = findViewById(R.id.btnContinue);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        setupBottomNavigation();
        if (getIntent().getBooleanExtra("IS_SELECTION_MODE", false)) {
            bottomNavigation.setVisibility(View.GONE);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_search) {
                return true;
            }

            Intent intent = null;
            if (itemId == R.id.nav_home) {
                intent = new Intent(this, UserHomeActivity.class);
            } else if (itemId == R.id.nav_history) {
                intent = new Intent(this, HistoryActivity.class);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(this, UserProfileActivity.class);
            }

            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tìm kiếm thiết bị");
        }
        if (toolbar != null && toolbar.getNavigationIcon() != null) {
            androidx.core.graphics.drawable.DrawableCompat.setTint(
                androidx.core.graphics.drawable.DrawableCompat.wrap(toolbar.getNavigationIcon()), 
                android.graphics.Color.WHITE
            );
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
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                com.haui.devicemanagement.R.layout.spinner_item, categories) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(android.graphics.Color.WHITE);
                }
                return view;
            }
            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(android.graphics.Color.WHITE);
                    ((TextView) view).setBackgroundColor(android.graphics.Color.parseColor("#1C1C1E"));
                }
                return view;
            }
        };
        spinnerAdapter.setDropDownViewResource(com.haui.devicemanagement.R.layout.spinner_dropdown_item);
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
        // Style SearchView internal views for high contrast dark theme
        SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchAutoComplete != null) {
            searchAutoComplete.setTextColor(android.graphics.Color.WHITE);
            searchAutoComplete.setHintTextColor(android.graphics.Color.parseColor("#8E8E8E"));
        }
        android.widget.ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (searchIcon != null) {
            searchIcon.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
        }
        android.widget.ImageView closeIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (closeIcon != null) {
            closeIcon.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
        }

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
            if (!getIntent().getBooleanExtra("IS_SELECTION_MODE", false)) {
                bottomNavigation.setVisibility(View.VISIBLE);
            }
        } else {
            layoutBottomBar.setVisibility(View.VISIBLE);
            bottomNavigation.setVisibility(View.GONE);
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