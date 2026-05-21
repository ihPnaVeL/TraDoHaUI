package com.haui.devicemanagement.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * DateUtils — tiện ích xử lý ngày giờ.
 */
public class DateUtils {

    private DateUtils() {}

    private static final String FORMAT_DATE     = "yyyy-MM-dd";
    private static final String FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";
    private static final String FORMAT_DISPLAY  = "dd/MM/yyyy";
    private static final String FORMAT_DISPLAY_DATETIME = "dd/MM/yyyy HH:mm";

    // ─── GET CURRENT ───────────────────────────────────────────────────────────

    public static String getCurrentDate() {
        return new SimpleDateFormat(FORMAT_DATE, Locale.getDefault()).format(new Date());
    }

    public static String getCurrentDateTime() {
        return new SimpleDateFormat(FORMAT_DATETIME, Locale.getDefault()).format(new Date());
    }

    // ─── FORMAT FOR DISPLAY ────────────────────────────────────────────────────

    /**
     * Chuyển "yyyy-MM-dd" sang "dd/MM/yyyy" để hiển thị.
     */
    public static String formatDateDisplay(String dbDate) {
        if (dbDate == null || dbDate.isEmpty()) return "";
        try {
            Date date = new SimpleDateFormat(FORMAT_DATE, Locale.getDefault()).parse(dbDate);
            return new SimpleDateFormat(FORMAT_DISPLAY, Locale.getDefault()).format(date);
        } catch (ParseException e) {
            return dbDate;
        }
    }

    /**
     * Chuyển "yyyy-MM-dd HH:mm:ss" sang "dd/MM/yyyy HH:mm" để hiển thị.
     */
    public static String formatDateTimeDisplay(String dbDateTime) {
        if (dbDateTime == null || dbDateTime.isEmpty()) return "";
        try {
            Date date = new SimpleDateFormat(FORMAT_DATETIME, Locale.getDefault()).parse(dbDateTime);
            return new SimpleDateFormat(FORMAT_DISPLAY_DATETIME, Locale.getDefault()).format(date);
        } catch (ParseException e) {
            return dbDateTime;
        }
    }

    public static String formatDisplayDate(String dbDate) {
        return formatDateDisplay(dbDate);
    }

    public static String formatDisplayDateTime(String dbDateTime) {
        return formatDateTimeDisplay(dbDateTime);
    }

    // ─── COMPARE ───────────────────────────────────────────────────────────────

    /**
     * Kiểm tra ngày có quá hạn so với hôm nay không.
     * @param dbDate dạng "yyyy-MM-dd"
     * @return true nếu dbDate < today
     */
    public static boolean isOverdue(String dbDate) {
        if (dbDate == null || dbDate.isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE, Locale.getDefault());
            Date date = sdf.parse(dbDate);
            Date today = sdf.parse(getCurrentDate());
            return date != null && today != null && date.before(today);
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Số ngày còn lại đến hạn trả (âm nếu quá hạn).
     */
    public static int daysUntilDue(String dbDate) {
        if (dbDate == null || dbDate.isEmpty()) return 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE, Locale.getDefault());
            Date due = sdf.parse(dbDate);
            Date today = new Date();
            if (due == null) return 0;
            long diff = due.getTime() - today.getTime();
            return (int) (diff / (1000 * 60 * 60 * 24));
        } catch (ParseException e) {
            return 0;
        }
    }
}
