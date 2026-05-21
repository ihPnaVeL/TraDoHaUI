package com.haui.devicemanagement.view.admin;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.presenter.ReportPresenter;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ReportActivity extends AppCompatActivity implements ReportPresenter.ReportView {

    private TextView tvTotalBorrowed;
    private TextView tvTotalMaintenance;
    private TextView tvTotalLost;
    private TextView tvPendingBorrow;
    private TextView tvPendingReturn;
    private LinearLayout containerRows;

    private ReportPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        initViews();
        presenter = new ReportPresenter(DatabaseHelper.getInstance(this));
        presenter.loadReport(this);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Báo cáo thống kê");
        }

        tvTotalBorrowed = findViewById(R.id.tvTotalBorrowed);
        tvTotalMaintenance = findViewById(R.id.tvTotalMaintenance);
        tvTotalLost = findViewById(R.id.tvTotalLost);
        tvPendingBorrow = findViewById(R.id.tvPendingBorrow);
        tvPendingReturn = findViewById(R.id.tvPendingReturn);
        containerRows = findViewById(R.id.containerRows);
    }

    @Override
    public void onReportLoaded(Map<String, Integer> borrowByMonth, Map<String, Integer> returnByMonth,
                               int totalBorrowed, int totalMaintenance, int totalLost,
                               int pendingBorrow, int pendingReturn) {
        
        tvTotalBorrowed.setText(String.valueOf(totalBorrowed));
        tvTotalMaintenance.setText(String.valueOf(totalMaintenance));
        tvTotalLost.setText(String.valueOf(totalLost));
        tvPendingBorrow.setText(String.valueOf(pendingBorrow));
        tvPendingReturn.setText(String.valueOf(pendingReturn));

        containerRows.removeAllViews();

        Set<String> allMonths = new TreeSet<>(Collections.reverseOrder());
        allMonths.addAll(borrowByMonth.keySet());
        allMonths.addAll(returnByMonth.keySet());

        if (allMonths.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Không có dữ liệu thống kê theo tháng");
            tvEmpty.setPadding(0, 24, 0, 24);
            tvEmpty.setTextColor(getResources().getColor(R.color.text_secondary));
            tvEmpty.setTextSize(13);
            containerRows.addView(tvEmpty);
            return;
        }

        for (String month : allMonths) {
            int bCount = borrowByMonth.containsKey(month) ? borrowByMonth.get(month) : 0;
            int rCount = returnByMonth.containsKey(month) ? returnByMonth.get(month) : 0;
            addStatsRow(month, bCount, rCount);
        }
    }

    private void addStatsRow(String month, int borrowCount, int returnCount) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 16, 0, 16);
        row.setWeightSum(3);

        TextView tvMonth = new TextView(this);
        tvMonth.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        tvMonth.setText(month);
        tvMonth.setTextColor(getResources().getColor(R.color.text_primary));
        tvMonth.setTextSize(13);

        TextView tvBorrow = new TextView(this);
        tvBorrow.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        tvBorrow.setText(String.valueOf(borrowCount));
        tvBorrow.setGravity(android.view.Gravity.CENTER);
        tvBorrow.setTextColor(getResources().getColor(R.color.text_secondary));
        tvBorrow.setTextSize(13);

        TextView tvReturn = new TextView(this);
        tvReturn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        tvReturn.setText(String.valueOf(returnCount));
        tvReturn.setGravity(android.view.Gravity.END);
        tvReturn.setTextColor(getResources().getColor(R.color.text_secondary));
        tvReturn.setTextSize(13);

        row.addView(tvMonth);
        row.addView(tvBorrow);
        row.addView(tvReturn);

        containerRows.addView(row);

        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(getResources().getColor(R.color.divider));
        containerRows.addView(divider);
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, "Lỗi khi tải báo cáo: " + message, Toast.LENGTH_SHORT).show();
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