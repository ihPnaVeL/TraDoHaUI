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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.BorrowTicket;
import com.haui.devicemanagement.data.entity.ReturnTicket;
import com.haui.devicemanagement.presenter.BorrowPresenter;
import com.haui.devicemanagement.presenter.ReturnPresenter;
import com.haui.devicemanagement.util.SessionManager;
import com.haui.devicemanagement.view.adapter.BorrowTicketAdapter;
import com.haui.devicemanagement.view.adapter.ReturnTicketAdapter;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView rvHistory;
    private LinearLayout layoutEmpty;
    private BottomNavigationView bottomNavigation;

    private BorrowPresenter borrowPresenter;
    private ReturnPresenter returnPresenter;
    private SessionManager sessionManager;

    private BorrowTicketAdapter borrowAdapter;
    private ReturnTicketAdapter returnAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        sessionManager = new SessionManager(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        borrowPresenter = new BorrowPresenter(dbHelper);
        returnPresenter = new ReturnPresenter(dbHelper);

        initViews();
        setupToolbar();
        setupRecyclerViews();
        setupTabLayout();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        rvHistory = findViewById(R.id.rvHistory);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        setupBottomNavigation();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Lịch sử hoạt động");
        }
        if (toolbar != null && toolbar.getNavigationIcon() != null) {
            androidx.core.graphics.drawable.DrawableCompat.setTint(
                androidx.core.graphics.drawable.DrawableCompat.wrap(toolbar.getNavigationIcon()), 
                android.graphics.Color.WHITE
            );
        }
    }

    private void setupRecyclerViews() {
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        borrowAdapter = new BorrowTicketAdapter(false, ticket -> {
            Intent intent = new Intent(HistoryActivity.this, TicketDetailActivity.class);
            intent.putExtra("TICKET_TYPE", "borrow");
            intent.putExtra("TICKET_ID", ticket.getId());
            startActivity(intent);
        });

        returnAdapter = new ReturnTicketAdapter(false, ticket -> {
            Intent intent = new Intent(HistoryActivity.this, TicketDetailActivity.class);
            intent.putExtra("TICKET_TYPE", "return");
            intent.putExtra("TICKET_ID", ticket.getId());
            startActivity(intent);
        });
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Lịch sử mượn"));
        tabLayout.addTab(tabLayout.newTab().setText("Lịch sử trả"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadDataForTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_history) {
                return true;
            }

            Intent intent = null;
            if (itemId == R.id.nav_home) {
                intent = new Intent(this, UserHomeActivity.class);
            } else if (itemId == R.id.nav_search) {
                intent = new Intent(this, DeviceSearchActivity.class);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_history);
        }
        loadDataForTab(tabLayout.getSelectedTabPosition());
    }

    private void loadDataForTab(int position) {
        int userId = sessionManager.getAccountId();
        if (position == 0) {
            rvHistory.setAdapter(borrowAdapter);
            borrowPresenter.loadUserTickets(userId, borrowListView);
        } else {
            rvHistory.setAdapter(returnAdapter);
            returnPresenter.loadUserReturnTickets(userId, returnListView);
        }
    }

    private final BorrowPresenter.BorrowListView borrowListView = new BorrowPresenter.BorrowListView() {
        @Override
        public void onTicketsLoaded(List<BorrowTicket> tickets) {
            if (tickets == null || tickets.isEmpty()) {
                layoutEmpty.setVisibility(View.VISIBLE);
                rvHistory.setVisibility(View.GONE);
            } else {
                layoutEmpty.setVisibility(View.GONE);
                rvHistory.setVisibility(View.VISIBLE);
            }
            borrowAdapter.setTickets(tickets);
        }

        @Override
        public void onError(String message) {
            Toast.makeText(HistoryActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
        }
    };

    private final ReturnPresenter.ReturnListView returnListView = new ReturnPresenter.ReturnListView() {
        @Override
        public void onTicketsLoaded(List<ReturnTicket> tickets) {
            if (tickets == null || tickets.isEmpty()) {
                layoutEmpty.setVisibility(View.VISIBLE);
                rvHistory.setVisibility(View.GONE);
            } else {
                layoutEmpty.setVisibility(View.GONE);
                rvHistory.setVisibility(View.VISIBLE);
            }
            returnAdapter.setTickets(tickets);
        }

        @Override
        public void onError(String message) {
            Toast.makeText(HistoryActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
