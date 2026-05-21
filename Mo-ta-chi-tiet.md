# HaUI Device Management System
 
> Hệ thống là ứng dụng quản lý **mượn thiết bị / trả thiết bị** tại HaUI, có 2 nhóm tài khoản riêng: **Sinh viên (users)** và **Admin/Cán bộ quản lý (admin)**.

---

## 1. Công nghệ và phạm vi triển khai

### 1.1. Nền tảng

- IDE: Android Studio.
- Ngôn ngữ: Java.
- Database local: SQLite.
- Kiến trúc: MVP Pattern.
- Min SDK: API 27 trở lên.
- UI: XML Layout + Activity + RecyclerView + Adapter.

### 1.2. Mục tiêu nghiệp vụ

Ứng dụng cho phép sinh viên mượn thiết bị từ nhà trường/phòng lab, theo dõi trạng thái mượn, tạo phiếu trả và nhận thông báo. Admin quản lý thiết bị, duyệt phiếu mượn, gán thiết bị cụ thể, xác nhận trả từng thiết bị, xử lý thiết bị hỏng/mất/quá hạn và xem báo cáo thống kê.

---

## 2. Các điều chỉnh chính so với bản thiết kế cũ

### 2.1. Tách tài khoản thành 2 bảng riêng

Thiết kế cũ dùng một bảng `users` có cột `role` để phân biệt user/admin. Thiết kế mới phải tách thành:

- `users`: chỉ lưu tài khoản sinh viên/người mượn.
- `admin`: chỉ lưu tài khoản cán bộ quản lý/admin.

Không dùng chung cột `role` trong bảng `users` nữa.

### 2.2. Quản lý từng thiết bị vật lý

Thiết kế mới tách thiết bị thành 2 mức:

- `devices`: lưu thông tin loại/dòng thiết bị, ví dụ: Laptop Dell, Máy chiếu Epson, Micro không dây.
- `device_detail`: lưu từng thiết bị vật lý cụ thể, ví dụ: mã tài sản, serial, vị trí, tình trạng, trạng thái khả dụng.

Lý do: khi mượn/trả, hệ thống cần biết chính xác sinh viên đã mượn chiếc thiết bị nào, không chỉ biết loại thiết bị.

### 2.3. Trả theo từng item

Thiết kế mới bổ sung bảng `return_items` để ghi nhận tình trạng trả của từng thiết bị cụ thể:

- Thiết bị trả tốt.
- Thiết bị hỏng.
- Thiết bị mất.
- Thiếu phụ kiện.
- Có phí bồi thường nếu cần.

### 2.4. Phù hợp triển khai Android Java + SQLite

Database chia rõ Entity, DAO, Presenter và Activity. Các bảng có quan hệ khóa ngoại rõ ràng, dễ viết `SQLiteOpenHelper`, dễ seed dữ liệu mẫu và dễ test luồng nghiệp vụ.

---

## 3. Phân quyền và chức năng

## 3.1. User — Sinh viên

Sinh viên có các chức năng:

1. Đăng nhập bằng MSSV + mật khẩu.
2. Tìm kiếm thiết bị khả dụng.
3. Xem danh sách loại thiết bị và số lượng/tình trạng còn có thể mượn.
4. Tạo phiếu mượn thiết bị.
5. Chọn thiết bị cần mượn, nhập lý do mượn và hạn trả dự kiến.
6. Theo dõi trạng thái phiếu mượn.
7. Tạo phiếu trả từ phiếu đang mượn.
8. Xem lịch sử mượn/trả.
9. Xem hạn trả, cảnh báo quá hạn.
10. Xem thông báo.
11. Quản lý hồ sơ cá nhân.
12. Đổi mật khẩu.

## 3.2. Admin — Cán bộ quản lý

Admin có các chức năng:

1. Đăng nhập bằng mã quản trị/email + mật khẩu.
2. Xem dashboard tổng quan.
3. Duyệt phiếu mượn.
4. Từ chối phiếu mượn.
5. Gán thiết bị cụ thể từ bảng `device_detail` cho phiếu mượn.
6. Xác nhận sinh viên đã nhận thiết bị.
7. Xác nhận trả thiết bị.
8. Kiểm tra tình trạng từng thiết bị khi trả.
9. Ghi nhận thiết bị hỏng/mất/thiếu phụ kiện.
10. CRUD loại thiết bị trong bảng `devices`.
11. CRUD thiết bị vật lý trong bảng `device_detail`.
12. Quản lý tài khoản sinh viên.
13. Quản lý tài khoản admin.
14. Xem danh sách phiếu quá hạn.
15. Xem báo cáo thống kê mượn/trả/hỏng/mất.
16. Gửi/xem thông báo hệ thống.

---

## 4. Luồng trạng thái nghiệp vụ

## 4.1. Trạng thái phiếu mượn — `borrow_tickets.status`

Các trạng thái chính:

```text
pending -> approved -> borrowed -> returned
```

Các trạng thái ngoại lệ:

```text
rejected
 overdue
 partially_returned
```

Ý nghĩa:

- `pending`: sinh viên vừa tạo phiếu, chờ admin duyệt.
- `approved`: admin đã duyệt phiếu mượn.
- `borrowed`: sinh viên đã nhận thiết bị, thiết bị đang được mượn.
- `returned`: tất cả thiết bị trong phiếu đã được trả xong.
- `rejected`: admin từ chối phiếu.
- `overdue`: phiếu đã quá hạn trả.
- `partially_returned`: phiếu đã trả một phần nhưng chưa trả đủ tất cả thiết bị.

## 4.2. Trạng thái phiếu trả — `return_tickets.status`

Các trạng thái:

```text
pending -> completed
pending -> damaged
pending -> lost
```

Ý nghĩa:

- `pending`: sinh viên đã gửi yêu cầu trả, chờ admin kiểm tra.
- `completed`: tất cả thiết bị trả về ở trạng thái chấp nhận được.
- `damaged`: có thiết bị hỏng.
- `lost`: có thiết bị mất.

## 4.3. Trạng thái thiết bị chi tiết — `device_detail.availability_status`

Các trạng thái:

```text
available -> borrowed -> available
available -> borrowed -> maintenance
available -> borrowed -> lost
```

Ý nghĩa:

- `available`: thiết bị sẵn sàng cho mượn.
- `borrowed`: thiết bị đang được mượn.
- `maintenance`: thiết bị hỏng/cần bảo trì.
- `lost`: thiết bị bị mất.

## 4.4. Trạng thái tình trạng vật lý — `device_detail.condition_status`

Các giá trị:

- `good`: tốt.
- `fair`: dùng được nhưng đã cũ/có hao mòn.
- `damaged`: hỏng.

---

## 5. Database SQLite — 9 bảng chính

Database có 9 bảng:

1. `users`
2. `admin`
3. `devices`
4. `device_detail`
5. `borrow_tickets`
6. `borrow_items`
7. `return_tickets`
8. `return_items`
9. `notifications`

---

## 5.1. Bảng `users`

Chỉ lưu tài khoản sinh viên/người mượn.

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `id` | INTEGER PRIMARY KEY AUTOINCREMENT | Mã sinh viên nội bộ |
| `mssv` | TEXT UNIQUE NOT NULL | Mã số sinh viên |
| `full_name` | TEXT NOT NULL | Họ tên |
| `password_hash` | TEXT NOT NULL | Mật khẩu mã hóa |
| `phone` | TEXT | Số điện thoại |
| `email` | TEXT | Email sinh viên |
| `class_name` | TEXT | Lớp |
| `faculty` | TEXT | Khoa/viện |
| `is_active` | INTEGER DEFAULT 1 | 0/1 |
| `created_at` | TEXT | Ngày tạo |

## 5.2. Bảng `admin`

Chỉ lưu tài khoản cán bộ quản lý/admin.

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `id` | INTEGER PRIMARY KEY AUTOINCREMENT | Mã quản trị |
| `admin_code` | TEXT UNIQUE NOT NULL | Mã cán bộ |
| `full_name` | TEXT NOT NULL | Họ tên |
| `email` | TEXT UNIQUE NOT NULL | Email đăng nhập |
| `password_hash` | TEXT NOT NULL | Mật khẩu mã hóa |
| `phone` | TEXT | Số điện thoại |
| `permission_level` | TEXT | `manager` hoặc `staff` |
| `is_active` | INTEGER DEFAULT 1 | 0/1 |
| `created_at` | TEXT | Ngày tạo |

## 5.3. Bảng `devices`

Lưu thông tin loại/dòng thiết bị.

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `id` | INTEGER PRIMARY KEY AUTOINCREMENT | Mã loại thiết bị |
| `device_code` | TEXT UNIQUE NOT NULL | Mã loại thiết bị |
| `device_name` | TEXT NOT NULL | Tên thiết bị |
| `category` | TEXT | Danh mục |
| `brand` | TEXT | Hãng |
| `model` | TEXT | Model |
| `description` | TEXT | Mô tả |
| `created_at` | TEXT | Ngày tạo |

## 5.4. Bảng `device_detail`

Lưu từng thiết bị vật lý cụ thể.

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `id` | INTEGER PRIMARY KEY AUTOINCREMENT | Mã thiết bị vật lý |
| `device_id` | INTEGER NOT NULL | FK đến `devices.id` |
| `asset_code` | TEXT UNIQUE NOT NULL | Mã tài sản |
| `serial_number` | TEXT UNIQUE | Số serial |
| `room_location` | TEXT | Vị trí/kho |
| `condition_status` | TEXT DEFAULT 'good' | `good`, `fair`, `damaged` |
| `availability_status` | TEXT DEFAULT 'available' | `available`, `borrowed`, `maintenance`, `lost` |
| `purchase_date` | TEXT | Ngày nhập |
| `note` | TEXT | Ghi chú |

## 5.5. Bảng `borrow_tickets`

Lưu phiếu mượn cấp tổng.

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `id` | INTEGER PRIMARY KEY AUTOINCREMENT | Mã phiếu mượn |
| `ticket_code` | TEXT UNIQUE NOT NULL | Mã phiếu hiển thị |
| `user_id` | INTEGER NOT NULL | FK đến `users.id` |
| `status` | TEXT DEFAULT 'pending' | `pending`, `approved`, `borrowed`, `returned`, `rejected`, `overdue`, `partially_returned` |
| `borrow_reason` | TEXT | Lý do mượn |
| `expected_return_date` | TEXT | Hạn trả |
| `approved_by` | INTEGER | FK đến `admin.id` |
| `approved_at` | TEXT | Thời điểm duyệt |
| `created_at` | TEXT | Ngày tạo |
| `admin_note` | TEXT | Ghi chú xử lý |

## 5.6. Bảng `borrow_items`

Lưu từng dòng thiết bị được mượn trong một phiếu mượn.

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `id` | INTEGER PRIMARY KEY AUTOINCREMENT | Mã dòng mượn |
| `ticket_id` | INTEGER NOT NULL | FK đến `borrow_tickets.id` |
| `device_detail_id` | INTEGER | FK đến `device_detail.id` |
| `condition_out` | TEXT | Tình trạng lúc giao |
| `accessories_out` | TEXT | Phụ kiện đi kèm |
| `note` | TEXT | Ghi chú |

Ghi chú:

- Khi sinh viên vừa tạo phiếu, có thể chưa gán `device_detail_id` nếu admin sẽ chọn thiết bị cụ thể sau.
- Khi admin duyệt và gán thiết bị, cập nhật `device_detail_id` và chuyển `device_detail.availability_status = 'borrowed'`.

## 5.7. Bảng `return_tickets`

Lưu phiếu trả cấp tổng.

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `id` | INTEGER PRIMARY KEY AUTOINCREMENT | Mã phiếu trả |
| `ticket_code` | TEXT UNIQUE NOT NULL | Mã phiếu hiển thị |
| `borrow_ticket_id` | INTEGER NOT NULL | FK đến `borrow_tickets.id` |
| `user_id` | INTEGER NOT NULL | FK đến `users.id` |
| `status` | TEXT DEFAULT 'pending' | `pending`, `completed`, `damaged`, `lost` |
| `returned_at` | TEXT | Ngày sinh viên gửi trả |
| `confirmed_by` | INTEGER | FK đến `admin.id` |
| `confirmed_at` | TEXT | Ngày admin xác nhận |
| `overall_condition` | TEXT | Kết luận chung |
| `note` | TEXT | Ghi chú |

## 5.8. Bảng `return_items`

Lưu từng dòng thiết bị trả trong một phiếu trả.

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `id` | INTEGER PRIMARY KEY AUTOINCREMENT | Mã dòng trả |
| `return_ticket_id` | INTEGER NOT NULL | FK đến `return_tickets.id` |
| `borrow_item_id` | INTEGER NOT NULL | FK đến `borrow_items.id` |
| `device_detail_id` | INTEGER NOT NULL | FK đến `device_detail.id` |
| `condition_in` | TEXT | `good`, `damaged`, `lost` |
| `accessories_in` | TEXT | Phụ kiện trả lại |
| `damage_note` | TEXT | Mô tả hỏng/mất |
| `penalty_amount` | INTEGER DEFAULT 0 | Phí bồi thường nếu có |
| `is_completed` | INTEGER DEFAULT 0 | 0/1 |

## 5.9. Bảng `notifications`

Lưu thông báo cho user hoặc admin.

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `id` | INTEGER PRIMARY KEY AUTOINCREMENT | Mã thông báo |
| `receiver_type` | TEXT NOT NULL | `user` hoặc `admin` |
| `receiver_id` | INTEGER NOT NULL | ID người nhận |
| `type` | TEXT | `borrow`, `return`, `overdue`, `system` |
| `title` | TEXT | Tiêu đề |
| `message` | TEXT | Nội dung |
| `ref_id` | INTEGER | ID phiếu liên quan |
| `ref_type` | TEXT | `borrow`, `return`, `device` |
| `is_read` | INTEGER DEFAULT 0 | 0/1 |
| `created_at` | TEXT | Ngày tạo |

---

## 6. Quan hệ dữ liệu

Các quan hệ chính:

```text
users.id              1 --- n   borrow_tickets.user_id
users.id              1 --- n   return_tickets.user_id
admin.id              1 --- n   borrow_tickets.approved_by
admin.id              1 --- n   return_tickets.confirmed_by

devices.id            1 --- n   device_detail.device_id
device_detail.id      1 --- n   borrow_items.device_detail_id
device_detail.id      1 --- n   return_items.device_detail_id

borrow_tickets.id     1 --- n   borrow_items.ticket_id
borrow_tickets.id     1 --- n   return_tickets.borrow_ticket_id

return_tickets.id     1 --- n   return_items.return_ticket_id
borrow_items.id       1 --- 1   return_items.borrow_item_id
```

Ý tưởng quan trọng:

- `devices` đại diện cho loại thiết bị.
- `device_detail` đại diện cho từng thiết bị vật lý.
- Khi mượn: tạo `borrow_tickets` và `borrow_items`.
- Khi admin duyệt: gán `device_detail` vào `borrow_items`.
- Khi trả: tạo `return_tickets` và `return_items` để kiểm tra từng món.

---

## 7. SQLite DDL

`DatabaseHelper.java` với đầy đủ câu lệnh tạo bảng

```sql
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    mssv TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    phone TEXT,
    email TEXT,
    class_name TEXT,
    faculty TEXT,
    is_active INTEGER DEFAULT 1,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    admin_code TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    phone TEXT,
    permission_level TEXT DEFAULT 'staff',
    is_active INTEGER DEFAULT 1,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS devices (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_code TEXT UNIQUE NOT NULL,
    device_name TEXT NOT NULL,
    category TEXT,
    brand TEXT,
    model TEXT,
    description TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS device_detail (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_id INTEGER NOT NULL,
    asset_code TEXT UNIQUE NOT NULL,
    serial_number TEXT UNIQUE,
    room_location TEXT,
    condition_status TEXT DEFAULT 'good',
    availability_status TEXT DEFAULT 'available',
    purchase_date TEXT,
    note TEXT,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS borrow_tickets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ticket_code TEXT UNIQUE NOT NULL,
    user_id INTEGER NOT NULL,
    status TEXT DEFAULT 'pending',
    borrow_reason TEXT,
    expected_return_date TEXT,
    approved_by INTEGER,
    approved_at TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    admin_note TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES admin(id)
);

CREATE TABLE IF NOT EXISTS borrow_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ticket_id INTEGER NOT NULL,
    device_detail_id INTEGER,
    condition_out TEXT,
    accessories_out TEXT,
    note TEXT,
    FOREIGN KEY (ticket_id) REFERENCES borrow_tickets(id) ON DELETE CASCADE,
    FOREIGN KEY (device_detail_id) REFERENCES device_detail(id)
);

CREATE TABLE IF NOT EXISTS return_tickets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ticket_code TEXT UNIQUE NOT NULL,
    borrow_ticket_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    status TEXT DEFAULT 'pending',
    returned_at TEXT DEFAULT CURRENT_TIMESTAMP,
    confirmed_by INTEGER,
    confirmed_at TEXT,
    overall_condition TEXT,
    note TEXT,
    FOREIGN KEY (borrow_ticket_id) REFERENCES borrow_tickets(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (confirmed_by) REFERENCES admin(id)
);

CREATE TABLE IF NOT EXISTS return_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    return_ticket_id INTEGER NOT NULL,
    borrow_item_id INTEGER NOT NULL,
    device_detail_id INTEGER NOT NULL,
    condition_in TEXT,
    accessories_in TEXT,
    damage_note TEXT,
    penalty_amount INTEGER DEFAULT 0,
    is_completed INTEGER DEFAULT 0,
    FOREIGN KEY (return_ticket_id) REFERENCES return_tickets(id) ON DELETE CASCADE,
    FOREIGN KEY (borrow_item_id) REFERENCES borrow_items(id),
    FOREIGN KEY (device_detail_id) REFERENCES device_detail(id)
);

CREATE TABLE IF NOT EXISTS notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    receiver_type TEXT NOT NULL,
    receiver_id INTEGER NOT NULL,
    type TEXT,
    title TEXT,
    message TEXT,
    ref_id INTEGER,
    ref_type TEXT,
    is_read INTEGER DEFAULT 0,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);
```

---

## 8. Dữ liệu mẫu

### 8.1. Admin mẫu

```text
admin_code: ADMIN001
full_name: Quản trị viên
email: admin@haui.edu.vn
password_hash: 123456
phone: 0900000000
permission_level: manager
is_active: 1
```

Ghi chú: trong demo có thể lưu password dạng plain text `123456`, nhưng nên đặt tên cột là `password_hash` để dễ nâng cấp mã hóa sau.

### 8.2. User mẫu

```text
mssv: 2021600001
full_name: Nguyễn Văn An
password_hash: 123456
phone: 0911111111
email: an@sv.haui.edu.vn
class_name: CNTT01
faculty: Công nghệ thông tin
is_active: 1
```

```text
mssv: 2021600002
full_name: Trần Thị Bình
password_hash: 123456
phone: 0922222222
email: binh@sv.haui.edu.vn
class_name: CNTT02
faculty: Công nghệ thông tin
is_active: 1
```

### 8.3. Devices mẫu

```text
device_code: LAPTOP_DELL
device_name: Laptop Dell Latitude
category: Laptop
brand: Dell
model: Latitude 5420
```

```text
device_code: PROJECTOR_EPSON
device_name: Máy chiếu Epson
category: Projector
brand: Epson
model: EB-X500
```

```text
device_code: MIC_WIRELESS
device_name: Micro không dây
category: Audio
brand: Shure
model: SVX24
```

### 8.4. Device detail mẫu

```text
asset_code: HAU-LAP-001
serial_number: DELL001
room_location: Kho A1
condition_status: good
availability_status: available
```

```text
asset_code: HAU-LAP-002
serial_number: DELL002
room_location: Kho A1
condition_status: fair
availability_status: available
```

```text
asset_code: HAU-PRO-001
serial_number: EPS001
room_location: Kho B2
condition_status: good
availability_status: available
```

```text
asset_code: HAU-MIC-001
serial_number: MIC001
room_location: Kho C1
condition_status: good
availability_status: available
```

---

## 9. Danh sách màn hình ứng dụng

## 9.1. Màn hình dùng chung

### `LoginActivity`

Chức năng:

- Cho phép đăng nhập.
- Có thể dùng 2 tab: `Sinh viên` và `Admin`.
- Hoặc tự nhận diện tài khoản bằng cách kiểm tra bảng `users` trước, nếu không có thì kiểm tra bảng `admin`.
- Sau đăng nhập, lưu session bằng `SharedPreferences`.

Session cần lưu:

```text
account_type: user/admin
account_id: id trong bảng users hoặc admin
full_name: tên người dùng
is_logged_in: true/false
```

### `ChangePasswordActivity`

Chức năng:

- Đổi mật khẩu theo `account_type`.
- Nếu là `user`, cập nhật bảng `users`.
- Nếu là `admin`, cập nhật bảng `admin`.

### `NotificationActivity`

Chức năng:

- Hiển thị thông báo theo `receiver_type` và `receiver_id`.
- Cho phép đánh dấu đã đọc.

## 9.2. Màn hình User/Sinh viên

### `UserHomeActivity`

Dashboard sinh viên:

- Số phiếu đang chờ duyệt.
- Số phiếu đang mượn.
- Số phiếu quá hạn.
- Nút tạo phiếu mượn.
- Nút tạo phiếu trả.
- Nút xem lịch sử.

### `DeviceSearchActivity`

Chức năng:

- Tìm kiếm loại thiết bị còn khả dụng.
- Lọc theo danh mục.
- Hiển thị số lượng thiết bị `available` theo từng loại.
- Cho phép chọn thiết bị để thêm vào phiếu mượn.

### `BorrowCreateActivity`

Chức năng:

- Tạo phiếu mượn.
- Chọn thiết bị muốn mượn.
- Nhập lý do mượn.
- Chọn hạn trả dự kiến.
- Lưu vào `borrow_tickets` và `borrow_items`.
- Trạng thái ban đầu của phiếu: `pending`.

### `ReturnCreateActivity`

Chức năng:

- Chọn phiếu mượn đang ở trạng thái `borrowed`.
- Hiển thị danh sách `borrow_items` của phiếu.
- Tạo `return_tickets`.
- Tự sinh `return_items` tương ứng với các `borrow_items`.
- Trạng thái ban đầu của phiếu trả: `pending`.

### `MyBorrowActivity`

Chức năng:

- Hiển thị lịch sử phiếu mượn của sinh viên.
- Lọc theo trạng thái: `pending`, `approved`, `borrowed`, `returned`, `rejected`, `overdue`.

### `MyReturnActivity`

Chức năng:

- Hiển thị lịch sử phiếu trả của sinh viên.
- Lọc theo trạng thái: `pending`, `completed`, `damaged`, `lost`.

### `TicketDetailActivity`

Chức năng:

- Hiển thị chi tiết phiếu mượn hoặc phiếu trả.
- Hiển thị timeline trạng thái.
- Hiển thị danh sách thiết bị cụ thể.
- Hiển thị ghi chú admin.

### `UserProfileActivity`

Chức năng:

- Xem thông tin cá nhân.
- Cập nhật số điện thoại/email nếu cho phép.
- Đổi mật khẩu.

## 9.3. Màn hình Admin

### `AdminDashboardActivity`

Dashboard admin:

- Số phiếu mượn đang chờ duyệt.
- Số phiếu trả đang chờ xác nhận.
- Số thiết bị đang được mượn.
- Số thiết bị hỏng/mất.
- Số phiếu quá hạn.

### `BorrowApprovalActivity`

Chức năng:

- Xem danh sách phiếu mượn `pending`.
- Xem chi tiết phiếu.
- Duyệt hoặc từ chối.
- Nếu duyệt, chuyển sang màn hình gán thiết bị cụ thể.
- Nếu từ chối, cập nhật `borrow_tickets.status = 'rejected'` và ghi `admin_note`.

### `AssignDeviceActivity`

Chức năng:

- Admin gán thiết bị cụ thể từ bảng `device_detail`.
- Chỉ được chọn thiết bị có `availability_status = 'available'`.
- Cập nhật `borrow_items.device_detail_id`.
- Cập nhật `borrow_items.condition_out`.
- Cập nhật `borrow_items.accessories_out`.
- Cập nhật `device_detail.availability_status = 'borrowed'`.
- Cập nhật `borrow_tickets.status = 'borrowed'` hoặc `approved` tùy quy trình.

Khuyến nghị: sau khi admin duyệt và giao thiết bị, đặt luôn `borrow_tickets.status = 'borrowed'` để đơn giản hóa.

### `ReturnApprovalActivity`

Chức năng:

- Xem danh sách phiếu trả `pending`.
- Kiểm tra từng `return_items`.
- Nhập `condition_in`, `accessories_in`, `damage_note`, `penalty_amount`.
- Nếu trả tốt: cập nhật `device_detail.availability_status = 'available'`.
- Nếu hỏng: cập nhật `device_detail.availability_status = 'maintenance'`, `condition_status = 'damaged'`.
- Nếu mất: cập nhật `device_detail.availability_status = 'lost'`.
- Cập nhật `return_items.is_completed = 1`.
- Cập nhật `return_tickets.status` thành `completed`, `damaged` hoặc `lost`.
- Nếu toàn bộ item đã trả, cập nhật `borrow_tickets.status = 'returned'`.
- Nếu mới trả một phần, cập nhật `borrow_tickets.status = 'partially_returned'`.

### `DeviceManageActivity`

Chức năng:

- CRUD bảng `devices`.
- Thêm/sửa/xóa loại thiết bị.
- Xem số lượng thiết bị con theo từng loại.

### `DeviceDetailManageActivity`

Chức năng:

- CRUD bảng `device_detail`.
- Thêm/sửa/xóa từng thiết bị vật lý.
- Lọc theo `availability_status`.
- Lọc theo `condition_status`.

### `UserManageActivity`

Chức năng:

- CRUD tài khoản sinh viên.
- Khóa/mở tài khoản bằng `is_active`.
- Reset mật khẩu nếu cần.

### `AdminManageActivity`

Chức năng:

- Quản lý tài khoản admin.
- Chỉ admin có `permission_level = 'manager'` mới được tạo/sửa/xóa admin khác.

### `OverdueActivity`

Chức năng:

- Hiển thị phiếu đang `borrowed` nhưng `expected_return_date` đã nhỏ hơn ngày hiện tại.
- Có thể tự cập nhật `borrow_tickets.status = 'overdue'`.
- Gửi notification cho user.

### `ReportActivity`

Chức năng:

- Thống kê số phiếu mượn theo tháng.
- Thống kê số phiếu trả theo tháng.
- Thống kê số thiết bị đang mượn.
- Thống kê thiết bị hỏng/mất.
- Thống kê top thiết bị được mượn nhiều.

---

## 10. Kiến trúc Android

## 10.1. View Layer

Bao gồm:

- Activity.
- Fragment nếu cần.
- XML Layout.
- RecyclerView.
- Adapter.
- Dialog.

Nhiệm vụ:

- Hiển thị dữ liệu.
- Nhận input từ người dùng.
- Gọi Presenter xử lý logic.
- Không viết SQL trực tiếp trong Activity.

## 10.2. Presenter Layer

Bao gồm:

- `AuthPresenter`
- `BorrowPresenter`
- `ReturnPresenter`
- `DevicePresenter`
- `ReportPresenter`
- `NotificationPresenter`

Nhiệm vụ:

- Xử lý business logic.
- Validate form.
- Gọi DAO.
- Trả kết quả về View.

## 10.3. Model Layer

Bao gồm:

- Entity POJO.
- DAO classes.
- `DatabaseHelper` kế thừa `SQLiteOpenHelper`.
- `SessionManager` dùng `SharedPreferences`.

---

## 11. Cấu trúc thư mục Android

```text
app/src/main/java/com/haui/devicemanagement/
├── data/
│   ├── DatabaseHelper.java
│   ├── dao/
│   │   ├── UserDao.java
│   │   ├── AdminDao.java
│   │   ├── DeviceDao.java
│   │   ├── DeviceDetailDao.java
│   │   ├── BorrowTicketDao.java
│   │   ├── BorrowItemDao.java
│   │   ├── ReturnTicketDao.java
│   │   ├── ReturnItemDao.java
│   │   └── NotificationDao.java
│   └── entity/
│       ├── User.java
│       ├── Admin.java
│       ├── Device.java
│       ├── DeviceDetail.java
│       ├── BorrowTicket.java
│       ├── BorrowItem.java
│       ├── ReturnTicket.java
│       ├── ReturnItem.java
│       └── Notification.java
│
├── presenter/
│   ├── AuthPresenter.java
│   ├── BorrowPresenter.java
│   ├── ReturnPresenter.java
│   ├── DevicePresenter.java
│   ├── ReportPresenter.java
│   └── NotificationPresenter.java
│
├── view/
│   ├── auth/
│   │   ├── LoginActivity.java
│   │   └── ChangePasswordActivity.java
│   ├── user/
│   │   ├── UserHomeActivity.java
│   │   ├── DeviceSearchActivity.java
│   │   ├── BorrowCreateActivity.java
│   │   ├── ReturnCreateActivity.java
│   │   ├── MyBorrowActivity.java
│   │   ├── MyReturnActivity.java
│   │   ├── TicketDetailActivity.java
│   │   └── UserProfileActivity.java
│   ├── admin/
│   │   ├── AdminDashboardActivity.java
│   │   ├── BorrowApprovalActivity.java
│   │   ├── AssignDeviceActivity.java
│   │   ├── ReturnApprovalActivity.java
│   │   ├── DeviceManageActivity.java
│   │   ├── DeviceDetailManageActivity.java
│   │   ├── UserManageActivity.java
│   │   ├── AdminManageActivity.java
│   │   ├── OverdueActivity.java
│   │   └── ReportActivity.java
│   └── common/
│       └── NotificationActivity.java
│
├── adapter/
│   ├── DeviceAdapter.java
│   ├── DeviceDetailAdapter.java
│   ├── BorrowTicketAdapter.java
│   ├── BorrowItemAdapter.java
│   ├── ReturnTicketAdapter.java
│   ├── ReturnItemAdapter.java
│   └── NotificationAdapter.java
│
├── util/
│   ├── SessionManager.java
│   ├── DateUtils.java
│   ├── PasswordUtils.java
│   ├── TicketCodeGenerator.java
│   └── Constants.java
│
└── MainActivity.java
```

---

## 12. Lớp Entity

POJO tương ứng với 9 bảng:

1. `User`
2. `Admin`
3. `Device`
4. `DeviceDetail`
5. `BorrowTicket`
6. `BorrowItem`
7. `ReturnTicket`
8. `ReturnItem`
9. `Notification`

Mỗi entity có:

- Field tương ứng cột database.
- Constructor rỗng.
- Constructor đầy đủ.
- Getter/setter.

---

## 13. DAO

## 13.1. `UserDao`

```text
login(mssv, password)
getById(id)
getAll()
insert(user)
update(user)
setActive(id, isActive)
changePassword(id, newPassword)
```

## 13.2. `AdminDao`

```text
login(emailOrCode, password)
getById(id)
getAll()
insert(admin)
update(admin)
setActive(id, isActive)
changePassword(id, newPassword)
```

## 13.3. `DeviceDao`

```text
getAll()
getById(id)
insert(device)
update(device)
delete(id)
search(keyword)
getAvailableCountByDevice(deviceId)
```

## 13.4. `DeviceDetailDao`

```text
getAll()
getById(id)
getByDeviceId(deviceId)
getAvailableByDeviceId(deviceId)
insert(deviceDetail)
update(deviceDetail)
delete(id)
updateAvailabilityStatus(id, status)
updateConditionStatus(id, status)
```

## 13.5. `BorrowTicketDao`

```text
createBorrowTicket(ticket, items)
getById(id)
getByUserId(userId)
getPendingTickets()
getBorrowedTicketsByUser(userId)
approveTicket(ticketId, adminId, note)
rejectTicket(ticketId, adminId, note)
updateStatus(ticketId, status)
getOverdueTickets(currentDate)
```

## 13.6. `BorrowItemDao`

```text
getByTicketId(ticketId)
insert(item)
assignDeviceDetail(borrowItemId, deviceDetailId, conditionOut, accessoriesOut)
```

## 13.7. `ReturnTicketDao`

```text
createReturnTicket(returnTicket, returnItems)
getById(id)
getByUserId(userId)
getPendingReturns()
confirmReturn(returnTicketId, adminId, status, note)
updateStatus(returnTicketId, status)
```

## 13.8. `ReturnItemDao`

```text
getByReturnTicketId(returnTicketId)
insert(returnItem)
updateCheckResult(returnItemId, conditionIn, accessoriesIn, damageNote, penaltyAmount, isCompleted)
```

## 13.9. `NotificationDao`

```text
insert(notification)
getByReceiver(receiverType, receiverId)
getUnreadCount(receiverType, receiverId)
markAsRead(id)
markAllAsRead(receiverType, receiverId)
```

---

## 14. Luồng nghiệp vụ chi tiết

## 14.1. Luồng đăng nhập

1. Người dùng mở `LoginActivity`.
2. Người dùng chọn loại tài khoản hoặc hệ thống tự nhận diện.
3. Nếu đăng nhập sinh viên:
   - Kiểm tra `users.mssv` và `users.password_hash`.
   - Chỉ cho đăng nhập nếu `is_active = 1`.
   - Lưu session `account_type = user`.
   - Chuyển đến `UserHomeActivity`.
4. Nếu đăng nhập admin:
   - Kiểm tra `admin.email` hoặc `admin.admin_code` và `admin.password_hash`.
   - Chỉ cho đăng nhập nếu `is_active = 1`.
   - Lưu session `account_type = admin`.
   - Chuyển đến `AdminDashboardActivity`.

## 14.2. Luồng sinh viên tạo phiếu mượn

1. Sinh viên vào `DeviceSearchActivity`.
2. Hệ thống hiển thị danh sách `devices` kèm số lượng `device_detail` có `availability_status = 'available'`.
3. Sinh viên chọn thiết bị muốn mượn.
4. Sinh viên nhập lý do mượn và ngày trả dự kiến.
5. Sinh viên nhấn gửi phiếu.
6. Hệ thống tạo `borrow_tickets` với `status = 'pending'`.
7. Hệ thống tạo các dòng `borrow_items` tương ứng.
8. Hệ thống gửi notification cho admin.

## 14.3. Luồng admin duyệt phiếu mượn

1. Admin vào `BorrowApprovalActivity`.
2. Hệ thống hiển thị danh sách `borrow_tickets.status = 'pending'`.
3. Admin mở chi tiết phiếu.
4. Nếu từ chối:
   - Cập nhật `borrow_tickets.status = 'rejected'`.
   - Ghi `admin_note`.
   - Gửi notification cho user.
5. Nếu duyệt:
   - Chuyển sang `AssignDeviceActivity`.
   - Admin chọn từng thiết bị cụ thể từ `device_detail` có `availability_status = 'available'`.
   - Cập nhật `borrow_items.device_detail_id`.
   - Cập nhật `device_detail.availability_status = 'borrowed'`.
   - Cập nhật `borrow_tickets.status = 'borrowed'`.
   - Lưu `approved_by`, `approved_at`.
   - Gửi notification cho user.

## 14.4. Luồng sinh viên tạo phiếu trả

1. Sinh viên vào `ReturnCreateActivity`.
2. Hệ thống hiển thị các phiếu mượn đang `borrowed` hoặc `overdue`.
3. Sinh viên chọn phiếu cần trả.
4. Hệ thống lấy danh sách `borrow_items` của phiếu.
5. Sinh viên xác nhận tạo phiếu trả.
6. Hệ thống tạo `return_tickets` với `status = 'pending'`.
7. Hệ thống tạo `return_items` tương ứng với từng `borrow_items`.
8. Gửi notification cho admin.

## 14.5. Luồng admin xác nhận trả thiết bị

1. Admin vào `ReturnApprovalActivity`.
2. Hệ thống hiển thị danh sách `return_tickets.status = 'pending'`.
3. Admin mở chi tiết phiếu trả.
4. Admin kiểm tra từng `return_items`.
5. Với mỗi item:
   - Nếu tốt: `condition_in = 'good'`, `device_detail.availability_status = 'available'`.
   - Nếu hỏng: `condition_in = 'damaged'`, `device_detail.availability_status = 'maintenance'`, `device_detail.condition_status = 'damaged'`.
   - Nếu mất: `condition_in = 'lost'`, `device_detail.availability_status = 'lost'`, nhập `penalty_amount` nếu có.
6. Cập nhật `return_items.is_completed = 1`.
7. Nếu tất cả item tốt: `return_tickets.status = 'completed'`.
8. Nếu có item hỏng: `return_tickets.status = 'damaged'`.
9. Nếu có item mất: `return_tickets.status = 'lost'`.
10. Nếu tất cả item của phiếu mượn đã trả: `borrow_tickets.status = 'returned'`.
11. Nếu mới trả một phần: `borrow_tickets.status = 'partially_returned'`.
12. Gửi notification cho user.

## 14.6. Luồng kiểm tra quá hạn

1. Admin vào `OverdueActivity`.
2. Hệ thống tìm các phiếu:

```sql
SELECT * FROM borrow_tickets
WHERE status = 'borrowed'
AND expected_return_date < currentDate;
```

3. Cập nhật các phiếu này thành `status = 'overdue'` nếu cần.
4. Gửi notification nhắc sinh viên trả thiết bị.

---

## 15. Quy tắc xử lý quan trọng

## 15.1. Quy tắc khi mượn

- Sinh viên không được tự chọn `device_detail` cụ thể nếu quy trình yêu cầu admin gán thiết bị.
- Sinh viên chỉ chọn loại thiết bị từ `devices`.
- Admin chịu trách nhiệm gán thiết bị cụ thể từ `device_detail`.
- Chỉ gán thiết bị có `availability_status = 'available'`.
- Khi gán xong, thiết bị chuyển sang `borrowed`.

## 15.2. Quy tắc khi trả

- Mỗi `borrow_item` phải có tối đa một `return_item` hoàn tất.
- Khi trả tốt, thiết bị chuyển về `available`.
- Khi hỏng, thiết bị chuyển sang `maintenance`.
- Khi mất, thiết bị chuyển sang `lost`.
- Nếu có phí bồi thường, lưu vào `return_items.penalty_amount`.

## 15.3. Quy tắc thông báo

Tạo thông báo khi:

- Sinh viên tạo phiếu mượn.
- Admin duyệt phiếu mượn.
- Admin từ chối phiếu mượn.
- Sinh viên tạo phiếu trả.
- Admin xác nhận phiếu trả.
- Phiếu bị quá hạn.

## 15.4. Quy tắc phân quyền

- `users` không được vào màn hình admin.
- `admin` không được tạo phiếu mượn/trả với tư cách sinh viên, trừ khi có chức năng tạo hộ được bổ sung sau.
- Chỉ `admin.permission_level = 'manager'` mới được quản lý tài khoản admin.

---

## 16. Ticket code generator

`TicketCodeGenerator`.

Quy tắc mã phiếu:

```text
Phiếu mượn: BR-yyyyMMdd-HHmmss
Phiếu trả: RT-yyyyMMdd-HHmmss
```

Ví dụ:

```text
BR-20260519-093015
RT-20260519-101122
```

---

## 17. Constants

`Constants.java`:

```java
public class Constants {
    public static final String ACCOUNT_USER = "user";
    public static final String ACCOUNT_ADMIN = "admin";

    public static final String BORROW_PENDING = "pending";
    public static final String BORROW_APPROVED = "approved";
    public static final String BORROW_BORROWED = "borrowed";
    public static final String BORROW_RETURNED = "returned";
    public static final String BORROW_REJECTED = "rejected";
    public static final String BORROW_OVERDUE = "overdue";
    public static final String BORROW_PARTIALLY_RETURNED = "partially_returned";

    public static final String RETURN_PENDING = "pending";
    public static final String RETURN_COMPLETED = "completed";
    public static final String RETURN_DAMAGED = "damaged";
    public static final String RETURN_LOST = "lost";

    public static final String DEVICE_AVAILABLE = "available";
    public static final String DEVICE_BORROWED = "borrowed";
    public static final String DEVICE_MAINTENANCE = "maintenance";
    public static final String DEVICE_LOST = "lost";

    public static final String CONDITION_GOOD = "good";
    public static final String CONDITION_FAIR = "fair";
    public static final String CONDITION_DAMAGED = "damaged";
}
```
