package com.haui.devicemanagement.view.user;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.dao.DeviceDao;
import com.haui.devicemanagement.data.entity.Device;
import com.haui.devicemanagement.presenter.BorrowPresenter;
import com.haui.devicemanagement.util.SessionManager;
import com.haui.devicemanagement.view.adapter.DeviceAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BorrowCreateActivity extends AppCompatActivity implements BorrowPresenter.BorrowActionView {

    private static final int REQUEST_SELECT_DEVICES = 1001;

    private BorrowPresenter borrowPresenter;
    private SessionManager sessionManager;
    private List<Integer> selectedDeviceIds;
    private List<Device> selectedDevicesList = new ArrayList<>();
    
    private RecyclerView rvSelectedDevices;
    private android.widget.TextView tvNoDevices;
    private DeviceAdapter adapter;
    private TextInputEditText etReason;
    private TextInputEditText etExpectedDate;
    private MaterialButton btnSubmit;
    
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private String sqlFormattedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_create);

        selectedDeviceIds = getIntent().getIntegerArrayListExtra("selected_device_ids");
        if (selectedDeviceIds == null) {
            selectedDeviceIds = new ArrayList<>();
        }

        sessionManager = new SessionManager(this);
        borrowPresenter = new BorrowPresenter(DatabaseHelper.getInstance(this));

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupDatePicker();
        loadSelectedDevices();

        btnSubmit.setOnClickListener(v -> submitTicket());
        findViewById(R.id.btnSelectDevices).setOnClickListener(v -> {
            Intent intent = new Intent(this, DeviceSearchActivity.class);
            intent.putExtra("IS_SELECTION_MODE", true);
            intent.putIntegerArrayListExtra("pre_selected_device_ids", new ArrayList<>(selectedDeviceIds));
            startActivityForResult(intent, REQUEST_SELECT_DEVICES);
        });
    }

    private void initViews() {
        rvSelectedDevices = findViewById(R.id.rvSelectedDevices);
        tvNoDevices = findViewById(R.id.tvNoDevices);
        etReason = findViewById(R.id.etReason);
        etExpectedDate = findViewById(R.id.etExpectedDate);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tạo phiếu mượn");
        }
    }

    private void setupRecyclerView() {
        rvSelectedDevices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceAdapter(false, null);
        rvSelectedDevices.setAdapter(adapter);
    }

    private void setupDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            
            etExpectedDate.setText(displayDateFormat.format(calendar.getTime()));
            sqlFormattedDate = sqlDateFormat.format(calendar.getTime());
        };

        etExpectedDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                BorrowCreateActivity.this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            // Giới hạn không được chọn ngày trong quá khứ (phải từ ngày mai trở đi)
            Calendar minDate = Calendar.getInstance();
            minDate.add(Calendar.DAY_OF_MONTH, 1);
            dialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
            dialog.show();
        });
    }

    private void loadSelectedDevices() {
        DeviceDao deviceDao = new DeviceDao(DatabaseHelper.getInstance(this));
        selectedDevicesList.clear();
        for (int id : selectedDeviceIds) {
            Device d = deviceDao.getById(id);
            if (d != null) {
                selectedDevicesList.add(d);
            }
        }
        adapter.setDevices(selectedDevicesList);

        if (selectedDevicesList.isEmpty()) {
            tvNoDevices.setVisibility(android.view.View.VISIBLE);
            rvSelectedDevices.setVisibility(android.view.View.GONE);
        } else {
            tvNoDevices.setVisibility(android.view.View.GONE);
            rvSelectedDevices.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void submitTicket() {
        String reason = etReason.getText() != null ? etReason.getText().toString().trim() : "";
        int userId = sessionManager.getAccountId();

        if (userId == -1) {
            Toast.makeText(this, "Chưa đăng nhập sinh viên!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDeviceIds == null || selectedDeviceIds.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 thiết bị để mượn!", Toast.LENGTH_SHORT).show();
            return;
        }

        borrowPresenter.createBorrowTicket(userId, selectedDeviceIds, reason, sqlFormattedDate, this);
    }

    @Override
    public void onSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        // Quay lại màn hình UserHome
        finish();
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, "Lỗi: " + message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_DEVICES && resultCode == RESULT_OK && data != null) {
            List<Integer> newSelectedIds = data.getIntegerArrayListExtra("selected_device_ids");
            if (newSelectedIds != null) {
                selectedDeviceIds.clear();
                selectedDeviceIds.addAll(newSelectedIds);
                loadSelectedDevices();
            }
        }
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