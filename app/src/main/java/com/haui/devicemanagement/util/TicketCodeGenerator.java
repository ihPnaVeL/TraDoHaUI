package com.haui.devicemanagement.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * TicketCodeGenerator — sinh mã phiếu theo spec.
 *
 * Phiếu mượn: BR-yyyyMMdd-HHmmss
 * Phiếu trả:  RT-yyyyMMdd-HHmmss
 *
 * Ví dụ:
 *   BR-20260519-093015
 *   RT-20260519-101122
 */
public class TicketCodeGenerator {

    private TicketCodeGenerator() {}

    private static final String DATE_FORMAT = "yyyyMMdd-HHmmss";

    /**
     * Sinh mã phiếu mượn: BR-yyyyMMdd-HHmmss
     */
    public static String generateBorrowCode() {
        String timestamp = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());
        return "BR-" + timestamp;
    }

    /**
     * Sinh mã phiếu trả: RT-yyyyMMdd-HHmmss
     */
    public static String generateReturnCode() {
        String timestamp = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());
        return "RT-" + timestamp;
    }
}
