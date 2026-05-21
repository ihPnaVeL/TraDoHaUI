package com.haui.devicemanagement.view.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.ReturnItem;
import com.haui.devicemanagement.data.entity.ReturnTicket;
import com.haui.devicemanagement.presenter.ReturnPresenter;
import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.DateUtils;
import com.haui.devicemanagement.util.SessionManager;
import com.haui.devicemanagement.util.ThemeHelper;
import com.haui.devicemanagement.view.adapter.ReturnItemAdapter;

import java.util.List;

public class ReturnDetailCheckActivity extends AppCompatActivity 
        implements ReturnPresenter.ReturnDetailView {

    private TextView tvReturnTicketCode;
    private TextView tvStudentInfo;
    private TextView tvDateInfo;
    private TextView tvReturnNote;
    private RecyclerView rvItemsToCheck;
    private MaterialButton btnConfirmReturn;

    private ReturnPresenter presenter;
    private SessionManager sessionManager;
    private ReturnItemAdapter adapter;
    private int returnTicketId = -1;
    private boolean isPending = true;
    private boolean allChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_detail_check);
        ThemeHelper.applyDarkTheme(this);

        sessionManager = new SessionManager(this);
        presenter = new ReturnPresenter(DatabaseHelper.getInstance(this));

        returnTicketId = getIntent().getIntExtra("RETURN_TICKET_ID", -1);
        if (returnTicketId == -1) {
            Toast.makeText(this, "Không tìm thấy mã phiếu trả", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
    }

    private void initViews() {
        tvReturnTicketCode = findViewById(R.id.tvReturnTicketCode);
        tvStudentInfo = findViewById(R.id.tvStudentInfo);
        tvDateInfo = findViewById(R.id.tvDateInfo);
        tvReturnNote = findViewById(R.id.tvReturnNote);
        rvItemsToCheck = findViewById(R.id.rvItemsToCheck);
        btnConfirmReturn = findViewById(R.id.btnConfirmReturn);

        btnConfirmReturn.setOnClickListener(v -> {
            if (!isPending || allChecked) {
                finish();
            } else {
                Toast.makeText(ReturnDetailCheckActivity.this, 
                        "Vui lòng kiểm tra và xác nhận tất cả thiết bị trước", 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Kiểm duyệt chi tiết");
        }
    }

    private void setupRecyclerView() {
        rvItemsToCheck.setLayoutManager(new LinearLayoutManager(this));
        // adapter will be re-initialized / set in onTicketLoaded based on whether it is pending or not
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        presenter.loadReturnDetail(returnTicketId, this);
    }

    @Override
    public void onTicketLoaded(ReturnTicket ticket, List<ReturnItem> items) {
        isPending = Constants.RETURN_PENDING.equals(ticket.getStatus());

        tvReturnTicketCode.setText("Mã phiếu trả: " + ticket.getTicketCode());
        
        String studentText = "Sinh viên: " + 
                (ticket.getUserFullName() != null ? ticket.getUserFullName() : "N/A") + 
                " (" + (ticket.getUserMssv() != null ? ticket.getUserMssv() : ticket.getUserId()) + ")";
        tvStudentInfo.setText(studentText);

        tvDateInfo.setText("Ngày gửi trả: " + DateUtils.formatDisplayDateTime(ticket.getReturnedAt()));
        
        String noteText = "Ghi chú sv: " + 
                (ticket.getNote() != null && !ticket.getNote().isEmpty() ? ticket.getNote() : "Không");
        tvReturnNote.setText(noteText);

        // Check if all items are completed
        allChecked = true;
        for (ReturnItem ri : items) {
            if (ri.getIsCompleted() == 0) {
                allChecked = false;
                break;
            }
        }

        if (!isPending) {
            btnConfirmReturn.setText("Quay lại");
            btnConfirmReturn.setEnabled(true);
        } else {
            if (allChecked) {
                btnConfirmReturn.setText("Hoàn tất kiểm duyệt");
                btnConfirmReturn.setEnabled(true);
            } else {
                btnConfirmReturn.setText("Hoàn tất kiểm duyệt");
                // Keep it enabled so admin can click to see the validation toast if needed,
                // or we can just handle the message on click.
                btnConfirmReturn.setEnabled(true);
            }
        }

        // Set adapter
        adapter = new ReturnItemAdapter(isPending, item -> showCheckDialog(item));
        adapter.setItems(items);
        rvItemsToCheck.setAdapter(adapter);
    }

    private void showCheckDialog(ReturnItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kiểm tra thiết bị");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_check_item, null);
        builder.setView(view);

        RadioGroup rgConditionIn = view.findViewById(R.id.rgConditionIn);
        RadioButton rbGood = view.findViewById(R.id.rbGood);
        RadioButton rbDamaged = view.findViewById(R.id.rbDamaged);
        RadioButton rbLost = view.findViewById(R.id.rbLost);
        EditText etAccessoriesIn = view.findViewById(R.id.etAccessoriesIn);
        EditText etDamageNote = view.findViewById(R.id.etDamageNote);
        EditText etPenaltyAmount = view.findViewById(R.id.etPenaltyAmount);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String condition = Constants.CONDITION_GOOD;
            if (rbDamaged.isChecked()) {
                condition = Constants.CONDITION_DAMAGED;
            } else if (rbLost.isChecked()) {
                condition = "lost";
            }

            String accessories = etAccessoriesIn.getText().toString().trim();
            if (accessories.isEmpty()) {
                accessories = "Đầy đủ";
            }

            String damageNote = etDamageNote.getText().toString().trim();
            
            String penaltyStr = etPenaltyAmount.getText().toString().trim();
            int penaltyAmount = 0;
            if (!penaltyStr.isEmpty()) {
                try {
                    penaltyAmount = Integer.parseInt(penaltyStr);
                } catch (NumberFormatException e) {
                    penaltyAmount = 0;
                }
            }

            int adminId = sessionManager.getAccountId();

            presenter.confirmReturnItem(item.getId(), returnTicketId, condition, accessories, 
                    damageNote, penaltyAmount, adminId, new ReturnPresenter.ReturnActionView() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(ReturnDetailCheckActivity.this, message, Toast.LENGTH_SHORT).show();
                    loadData(); // reload lists and ticket info
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(ReturnDetailCheckActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Hủy", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        ThemeHelper.applyDarkThemeToDialog(dialog);
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, "Lỗi tải thông tin: " + message, Toast.LENGTH_SHORT).show();
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
