package com.haui.devicemanagement.view.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.BorrowTicket;
import com.haui.devicemanagement.presenter.BorrowPresenter;
import com.haui.devicemanagement.util.SessionManager;
import com.haui.devicemanagement.util.ThemeHelper;
import com.haui.devicemanagement.view.adapter.BorrowTicketAdapter;

import java.util.List;

public class BorrowApprovalActivity extends AppCompatActivity 
        implements BorrowPresenter.BorrowListView, BorrowPresenter.BorrowActionView {

    private BorrowPresenter presenter;
    private SessionManager sessionManager;
    private BorrowTicketAdapter adapter;

    private RecyclerView rvPendingTickets;
    private LinearLayout layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_approval);
        ThemeHelper.applyDarkTheme(this);

        sessionManager = new SessionManager(this);
        presenter = new BorrowPresenter(DatabaseHelper.getInstance(this));

        initViews();
        setupToolbar();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews() {
        rvPendingTickets = findViewById(R.id.rvPendingTickets);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Duyệt phiếu mượn");
        }
    }

    private void setupRecyclerView() {
        rvPendingTickets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BorrowTicketAdapter(true, new BorrowTicketAdapter.OnTicketClickListener() {
            @Override
            public void onTicketClick(BorrowTicket ticket) {
                showApprovalDialog(ticket);
            }
        });
        rvPendingTickets.setAdapter(adapter);
    }

    private void loadData() {
        presenter.loadPendingTickets(this);
    }

    private void showApprovalDialog(BorrowTicket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xử lý yêu cầu mượn");
        builder.setMessage("Mã phiếu: " + ticket.getTicketCode() + "\n" +
                "Sinh viên: " + ticket.getUserFullName() + " (" + ticket.getUserMssv() + ")\n" +
                "Lý do: " + ticket.getBorrowReason() + "\n" +
                "Hạn trả: " + ticket.getExpectedReturnDate());

        builder.setPositiveButton("Duyệt", (dialog, which) -> {
            int adminId = sessionManager.getAccountId();
            presenter.approveTicket(ticket.getId(), adminId, "Duyệt yêu cầu", new BorrowPresenter.BorrowActionView() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(BorrowApprovalActivity.this, "Đã duyệt phiếu. Tiến hành gán thiết bị.", Toast.LENGTH_SHORT).show();
                    // Điều hướng ngay sang màn hình gán thiết bị
                    Intent intent = new Intent(BorrowApprovalActivity.this, AssignDeviceActivity.class);
                    intent.putExtra("ticket_id", ticket.getId());
                    startActivity(intent);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(BorrowApprovalActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Từ chối", (dialog, which) -> {
            showRejectReasonDialog(ticket);
        });

        builder.setNeutralButton("Hủy", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        ThemeHelper.applyDarkThemeToDialog(dialog);
    }

    private void showRejectReasonDialog(BorrowTicket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập lý do từ chối");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_input_text, null);
        final EditText input = viewInflated.findViewById(R.id.etDialogInput);
        input.setHint("Lý do từ chối...");
        builder.setView(viewInflated);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String note = input.getText().toString().trim();
            if (note.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập lý do từ chối!", Toast.LENGTH_SHORT).show();
                return;
            }
            int adminId = sessionManager.getAccountId();
            presenter.rejectTicket(ticket.getId(), adminId, note, this);
        });

        builder.setNegativeButton("Hủy", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        ThemeHelper.applyDarkThemeToDialog(dialog);
    }

    private void showRejectReasonDialogLayoutPlaceholder() {
        // Hàm này không cần thiết vì chúng ta sẽ tạo dialog_input_text.xml
    }

    @Override
    public void onTicketsLoaded(List<BorrowTicket> tickets) {
        adapter.setTickets(tickets);
        if (tickets == null || tickets.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvPendingTickets.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvPendingTickets.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        loadData(); // Tải lại danh sách
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