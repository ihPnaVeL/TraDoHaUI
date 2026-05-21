package com.haui.devicemanagement.view.admin;

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
import com.haui.devicemanagement.util.ThemeHelper;
import com.haui.devicemanagement.view.adapter.ReturnTicketAdapter;

import java.util.List;

public class ReturnApprovalActivity extends AppCompatActivity {

    private RecyclerView rvPendingReturns;
    private LinearLayout layoutEmpty;
    private ReturnPresenter presenter;
    private ReturnTicketAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_approval);
        ThemeHelper.applyDarkTheme(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Duyệt yêu cầu trả");
        }

        rvPendingReturns = findViewById(R.id.rvPendingReturns);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        presenter = new ReturnPresenter(DatabaseHelper.getInstance(this));
        
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        rvPendingReturns.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReturnTicketAdapter(true, ticket -> {
            Intent intent = new Intent(ReturnApprovalActivity.this, ReturnDetailCheckActivity.class);
            intent.putExtra("RETURN_TICKET_ID", ticket.getId());
            startActivity(intent);
        });
        rvPendingReturns.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingReturns();
    }

    private void loadPendingReturns() {
        presenter.loadPendingReturns(new ReturnPresenter.ReturnListView() {
            @Override
            public void onTicketsLoaded(List<ReturnTicket> tickets) {
                if (tickets.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    rvPendingReturns.setVisibility(View.GONE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    rvPendingReturns.setVisibility(View.VISIBLE);
                }
                adapter.setTickets(tickets);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ReturnApprovalActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
            }
        });
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