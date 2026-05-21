package com.haui.devicemanagement.view.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.dao.BorrowTicketDao;
import com.haui.devicemanagement.data.dao.NotificationDao;
import com.haui.devicemanagement.data.entity.BorrowTicket;
import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.DateUtils;
import com.haui.devicemanagement.util.ThemeHelper;
import com.haui.devicemanagement.view.adapter.BorrowTicketAdapter;
import com.haui.devicemanagement.view.user.TicketDetailActivity;

import java.util.List;

public class OverdueActivity extends AppCompatActivity implements BorrowTicketAdapter.OnTicketClickListener {

    private BorrowTicketDao borrowTicketDao;
    private NotificationDao notificationDao;
    private BorrowTicketAdapter adapter;
    private RecyclerView rvOverdueTickets;
    private MaterialButton btnScan;
    private MaterialButton btnNotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overdue);
        ThemeHelper.applyDarkTheme(this);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        borrowTicketDao = new BorrowTicketDao(dbHelper);
        notificationDao = new NotificationDao(dbHelper);

        initViews();
        setupRecyclerView();
        setupListeners();
        loadOverdueTickets();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Phiếu mượn quá hạn");
        }

        rvOverdueTickets = findViewById(R.id.rvOverdueTickets);
        btnScan = findViewById(R.id.btnScan);
        btnNotify = findViewById(R.id.btnNotify);
    }

    private void setupRecyclerView() {
        rvOverdueTickets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BorrowTicketAdapter(true, this);
        rvOverdueTickets.setAdapter(adapter);
    }

    private void setupListeners() {
        btnScan.setOnClickListener(v -> scanOverdueTickets());
        btnNotify.setOnClickListener(v -> notifyAllOverdue());
    }

    private void loadOverdueTickets() {
        String today = DateUtils.getCurrentDate();
        List<BorrowTicket> overdueList = borrowTicketDao.getOverdueTickets(today);
        adapter.setTickets(overdueList);
    }

    private void scanOverdueTickets() {
        String today = DateUtils.getCurrentDate();
        List<BorrowTicket> allPotentialOverdue = borrowTicketDao.getOverdueTickets(today);
        int updatedCount = 0;

        for (BorrowTicket ticket : allPotentialOverdue) {
            if (Constants.BORROW_BORROWED.equals(ticket.getStatus())) {
                borrowTicketDao.updateStatus(ticket.getId(), Constants.BORROW_OVERDUE);
                updatedCount++;
            }
        }

        Toast.makeText(this, "Đã quét và cập nhật trạng thái quá hạn cho " + updatedCount + " phiếu mượn!", Toast.LENGTH_SHORT).show();
        loadOverdueTickets();
    }

    private void notifyAllOverdue() {
        String today = DateUtils.getCurrentDate();
        List<BorrowTicket> overdueList = borrowTicketDao.getOverdueTickets(today);

        if (overdueList.isEmpty()) {
            Toast.makeText(this, "Không có phiếu mượn quá hạn nào cần nhắc nhở", Toast.LENGTH_SHORT).show();
            return;
        }

        int notifyCount = 0;
        for (BorrowTicket ticket : overdueList) {
            notificationDao.sendNotification(
                    Constants.ACCOUNT_USER,
                    ticket.getUserId(),
                    Constants.NOTIF_OVERDUE,
                    "Thiết bị mượn quá hạn!",
                    "Thiết bị thuộc phiếu mượn " + ticket.getTicketCode() + " đã quá hạn trả (" + DateUtils.formatDisplayDate(ticket.getExpectedReturnDate()) + "). Vui lòng trả thiết bị ngay.",
                    ticket.getId(),
                    Constants.REF_BORROW
            );
            notifyCount++;
        }

        Toast.makeText(this, "Đã gửi thông báo nhắc nhở đến " + notifyCount + " sinh viên!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTicketClick(BorrowTicket ticket) {
        Intent intent = new Intent(this, TicketDetailActivity.class);
        intent.putExtra("TICKET_TYPE", "borrow");
        intent.putExtra("TICKET_ID", ticket.getId());
        startActivity(intent);
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