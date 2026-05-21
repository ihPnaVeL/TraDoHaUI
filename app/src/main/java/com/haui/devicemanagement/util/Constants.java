package com.haui.devicemanagement.util;

/**
 * Hằng số toàn ứng dụng — tránh magic string rải rác trong code.
 */
public class Constants {

    private Constants() { /* không cho khởi tạo */ }

    // ─── Account types (SharedPreferences session) ─────────────────────────────
    public static final String ACCOUNT_USER  = "user";
    public static final String ACCOUNT_ADMIN = "admin";

    // ─── SharedPreferences keys ────────────────────────────────────────────────
    public static final String PREF_NAME        = "haui_session";
    public static final String KEY_ACCOUNT_TYPE = "account_type";
    public static final String KEY_ACCOUNT_ID   = "account_id";
    public static final String KEY_FULL_NAME    = "full_name";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_PERMISSION   = "permission_level";

    // ─── Borrow ticket status ──────────────────────────────────────────────────
    public static final String BORROW_PENDING            = "pending";
    public static final String BORROW_APPROVED           = "approved";
    public static final String BORROW_BORROWED           = "borrowed";
    public static final String BORROW_RETURNED           = "returned";
    public static final String BORROW_REJECTED           = "rejected";
    public static final String BORROW_OVERDUE            = "overdue";
    public static final String BORROW_PARTIALLY_RETURNED = "partially_returned";

    // ─── Return ticket status ──────────────────────────────────────────────────
    public static final String RETURN_PENDING   = "pending";
    public static final String RETURN_COMPLETED = "completed";
    public static final String RETURN_DAMAGED   = "damaged";
    public static final String RETURN_LOST      = "lost";

    // ─── Device availability status ────────────────────────────────────────────
    public static final String DEVICE_AVAILABLE   = "available";
    public static final String DEVICE_BORROWED    = "borrowed";
    public static final String DEVICE_MAINTENANCE = "maintenance";
    public static final String DEVICE_LOST        = "lost";

    // ─── Device condition status ───────────────────────────────────────────────
    public static final String CONDITION_GOOD    = "good";
    public static final String CONDITION_FAIR    = "fair";
    public static final String CONDITION_DAMAGED = "damaged";

    // ─── Notification types ────────────────────────────────────────────────────
    public static final String NOTIF_BORROW  = "borrow";
    public static final String NOTIF_RETURN  = "return";
    public static final String NOTIF_OVERDUE = "overdue";
    public static final String NOTIF_SYSTEM  = "system";

    // ─── Notification ref types ────────────────────────────────────────────────
    public static final String REF_BORROW = "borrow";
    public static final String REF_RETURN = "return";
    public static final String REF_DEVICE = "device";

    // ─── Permission levels ─────────────────────────────────────────────────────
    public static final String PERM_MANAGER = "manager";
    public static final String PERM_STAFF   = "staff";

    // ─── Intent extra keys ─────────────────────────────────────────────────────
    public static final String EXTRA_TICKET_ID       = "extra_ticket_id";
    public static final String EXTRA_RETURN_TICKET_ID = "extra_return_ticket_id";
    public static final String EXTRA_DEVICE_ID       = "extra_device_id";
    public static final String EXTRA_DEVICE_DETAIL_ID = "extra_device_detail_id";
    public static final String EXTRA_USER_ID         = "extra_user_id";
    public static final String EXTRA_ADMIN_ID        = "extra_admin_id";
    public static final String EXTRA_MODE            = "extra_mode";

    // ─── Mode values ───────────────────────────────────────────────────────────
    public static final String MODE_CREATE = "create";
    public static final String MODE_EDIT   = "edit";
    public static final String MODE_VIEW   = "view";
}
