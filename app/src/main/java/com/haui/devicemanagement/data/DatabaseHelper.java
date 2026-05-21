package com.haui.devicemanagement.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * DatabaseHelper — SQLiteOpenHelper cho HaUI Device Management System.
 *
 * Quản lý 9 bảng:
 *   users, admin, devices, device_detail,
 *   borrow_tickets, borrow_items,
 *   return_tickets, return_items,
 *   notifications
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public static final String DATABASE_NAME = "haui_device_management.db";
    public static final int DATABASE_VERSION = 1;

    // ─── Tên bảng ──────────────────────────────────────────────────────────────
    public static final String TABLE_USERS            = "users";
    public static final String TABLE_ADMIN            = "admin";
    public static final String TABLE_DEVICES          = "devices";
    public static final String TABLE_DEVICE_DETAIL    = "device_detail";
    public static final String TABLE_BORROW_TICKETS   = "borrow_tickets";
    public static final String TABLE_BORROW_ITEMS     = "borrow_items";
    public static final String TABLE_RETURN_TICKETS   = "return_tickets";
    public static final String TABLE_RETURN_ITEMS     = "return_items";
    public static final String TABLE_NOTIFICATIONS    = "notifications";

    // ─── DDL — users ───────────────────────────────────────────────────────────
    private static final String CREATE_USERS =
        "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
        "  id              INTEGER PRIMARY KEY AUTOINCREMENT," +
        "  mssv            TEXT UNIQUE NOT NULL," +
        "  full_name       TEXT NOT NULL," +
        "  password_hash   TEXT NOT NULL," +
        "  phone           TEXT," +
        "  email           TEXT," +
        "  class_name      TEXT," +
        "  faculty         TEXT," +
        "  is_active       INTEGER DEFAULT 1," +
        "  created_at      TEXT DEFAULT CURRENT_TIMESTAMP" +
        ");";

    // ─── DDL — admin ───────────────────────────────────────────────────────────
    private static final String CREATE_ADMIN =
        "CREATE TABLE IF NOT EXISTS " + TABLE_ADMIN + " (" +
        "  id               INTEGER PRIMARY KEY AUTOINCREMENT," +
        "  admin_code       TEXT UNIQUE NOT NULL," +
        "  full_name        TEXT NOT NULL," +
        "  email            TEXT UNIQUE NOT NULL," +
        "  password_hash    TEXT NOT NULL," +
        "  phone            TEXT," +
        "  permission_level TEXT DEFAULT 'staff'," +
        "  is_active        INTEGER DEFAULT 1," +
        "  created_at       TEXT DEFAULT CURRENT_TIMESTAMP" +
        ");";

    // ─── DDL — devices ─────────────────────────────────────────────────────────
    private static final String CREATE_DEVICES =
        "CREATE TABLE IF NOT EXISTS " + TABLE_DEVICES + " (" +
        "  id           INTEGER PRIMARY KEY AUTOINCREMENT," +
        "  device_code  TEXT UNIQUE NOT NULL," +
        "  device_name  TEXT NOT NULL," +
        "  category     TEXT," +
        "  brand        TEXT," +
        "  model        TEXT," +
        "  description  TEXT," +
        "  created_at   TEXT DEFAULT CURRENT_TIMESTAMP" +
        ");";

    // ─── DDL — device_detail ───────────────────────────────────────────────────
    private static final String CREATE_DEVICE_DETAIL =
        "CREATE TABLE IF NOT EXISTS " + TABLE_DEVICE_DETAIL + " (" +
        "  id                  INTEGER PRIMARY KEY AUTOINCREMENT," +
        "  device_id           INTEGER NOT NULL," +
        "  asset_code          TEXT UNIQUE NOT NULL," +
        "  serial_number       TEXT UNIQUE," +
        "  room_location       TEXT," +
        "  condition_status    TEXT DEFAULT 'good'," +
        "  availability_status TEXT DEFAULT 'available'," +
        "  purchase_date       TEXT," +
        "  note                TEXT," +
        "  FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE" +
        ");";

    // ─── DDL — borrow_tickets ──────────────────────────────────────────────────
    private static final String CREATE_BORROW_TICKETS =
        "CREATE TABLE IF NOT EXISTS " + TABLE_BORROW_TICKETS + " (" +
        "  id                   INTEGER PRIMARY KEY AUTOINCREMENT," +
        "  ticket_code          TEXT UNIQUE NOT NULL," +
        "  user_id              INTEGER NOT NULL," +
        "  status               TEXT DEFAULT 'pending'," +
        "  borrow_reason        TEXT," +
        "  expected_return_date TEXT," +
        "  approved_by          INTEGER," +
        "  approved_at          TEXT," +
        "  created_at           TEXT DEFAULT CURRENT_TIMESTAMP," +
        "  admin_note           TEXT," +
        "  FOREIGN KEY (user_id)     REFERENCES users(id)," +
        "  FOREIGN KEY (approved_by) REFERENCES admin(id)" +
        ");";

    // ─── DDL — borrow_items ────────────────────────────────────────────────────
    private static final String CREATE_BORROW_ITEMS =
        "CREATE TABLE IF NOT EXISTS " + TABLE_BORROW_ITEMS + " (" +
        "  id               INTEGER PRIMARY KEY AUTOINCREMENT," +
        "  ticket_id        INTEGER NOT NULL," +
        "  device_detail_id INTEGER," +
        "  condition_out    TEXT," +
        "  accessories_out  TEXT," +
        "  note             TEXT," +
        "  FOREIGN KEY (ticket_id)        REFERENCES borrow_tickets(id) ON DELETE CASCADE," +
        "  FOREIGN KEY (device_detail_id) REFERENCES device_detail(id)" +
        ");";

    // ─── DDL — return_tickets ──────────────────────────────────────────────────
    private static final String CREATE_RETURN_TICKETS =
        "CREATE TABLE IF NOT EXISTS " + TABLE_RETURN_TICKETS + " (" +
        "  id                INTEGER PRIMARY KEY AUTOINCREMENT," +
        "  ticket_code       TEXT UNIQUE NOT NULL," +
        "  borrow_ticket_id  INTEGER NOT NULL," +
        "  user_id           INTEGER NOT NULL," +
        "  status            TEXT DEFAULT 'pending'," +
        "  returned_at       TEXT DEFAULT CURRENT_TIMESTAMP," +
        "  confirmed_by      INTEGER," +
        "  confirmed_at      TEXT," +
        "  overall_condition TEXT," +
        "  note              TEXT," +
        "  FOREIGN KEY (borrow_ticket_id) REFERENCES borrow_tickets(id)," +
        "  FOREIGN KEY (user_id)          REFERENCES users(id)," +
        "  FOREIGN KEY (confirmed_by)     REFERENCES admin(id)" +
        ");";

    // ─── DDL — return_items ────────────────────────────────────────────────────
    private static final String CREATE_RETURN_ITEMS =
        "CREATE TABLE IF NOT EXISTS " + TABLE_RETURN_ITEMS + " (" +
        "  id               INTEGER PRIMARY KEY AUTOINCREMENT," +
        "  return_ticket_id INTEGER NOT NULL," +
        "  borrow_item_id   INTEGER NOT NULL," +
        "  device_detail_id INTEGER NOT NULL," +
        "  condition_in     TEXT," +
        "  accessories_in   TEXT," +
        "  damage_note      TEXT," +
        "  penalty_amount   INTEGER DEFAULT 0," +
        "  is_completed     INTEGER DEFAULT 0," +
        "  FOREIGN KEY (return_ticket_id) REFERENCES return_tickets(id) ON DELETE CASCADE," +
        "  FOREIGN KEY (borrow_item_id)   REFERENCES borrow_items(id)," +
        "  FOREIGN KEY (device_detail_id) REFERENCES device_detail(id)" +
        ");";

    // ─── DDL — notifications ───────────────────────────────────────────────────
    private static final String CREATE_NOTIFICATIONS =
        "CREATE TABLE IF NOT EXISTS " + TABLE_NOTIFICATIONS + " (" +
        "  id            INTEGER PRIMARY KEY AUTOINCREMENT," +
        "  receiver_type TEXT NOT NULL," +
        "  receiver_id   INTEGER NOT NULL," +
        "  type          TEXT," +
        "  title         TEXT," +
        "  message       TEXT," +
        "  ref_id        INTEGER," +
        "  ref_type      TEXT," +
        "  is_read       INTEGER DEFAULT 0," +
        "  created_at    TEXT DEFAULT CURRENT_TIMESTAMP" +
        ");";

    // ═══════════════════════════════════════════════════════════════════════════

    private static DatabaseHelper instance;

    /** Singleton — dùng chung 1 instance toàn app */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // ─── onCreate ──────────────────────────────────────────────────────────────
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Bật foreign key constraints
        db.execSQL("PRAGMA foreign_keys = ON;");

        // Tạo 9 bảng theo thứ tự phụ thuộc
        db.execSQL(CREATE_USERS);
        db.execSQL(CREATE_ADMIN);
        db.execSQL(CREATE_DEVICES);
        db.execSQL(CREATE_DEVICE_DETAIL);
        db.execSQL(CREATE_BORROW_TICKETS);
        db.execSQL(CREATE_BORROW_ITEMS);
        db.execSQL(CREATE_RETURN_TICKETS);
        db.execSQL(CREATE_RETURN_ITEMS);
        db.execSQL(CREATE_NOTIFICATIONS);

        Log.d(TAG, "Database created — 9 tables initialized");

        // Seed dữ liệu mẫu
        seedData(db);
    }

    // ─── onUpgrade ─────────────────────────────────────────────────────────────
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RETURN_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RETURN_TICKETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BORROW_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BORROW_TICKETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICE_DETAIL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADMIN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ─── onOpen ────────────────────────────────────────────────────────────────
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Bật foreign key mỗi lần mở (bắt buộc với SQLite Android)
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys = ON;");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SEED DATA
    // ═══════════════════════════════════════════════════════════════════════════

    private void seedData(SQLiteDatabase db) {
        seedAdmin(db);
        seedUsers(db);
        seedDevices(db);
        seedDeviceDetails(db);
        Log.d(TAG, "Seed data completed");
    }

    // ─── Seed Admin ────────────────────────────────────────────────────────────
    private void seedAdmin(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        cv.put("admin_code",       "ADMIN001");
        cv.put("full_name",        "Quản trị viên");
        cv.put("email",            "admin@haui.edu.vn");
        cv.put("password_hash",    "123456");
        cv.put("phone",            "0900000000");
        cv.put("permission_level", "manager");
        cv.put("is_active",        1);
        db.insert(TABLE_ADMIN, null, cv);

        // Tạo thêm 1 staff admin mẫu
        cv = new ContentValues();
        cv.put("admin_code",       "ADMIN002");
        cv.put("full_name",        "Cán bộ Quản lý 1");
        cv.put("email",            "staff@haui.edu.vn");
        cv.put("password_hash",    "123456");
        cv.put("phone",            "0900000001");
        cv.put("permission_level", "staff");
        cv.put("is_active",        1);
        db.insert(TABLE_ADMIN, null, cv);

        Log.d(TAG, "Seeded 2 admin accounts");
    }

    // ─── Seed Users ────────────────────────────────────────────────────────────
    private void seedUsers(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();

        // User 1
        cv.put("mssv",          "2023600783");
        cv.put("full_name",     "Lê Vân Phi");
        cv.put("password_hash", "123456");
        cv.put("phone",         "0911111111");
        cv.put("email",         "phi@sv.haui.edu.vn");
        cv.put("class_name",    "CNTT01");
        cv.put("faculty",       "Công nghệ thông tin");
        cv.put("is_active",     1);
        db.insert(TABLE_USERS, null, cv);

        // User 2
        cv = new ContentValues();
        cv.put("mssv",          "2021600002");
        cv.put("full_name",     "Trần Thị Bình");
        cv.put("password_hash", "123456");
        cv.put("phone",         "0922222222");
        cv.put("email",         "binh@sv.haui.edu.vn");
        cv.put("class_name",    "CNTT02");
        cv.put("faculty",       "Công nghệ thông tin");
        cv.put("is_active",     1);
        db.insert(TABLE_USERS, null, cv);

        // User 3
        cv = new ContentValues();
        cv.put("mssv",          "2021600003");
        cv.put("full_name",     "Lê Minh Cường");
        cv.put("password_hash", "123456");
        cv.put("phone",         "0933333333");
        cv.put("email",         "cuong@sv.haui.edu.vn");
        cv.put("class_name",    "DTVT01");
        cv.put("faculty",       "Điện tử - Viễn thông");
        cv.put("is_active",     1);
        db.insert(TABLE_USERS, null, cv);

        Log.d(TAG, "Seeded 3 user accounts");
    }

    // ─── Seed Devices ──────────────────────────────────────────────────────────
    private void seedDevices(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();

        // Device 1 — Laptop Dell
        cv.put("device_code",  "LAPTOP_DELL");
        cv.put("device_name",  "Laptop Dell Latitude");
        cv.put("category",     "Laptop");
        cv.put("brand",        "Dell");
        cv.put("model",        "Latitude 5420");
        cv.put("description",  "Laptop phục vụ học tập, thực hành lập trình");
        db.insert(TABLE_DEVICES, null, cv);

        // Device 2 — Máy chiếu Epson
        cv = new ContentValues();
        cv.put("device_code",  "PROJECTOR_EPSON");
        cv.put("device_name",  "Máy chiếu Epson");
        cv.put("category",     "Projector");
        cv.put("brand",        "Epson");
        cv.put("model",        "EB-X500");
        cv.put("description",  "Máy chiếu dùng cho phòng học, hội thảo");
        db.insert(TABLE_DEVICES, null, cv);

        // Device 3 — Micro không dây
        cv = new ContentValues();
        cv.put("device_code",  "MIC_WIRELESS");
        cv.put("device_name",  "Micro không dây");
        cv.put("category",     "Audio");
        cv.put("brand",        "Shure");
        cv.put("model",        "SVX24");
        cv.put("description",  "Micro không dây dùng cho thuyết trình, sự kiện");
        db.insert(TABLE_DEVICES, null, cv);

        // Device 4 — Máy tính bảng iPad
        cv = new ContentValues();
        cv.put("device_code",  "TABLET_IPAD");
        cv.put("device_name",  "Máy tính bảng iPad");
        cv.put("category",     "Tablet");
        cv.put("brand",        "Apple");
        cv.put("model",        "iPad Air 5");
        cv.put("description",  "Máy tính bảng dùng cho học tập và thực hành");
        db.insert(TABLE_DEVICES, null, cv);

        Log.d(TAG, "Seeded 4 device types");
    }

    // ─── Seed Device Details ───────────────────────────────────────────────────
    private void seedDeviceDetails(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();

        // Laptop Dell — device_id = 1
        cv.put("device_id",           1);
        cv.put("asset_code",          "HAU-LAP-001");
        cv.put("serial_number",       "DELL001");
        cv.put("room_location",       "Kho A1");
        cv.put("condition_status",    "good");
        cv.put("availability_status", "available");
        cv.put("purchase_date",       "2023-01-15");
        cv.put("note",                "Laptop mới, đủ phụ kiện: sạc, túi");
        db.insert(TABLE_DEVICE_DETAIL, null, cv);

        cv = new ContentValues();
        cv.put("device_id",           1);
        cv.put("asset_code",          "HAU-LAP-002");
        cv.put("serial_number",       "DELL002");
        cv.put("room_location",       "Kho A1");
        cv.put("condition_status",    "fair");
        cv.put("availability_status", "available");
        cv.put("purchase_date",       "2022-06-10");
        cv.put("note",                "Laptop đã qua sử dụng, pin còn tốt");
        db.insert(TABLE_DEVICE_DETAIL, null, cv);

        cv = new ContentValues();
        cv.put("device_id",           1);
        cv.put("asset_code",          "HAU-LAP-003");
        cv.put("serial_number",       "DELL003");
        cv.put("room_location",       "Kho A1");
        cv.put("condition_status",    "good");
        cv.put("availability_status", "available");
        cv.put("purchase_date",       "2023-09-01");
        cv.put("note",                "Laptop mới nhập kho tháng 9/2023");
        db.insert(TABLE_DEVICE_DETAIL, null, cv);

        // Máy chiếu Epson — device_id = 2
        cv = new ContentValues();
        cv.put("device_id",           2);
        cv.put("asset_code",          "HAU-PRO-001");
        cv.put("serial_number",       "EPS001");
        cv.put("room_location",       "Kho B2");
        cv.put("condition_status",    "good");
        cv.put("availability_status", "available");
        cv.put("purchase_date",       "2022-03-20");
        cv.put("note",                "Máy chiếu kèm remote và dây HDMI");
        db.insert(TABLE_DEVICE_DETAIL, null, cv);

        cv = new ContentValues();
        cv.put("device_id",           2);
        cv.put("asset_code",          "HAU-PRO-002");
        cv.put("serial_number",       "EPS002");
        cv.put("room_location",       "Kho B2");
        cv.put("condition_status",    "fair");
        cv.put("availability_status", "available");
        cv.put("purchase_date",       "2021-08-15");
        cv.put("note",                "Máy chiếu đã dùng, bóng đèn còn khoảng 70%");
        db.insert(TABLE_DEVICE_DETAIL, null, cv);

        // Micro không dây — device_id = 3
        cv = new ContentValues();
        cv.put("device_id",           3);
        cv.put("asset_code",          "HAU-MIC-001");
        cv.put("serial_number",       "MIC001");
        cv.put("room_location",       "Kho C1");
        cv.put("condition_status",    "good");
        cv.put("availability_status", "available");
        cv.put("purchase_date",       "2023-05-10");
        cv.put("note",                "Micro kèm bộ thu sóng và pin sạc");
        db.insert(TABLE_DEVICE_DETAIL, null, cv);

        cv = new ContentValues();
        cv.put("device_id",           3);
        cv.put("asset_code",          "HAU-MIC-002");
        cv.put("serial_number",       "MIC002");
        cv.put("room_location",       "Kho C1");
        cv.put("condition_status",    "good");
        cv.put("availability_status", "available");
        cv.put("purchase_date",       "2023-05-10");
        cv.put("note",                "Micro không dây backup");
        db.insert(TABLE_DEVICE_DETAIL, null, cv);

        // iPad — device_id = 4
        cv = new ContentValues();
        cv.put("device_id",           4);
        cv.put("asset_code",          "HAU-TAB-001");
        cv.put("serial_number",       "IPAD001");
        cv.put("room_location",       "Kho A2");
        cv.put("condition_status",    "good");
        cv.put("availability_status", "available");
        cv.put("purchase_date",       "2023-11-01");
        cv.put("note",                "iPad Air 5 kèm bút Apple Pencil và bao da");
        db.insert(TABLE_DEVICE_DETAIL, null, cv);

        Log.d(TAG, "Seeded 8 device details");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILITY — Reset database (dùng khi test)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Xóa toàn bộ dữ liệu và tạo lại — CHỈ dùng khi debug/test.
     */
    public void resetDatabase(SQLiteDatabase db) {
        onUpgrade(db, DATABASE_VERSION, DATABASE_VERSION);
        Log.d(TAG, "Database reset complete");
    }
}
