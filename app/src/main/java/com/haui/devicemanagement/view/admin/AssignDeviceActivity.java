package com.haui.devicemanagement.view.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.BorrowItem;
import com.haui.devicemanagement.data.entity.BorrowTicket;
import com.haui.devicemanagement.data.entity.DeviceDetail;
import com.haui.devicemanagement.presenter.BorrowPresenter;
import com.haui.devicemanagement.presenter.DevicePresenter;
import com.haui.devicemanagement.util.SessionManager;
import com.haui.devicemanagement.view.adapter.BorrowItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class AssignDeviceActivity extends AppCompatActivity 
        implements BorrowPresenter.BorrowDetailView, BorrowPresenter.BorrowActionView {

    private int ticketId = -1;
    private BorrowPresenter borrowPresenter;
    private DevicePresenter devicePresenter;
    private SessionManager sessionManager;
    private BorrowItemAdapter adapter;

    private TextView tvTicketCode;
    private TextView tvStudentInfo;
    private TextView tvDateInfo;
    private RecyclerView rvItemsToAssign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_device);

        ticketId = getIntent().getIntExtra("ticket_id", -1);
        if (ticketId == -1) {
            Toast.makeText(this, "Không nhận diện được phiếu mượn!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sessionManager = new SessionManager(this);
        borrowPresenter = new BorrowPresenter(DatabaseHelper.getInstance(this));
        devicePresenter = new DevicePresenter(DatabaseHelper.getInstance(this));

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadData();
    }

    private void initViews() {
        tvTicketCode = findViewById(R.id.tvTicketCode);
        tvStudentInfo = findViewById(R.id.tvStudentInfo);
        tvDateInfo = findViewById(R.id.tvDateInfo);
        rvItemsToAssign = findViewById(R.id.rvItemsToAssign);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bàn giao thiết bị");
        }
    }

    private void setupRecyclerView() {
        rvItemsToAssign.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BorrowItemAdapter(true, new BorrowItemAdapter.OnAssignClickListener() {
            @Override
            public void onAssignClick(BorrowItem item, int deviceId) {
                showDeviceSelectionDialog(item, deviceId);
            }
        });
        rvItemsToAssign.setAdapter(adapter);
    }

    private void loadData() {
        borrowPresenter.loadTicketDetail(ticketId, this);
    }

    private void showDeviceSelectionDialog(BorrowItem item, int deviceId) {
        if (deviceId <= 0) {
            Toast.makeText(this, "Không xác định được loại thiết bị cần gán!", Toast.LENGTH_SHORT).show();
            return;
        }

        devicePresenter.loadAvailableDeviceDetails(deviceId, new DevicePresenter.DeviceDetailListView() {
            @Override
            public void onDeviceDetailsLoaded(List<DeviceDetail> details) {
                if (details == null || details.isEmpty()) {
                    Toast.makeText(AssignDeviceActivity.this, "Không có thiết bị vật lý nào rảnh cho loại này!", Toast.LENGTH_LONG).show();
                    return;
                }
                openAssignDialog(item, details);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AssignDeviceActivity.this, "Lỗi tải thiết bị: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openAssignDialog(BorrowItem item, List<DeviceDetail> availableDetails) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Gán thiết bị vật lý");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_assign_device, null);
        Spinner spinner = view.findViewById(R.id.spinnerDeviceDetail);
        EditText etConditionOut = view.findViewById(R.id.etConditionOut);
        EditText etAccessoriesOut = view.findViewById(R.id.etAccessoriesOut);

        // Tạo nhãn hiển thị cho Spinner
        List<String> displayStrings = new ArrayList<>();
        for (DeviceDetail dd : availableDetails) {
            displayStrings.add(dd.getAssetCode() + " (S/N: " + (dd.getSerialNumber() != null ? dd.getSerialNumber() : "N/A") 
                    + " — " + dd.getRoomLocation() + ")");
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, displayStrings);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        builder.setView(view);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            int selectedPos = spinner.getSelectedItemPosition();
            if (selectedPos < 0 || selectedPos >= availableDetails.size()) {
                Toast.makeText(this, "Chưa chọn thiết bị!", Toast.LENGTH_SHORT).show();
                return;
            }

            DeviceDetail selectedDetail = availableDetails.get(selectedPos);
            String condition = etConditionOut.getText().toString().trim();
            String accessories = etAccessoriesOut.getText().toString().trim();

            if (condition.isEmpty()) condition = "Tốt";
            if (accessories.isEmpty()) accessories = "Không";

            int adminId = sessionManager.getAccountId();
            borrowPresenter.assignDevice(
                    item.getId(),
                    selectedDetail.getId(),
                    condition,
                    accessories,
                    ticketId,
                    adminId,
                    AssignDeviceActivity.this
            );
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    @Override
    public void onTicketLoaded(BorrowTicket ticket, List<BorrowItem> items) {
        tvTicketCode.setText("Mã phiếu: " + ticket.getTicketCode());
        tvStudentInfo.setText("Sinh viên: " + ticket.getUserFullName() + " (" + ticket.getUserMssv() + ")");
        tvDateInfo.setText("Hạn trả dự kiến: " + ticket.getExpectedReturnDate() + " | Trạng thái: " + ticket.getStatus());
        adapter.setItems(items);
    }

    @Override
    public void onSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        loadData(); // Tải lại dữ liệu sau khi gán
        
        // Kiểm tra xem tất cả các item đã được gán chưa, nếu đã gán xong thì đóng màn hình
        // Vì list ở adapter đã được cập nhật hoặc sẽ được tải lại, chúng ta có thể kiểm tra ở lần onTicketLoaded tiếp theo
        // Hoặc kiểm tra xem message có chứa chuỗi chuyển trạng thái không
        if (message.contains("Đang mượn")) {
            finish();
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