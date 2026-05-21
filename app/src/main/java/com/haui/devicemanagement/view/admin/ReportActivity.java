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
import com.haui.devicemanagement.util.ThemeHelper;

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

    // Fields to store data for export
    private int mTotalBorrowed;
    private int mTotalMaintenance;
    private int mTotalLost;
    private int mPendingBorrow;
    private int mPendingReturn;
    private Map<String, Integer> mBorrowByMonth = Collections.emptyMap();
    private Map<String, Integer> mReturnByMonth = Collections.emptyMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        initViews();
        presenter = new ReportPresenter(DatabaseHelper.getInstance(this));
        presenter.loadReport(this);

        ThemeHelper.applyDarkTheme(this);
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

        findViewById(R.id.btnExportExcel).setOnClickListener(v -> exportToExcel());
    }

    @Override
    public void onReportLoaded(Map<String, Integer> borrowByMonth, Map<String, Integer> returnByMonth,
                               int totalBorrowed, int totalMaintenance, int totalLost,
                               int pendingBorrow, int pendingReturn) {
        
        // Save values in memory for export
        this.mTotalBorrowed = totalBorrowed;
        this.mTotalMaintenance = totalMaintenance;
        this.mTotalLost = totalLost;
        this.mPendingBorrow = pendingBorrow;
        this.mPendingReturn = pendingReturn;
        this.mBorrowByMonth = borrowByMonth;
        this.mReturnByMonth = returnByMonth;

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

        ThemeHelper.applyDarkTheme(this);
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

    private void exportToExcel() {
        try {
            java.io.File dir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS);
            if (dir == null) {
                Toast.makeText(this, "Không thể truy cập thư mục lưu trữ", Toast.LENGTH_SHORT).show();
                return;
            }
            String fileName = "BaoCaoThongKe_" + System.currentTimeMillis() + ".csv";
            java.io.File file = new java.io.File(dir, fileName);

            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(fos, "UTF-8");

            // Write UTF-8 BOM (0xEF, 0xBB, 0xBF) to allow correct Vietnamese rendering in Excel
            osw.write('\ufeff');

            // Header info
            osw.write("BÁO CÁO THỐNG KÊ THIẾT BỊ VÀ PHIẾU\n");
            osw.write("Ngày xuất báo cáo," + com.haui.devicemanagement.util.DateUtils.getCurrentDate() + "\n\n");

            // General stats
            osw.write("TỔNG QUAN THIẾT BỊ VÀ PHIẾU\n");
            osw.write("Chỉ số,Số lượng\n");
            osw.write("Đang được mượn," + mTotalBorrowed + "\n");
            osw.write("Đang bảo trì," + mTotalMaintenance + "\n");
            osw.write("Bị mất," + mTotalLost + "\n");
            osw.write("Chờ duyệt mượn," + mPendingBorrow + "\n");
            osw.write("Chờ duyệt trả," + mPendingReturn + "\n\n");

            // Monthly stats
            osw.write("THỐNG KÊ HOẠT ĐỘNG THEO THÁNG\n");
            osw.write("Tháng,Số phiếu mượn,Số phiếu trả\n");

            Set<String> allMonths = new TreeSet<>(Collections.reverseOrder());
            allMonths.addAll(mBorrowByMonth.keySet());
            allMonths.addAll(mReturnByMonth.keySet());

            for (String month : allMonths) {
                int bCount = mBorrowByMonth.containsKey(month) ? mBorrowByMonth.get(month) : 0;
                int rCount = mReturnByMonth.containsKey(month) ? mReturnByMonth.get(month) : 0;
                osw.write(month + "," + bCount + "," + rCount + "\n");
            }

            osw.flush();
            osw.close();
            fos.close();

            Toast.makeText(this, "Xuất báo cáo thành công!\nĐường dẫn: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi xuất file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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