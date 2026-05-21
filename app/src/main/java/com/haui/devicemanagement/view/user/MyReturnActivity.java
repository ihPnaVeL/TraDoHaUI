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
import com.haui.devicemanagement.data.entity.ReturnTicket;
import com.haui.devicemanagement.presenter.ReturnPresenter;
import com.haui.devicemanagement.util.SessionManager;
import com.haui.devicemanagement.view.adapter.ReturnTicketAdapter;

import java.util.List;

public class MyReturnActivity extends AppCompatActivity implements ReturnPresenter.ReturnListView {

    private RecyclerView rvMyReturns;
    private LinearLayout layoutEmpty;
    private ReturnPresenter presenter;
    private SessionManager sessionManager;
    private ReturnTicketAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_return);

        sessionManager = new SessionManager(this);
        presenter = new ReturnPresenter(DatabaseHelper.getInstance(this));

        initViews();
        setupToolbar();
        setupRecyclerView();
    }

    private void initViews() {
        rvMyReturns = findViewById(R.id.rvMyReturns);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Lịch sử trả");
        }
    }

    private void setupRecyclerView() {
        rvMyReturns.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReturnTicketAdapter(false, ticket -> {
            Intent intent = new Intent(MyReturnActivity.this, TicketDetailActivity.class);
            intent.putExtra("TICKET_TYPE", "return");
            intent.putExtra("TICKET_ID", ticket.getId());
            startActivity(intent);
        });
        rvMyReturns.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        int userId = sessionManager.getAccountId();
        presenter.loadUserReturnTickets(userId, this);
    }

    @Override
    public void onTicketsLoaded(List<ReturnTicket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvMyReturns.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvMyReturns.setVisibility(View.VISIBLE);
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