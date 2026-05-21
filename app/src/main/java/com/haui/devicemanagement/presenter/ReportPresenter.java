package com.haui.devicemanagement.presenter;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.dao.BorrowTicketDao;
import com.haui.devicemanagement.data.dao.DeviceDetailDao;
import com.haui.devicemanagement.data.dao.ReturnTicketDao;
import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.DateUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ReportPresenter — thống kê báo cáo.
 */
public class ReportPresenter {

    public interface ReportView {
        void onReportLoaded(Map<String, Integer> borrowByMonth,
                            Map<String, Integer> returnByMonth,
                            int totalBorrowed, int totalMaintenance,
                            int totalLost, int pendingBorrow, int pendingReturn);
        void onError(String message);
    }

    private final DatabaseHelper  dbHelper;
    private final BorrowTicketDao borrowTicketDao;
    private final ReturnTicketDao returnTicketDao;
    private final DeviceDetailDao deviceDetailDao;

    public ReportPresenter(DatabaseHelper dbHelper) {
        this.dbHelper        = dbHelper;
        this.borrowTicketDao = new BorrowTicketDao(dbHelper);
        this.returnTicketDao = new ReturnTicketDao(dbHelper);
        this.deviceDetailDao = new DeviceDetailDao(dbHelper);
    }

    public void loadReport(ReportView view) {
        Map<String, Integer> borrowByMonth = getMonthlyStats(
            DatabaseHelper.TABLE_BORROW_TICKETS, "created_at");
        Map<String, Integer> returnByMonth = getMonthlyStats(
            DatabaseHelper.TABLE_RETURN_TICKETS, "returned_at");

        int totalBorrowed    = deviceDetailDao.countByAvailabilityStatus(Constants.DEVICE_BORROWED);
        int totalMaintenance = deviceDetailDao.countByAvailabilityStatus(Constants.DEVICE_MAINTENANCE);
        int totalLost        = deviceDetailDao.countByAvailabilityStatus(Constants.DEVICE_LOST);
        int pendingBorrow    = borrowTicketDao.countByStatus(Constants.BORROW_PENDING);
        int pendingReturn    = returnTicketDao.countByStatus(Constants.RETURN_PENDING);

        view.onReportLoaded(borrowByMonth, returnByMonth,
                totalBorrowed, totalMaintenance, totalLost,
                pendingBorrow, pendingReturn);
    }

    private Map<String, Integer> getMonthlyStats(String table, String dateCol) {
        Map<String, Integer> map = new LinkedHashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT substr(" + dateCol + ", 1, 7) AS month, COUNT(*) AS cnt " +
                     "FROM " + table + " " +
                     "GROUP BY month ORDER BY month DESC LIMIT 6";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            String month = cursor.getString(0);
            int    cnt   = cursor.getInt(1);
            if (month != null) map.put(month, cnt);
        }
        cursor.close();
        return map;
    }
}
