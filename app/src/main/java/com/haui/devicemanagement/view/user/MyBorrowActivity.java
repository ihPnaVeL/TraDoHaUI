package com.haui.devicemanagement.view.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.BorrowTicket;
import com.haui.devicemanagement.presenter.BorrowPresenter;
import com.haui.devicemanagement.util.SessionManager;
import com.haui.devicemanagement.view.adapter.BorrowTicketAdapter;

import java.util.List;

public class MyBorrowActivity extends AppCompatActivity implements BorrowPresenter.BorrowListView {

    private RecyclerView rvMyBorrows;
    private LinearLayout layoutEmpty;
    private BorrowPresenter presenter;
    private SessionManager sessionManager;
    private BorrowTicketAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_borrow);

        sessionManager = new SessionManager(this);
        presenter = new BorrowPresenter(DatabaseHelper.getInstance(this));

        initViews();
        setupToolbar();
        setupRecyclerView();
    }

    private void initViews() {
        rvMyBorrows = findViewById(R.id.rvMyBorrows);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Lịch sử mượn");
        }
    }

    private void setupRecyclerView() {
        rvMyBorrows.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BorrowTicketAdapter(false, ticket -> {
            Intent intent = new Intent(MyBorrowActivity.this, TicketDetailActivity.class);
            intent.putExtra("TICKET_TYPE", "borrow");
            intent.putExtra("TICKET_ID", ticket.getId());
            startActivity(intent);
        });
        rvMyBorrows.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        int userId = sessionManager.getAccountId();
        presenter.loadUserTickets(userId, this);
    }

    @Override
    public void onTicketsLoaded(List<BorrowTicket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvMyBorrows.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvMyBorrows.setVisibility(View.VISIBLE);
        }
        adapter.setTickets(tickets);
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
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