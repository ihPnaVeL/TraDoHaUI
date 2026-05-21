package com.haui.devicemanagement.view.user;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.BorrowItem;
import com.haui.devicemanagement.data.entity.BorrowTicket;
import com.haui.devicemanagement.data.entity.ReturnItem;
import com.haui.devicemanagement.data.entity.ReturnTicket;
import com.haui.devicemanagement.presenter.BorrowPresenter;
import com.haui.devicemanagement.presenter.ReturnPresenter;
import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.DateUtils;
import com.haui.devicemanagement.view.adapter.BorrowItemAdapter;
import com.haui.devicemanagement.view.adapter.ReturnItemAdapter;
import com.haui.devicemanagement.util.SessionManager;

import java.util.List;

public class TicketDetailActivity extends AppCompatActivity {

    private TextView tvTicketCode;
    private TextView tvStatusBadge;
    private TextView tvStudentName;
    private TextView tvDateCreated;
    private TextView tvDateTargetLabel;
    private TextView tvDateTargetValue;
    private LinearLayout layoutHandler;
    private TextView tvHandlerLabel;
    private TextView tvHandlerValue;
    private TextView tvNoteLabel;
    private TextView tvNoteValue;
    private RecyclerView rvTicketItems;

    private String ticketType;
    private int ticketId;

    private BorrowPresenter borrowPresenter;
    private ReturnPresenter returnPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        ticketType = getIntent().getStringExtra("TICKET_TYPE");
        ticketId = getIntent().getIntExtra("TICKET_ID", -1);

        if (ticketType == null || ticketId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin phiếu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        borrowPresenter = new BorrowPresenter(dbHelper);
        returnPresenter = new ReturnPresenter(dbHelper);

        initViews();
        setupToolbar();
        setupRecyclerView();
        
        applyDarkTheme();
        
        loadData();
    }

    private void initViews() {
        tvTicketCode = findViewById(R.id.tvTicketCode);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvDateCreated = findViewById(R.id.tvDateCreated);
        tvDateTargetLabel = findViewById(R.id.tvDateTargetLabel);
        tvDateTargetValue = findViewById(R.id.tvDateTargetValue);
        layoutHandler = findViewById(R.id.layoutHandler);
        tvHandlerLabel = findViewById(R.id.tvHandlerLabel);
        tvHandlerValue = findViewById(R.id.tvHandlerValue);
        tvNoteLabel = findViewById(R.id.tvNoteLabel);
        tvNoteValue = findViewById(R.id.tvNoteValue);
        rvTicketItems = findViewById(R.id.rvTicketItems);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết phiếu");
        }
    }

    private void setupRecyclerView() {
        rvTicketItems.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadData() {
        if ("borrow".equalsIgnoreCase(ticketType)) {
            borrowPresenter.loadTicketDetail(ticketId, new BorrowPresenter.BorrowDetailView() {
                @Override
                public void onTicketLoaded(BorrowTicket ticket, List<BorrowItem> items) {
                    bindBorrowTicket(ticket, items);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(TicketDetailActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } else if ("return".equalsIgnoreCase(ticketType)) {
            returnPresenter.loadReturnDetail(ticketId, new ReturnPresenter.ReturnDetailView() {
                @Override
                public void onTicketLoaded(ReturnTicket ticket, List<ReturnItem> items) {
                    bindReturnTicket(ticket, items);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(TicketDetailActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void bindBorrowTicket(BorrowTicket ticket, List<BorrowItem> items) {
        tvTicketCode.setText("Mã phiếu: " + ticket.getTicketCode());
        
        // Status badge setup
        bindBorrowStatus(ticket.getStatus());

        String studentText = (ticket.getUserFullName() != null ? ticket.getUserFullName() : "N/A") + 
                " (" + (ticket.getUserMssv() != null ? ticket.getUserMssv() : ticket.getUserId()) + ")";
        tvStudentName.setText(studentText);

        tvDateCreated.setText(DateUtils.formatDisplayDateTime(ticket.getCreatedAt()));
        
        tvDateTargetLabel.setText("Hạn trả dự kiến: ");
        tvDateTargetValue.setText(DateUtils.formatDisplayDate(ticket.getExpectedReturnDate()));

        if (ticket.getApprovedBy() > 0) {
            layoutHandler.setVisibility(View.VISIBLE);
            tvHandlerLabel.setText("Người duyệt: ");
            tvHandlerValue.setText(ticket.getApprovedByName() != null ? ticket.getApprovedByName() : "Admin #" + ticket.getApprovedBy());
        } else {
            layoutHandler.setVisibility(View.GONE);
        }

        tvNoteLabel.setText("Lý do mượn / Ghi chú admin:");
        String note = "Lý do: " + (ticket.getBorrowReason() != null ? ticket.getBorrowReason() : "N/A");
        if (ticket.getAdminNote() != null && !ticket.getAdminNote().isEmpty()) {
            note += "\nAdmin note: " + ticket.getAdminNote();
        }
        tvNoteValue.setText(note);

        BorrowItemAdapter borrowItemAdapter = new BorrowItemAdapter(false, null);
        borrowItemAdapter.setItems(items);
        rvTicketItems.setAdapter(borrowItemAdapter);
    }

    private void bindReturnTicket(ReturnTicket ticket, List<ReturnItem> items) {
        tvTicketCode.setText("Mã phiếu: " + ticket.getTicketCode());

        // Status badge setup
        bindReturnStatus(ticket.getStatus());

        String studentText = (ticket.getUserFullName() != null ? ticket.getUserFullName() : "N/A") + 
                " (" + (ticket.getUserMssv() != null ? ticket.getUserMssv() : ticket.getUserId()) + ")";
        tvStudentName.setText(studentText);

        tvDateCreated.setText(DateUtils.formatDisplayDateTime(ticket.getReturnedAt()));

        tvDateTargetLabel.setText("Ngày xác nhận: ");
        if (ticket.getConfirmedAt() != null) {
            tvDateTargetValue.setText(DateUtils.formatDisplayDateTime(ticket.getConfirmedAt()));
        } else {
            tvDateTargetValue.setText("Chưa xác nhận");
        }

        if (ticket.getConfirmedBy() > 0) {
            layoutHandler.setVisibility(View.VISIBLE);
            tvHandlerLabel.setText("Người xác nhận: ");
            tvHandlerValue.setText(ticket.getConfirmedByName() != null ? ticket.getConfirmedByName() : "Admin #" + ticket.getConfirmedBy());
        } else {
            layoutHandler.setVisibility(View.GONE);
        }

        tvNoteLabel.setText("Ghi chú trả:");
        tvNoteValue.setText(ticket.getNote() != null && !ticket.getNote().isEmpty() ? ticket.getNote() : "Không");

        ReturnItemAdapter returnItemAdapter = new ReturnItemAdapter(false, null);
        returnItemAdapter.setItems(items);
        rvTicketItems.setAdapter(returnItemAdapter);
    }

    private void bindBorrowStatus(String status) {
        int colorRes;
        String text;

        switch (status) {
            case Constants.BORROW_PENDING:
                colorRes = R.color.status_pending;
                text = "Chờ duyệt";
                break;
            case Constants.BORROW_APPROVED:
                colorRes = R.color.status_approved;
                text = "Đã duyệt";
                break;
            case Constants.BORROW_BORROWED:
                colorRes = R.color.status_borrowed;
                text = "Đang mượn";
                break;
            case Constants.BORROW_RETURNED:
                colorRes = R.color.status_returned;
                text = "Đã trả";
                break;
            case Constants.BORROW_REJECTED:
                colorRes = R.color.status_rejected;
                text = "Từ chối";
                break;
            case Constants.BORROW_OVERDUE:
                colorRes = R.color.status_overdue;
                text = "Quá hạn";
                break;
            case Constants.BORROW_PARTIALLY_RETURNED:
                colorRes = R.color.status_pending;
                text = "Trả một phần";
                break;
            default:
                colorRes = R.color.grey;
                text = status;
        }

        int color = ContextCompat.getColor(this, colorRes);
        tvStatusBadge.setText(text);
        tvStatusBadge.setTextColor(color);
        tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private void bindReturnStatus(String status) {
        int colorRes;
        String text;

        switch (status) {
            case Constants.RETURN_PENDING:
                colorRes = R.color.status_pending;
                text = "Chờ kiểm tra";
                break;
            case Constants.RETURN_COMPLETED:
                colorRes = R.color.status_returned;
                text = "Hoàn tất trả";
                break;
            case Constants.RETURN_DAMAGED:
                colorRes = R.color.status_overdue; // Cam/Đỏ
                text = "Có thiết bị hỏng";
                break;
            case Constants.RETURN_LOST:
                colorRes = R.color.status_rejected; // Đỏ sẫm
                text = "Mất thiết bị";
                break;
            default:
                colorRes = R.color.grey;
                text = status;
        }

        int color = ContextCompat.getColor(this, colorRes);
        tvStatusBadge.setText(text);
        tvStatusBadge.setTextColor(color);
        tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void applyDarkTheme() {
        View root = findViewById(R.id.rootLayout);
        if (root != null) {
            root.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
        }
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(android.graphics.Color.parseColor("#161616"));
            if (toolbar instanceof Toolbar && ((Toolbar) toolbar).getNavigationIcon() != null) {
                androidx.core.graphics.drawable.DrawableCompat.setTint(
                    androidx.core.graphics.drawable.DrawableCompat.wrap(((Toolbar) toolbar).getNavigationIcon()), 
                    android.graphics.Color.WHITE
                );
            }
        }
        androidx.cardview.widget.CardView cardGeneralInfo = findViewById(R.id.cardGeneralInfo);
        if (cardGeneralInfo != null) {
            cardGeneralInfo.setCardBackgroundColor(android.graphics.Color.parseColor("#1C1C1E"));
            applyDarkThemeToViewGroup(cardGeneralInfo);
        }
        View divider = findViewById(R.id.divider);
        if (divider != null) {
            divider.setBackgroundColor(android.graphics.Color.parseColor("#2C2C2E"));
        }
        TextView tvTitleDeviceList = findViewById(R.id.tvTitleDeviceList);
        if (tvTitleDeviceList != null) {
            tvTitleDeviceList.setTextColor(android.graphics.Color.parseColor("#B0B0B0"));
        }
    }

    private void applyDarkThemeToViewGroup(android.view.ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            android.view.View child = viewGroup.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                int id = tv.getId();
                if (id == R.id.tvTicketCode || id == R.id.tvStudentName || id == R.id.tvDateTargetValue || id == R.id.tvDateCreated || id == R.id.tvHandlerValue || id == R.id.tvNoteValue) {
                    tv.setTextColor(android.graphics.Color.WHITE);
                } else if (id == R.id.tvStatusBadge) {
                    // Trạng thái đã được tô màu riêng ở bindBorrowStatus/bindReturnStatus
                } else {
                    tv.setTextColor(android.graphics.Color.parseColor("#B0B0B0"));
                }
            } else if (child instanceof android.view.ViewGroup) {
                applyDarkThemeToViewGroup((android.view.ViewGroup) child);
            }
        }
    }
}