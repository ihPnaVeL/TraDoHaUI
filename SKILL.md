# Nguyên tắc chuyển đổi từ giao diện tối sang giao diện sáng trong thiết kế ứng dụng Android

## Tổng quan

Tài liệu này mô tả các nguyên tắc quan trọng khi chuyển đổi giao diện Android từ **Dark Mode** sang **Light Mode**. Mục tiêu là đảm bảo giao diện sáng vẫn dễ đọc, không gây chói, giữ được phân cấp thị giác, đồng thời nhất quán với nhận diện thương hiệu và hệ thống theme của Android.

---

## 1. Không đảo màu trực tiếp

Không nên chuyển giao diện tối sang sáng bằng cách “đổi đen thành trắng, trắng thành đen”. Cách này dễ làm giao diện bị chói, mất phân cấp và khó đọc.

### Không nên

```text
Dark mode:
Nền: #000000
Chữ: #FFFFFF

Light mode:
Nền: #FFFFFF
Chữ: #000000
```

### Nên dùng hệ màu có kiểm soát

```text
Light mode:
Nền chính: #FFFFFF hoặc #FAFAFA
Nền thẻ/card: #FFFFFF
Chữ chính: #1C1B1F
Chữ phụ: #5F5E66
Đường viền: #E0E0E0
```

---

## 2. Giữ nguyên nhận diện thương hiệu

Màu thương hiệu như xanh, cam, tím, đỏ không nên thay đổi hoàn toàn khi chuyển sang giao diện sáng. Tuy nhiên, cần điều chỉnh độ đậm, độ tương phản và sắc độ để phù hợp với nền sáng.

### Ví dụ

```text
Dark mode:
Primary: #BB86FC

Light mode:
Primary: #6750A4
```

Màu chính trong Light Mode thường cần **đậm hơn** để nổi bật trên nền sáng.

---

## 3. Nền sáng không nhất thiết phải trắng tuyệt đối

Nền trắng hoàn toàn `#FFFFFF` có thể gây chói, đặc biệt nếu ứng dụng có nhiều nội dung. Có thể dùng nền trắng hơi xám để dễ nhìn hơn.

### Gợi ý

```text
Background: #FAFAFA
Surface/Card: #FFFFFF
Border/Divider: #E5E5E5
```

Trong Android, nên phân biệt rõ:

```text
background: nền tổng thể
surface: nền của card, dialog, bottom sheet
primary: màu chính
secondary: màu phụ
```

---

## 4. Tăng độ rõ của đường viền và bóng

Trong giao diện tối, card thường được phân biệt bằng màu nền sáng hơn một chút. Nhưng trong giao diện sáng, các vùng trắng dễ bị hòa vào nhau, vì vậy cần dùng:

```text
Border nhẹ
Shadow nhẹ
Elevation
Màu nền khác biệt rất nhỏ
```

### Ví dụ

```text
Màn hình: #F8F9FA
Card: #FFFFFF
Border: #E0E0E0
Shadow: alpha thấp
```

---

## 5. Kiểm soát độ tương phản chữ

Chữ trên nền sáng cần đủ đậm, nhưng không nên dùng đen tuyệt đối quá nhiều vì dễ gây cảm giác nặng.

### Gợi ý

```text
Chữ chính: #1C1B1F
Chữ phụ: #49454F
Chữ mờ/placeholder: #79747E
Chữ bị vô hiệu hóa: #A0A0A0
```

### Nguyên tắc phân cấp chữ

```text
Tiêu đề: đậm nhất
Nội dung chính: rõ ràng
Mô tả phụ: nhạt hơn
Placeholder: nhạt nhất
```

---

## 6. Điều chỉnh icon cho phù hợp nền sáng

Icon trong Dark Mode thường dùng màu trắng hoặc xám sáng. Khi chuyển sang Light Mode, cần đổi sang màu tối hơn.

### Ví dụ

```text
Dark mode icon: #FFFFFF hoặc #E6E1E5
Light mode icon: #49454F hoặc #1C1B1F
```

Icon phụ không nên quá đậm, tránh cạnh tranh với nội dung chính.

---

## 7. Trạng thái nút phải rõ ràng

Các trạng thái của button cần được thiết kế lại cho nền sáng.

```text
Primary button:
Nền: màu chính
Chữ: trắng

Secondary button:
Nền: trong suốt hoặc màu rất nhạt
Viền: màu chính
Chữ: màu chính

Disabled button:
Nền: xám nhạt
Chữ: xám trung bình
```

### Ví dụ

```text
Enabled: #6750A4
Pressed: #5B4596
Disabled background: #E0E0E0
Disabled text: #9E9E9E
```

---

## 8. Tránh dùng màu quá rực trên nền sáng

Màu rực nhìn tốt trên nền tối nhưng có thể gây chói trên nền sáng. Khi chuyển sang Light Mode, nên giảm độ bão hòa hoặc dùng màu đậm hơn.

### Ví dụ

```text
Sai:
Nền sáng + màu neon xanh lá

Nên:
Nền sáng + màu xanh đậm hoặc xanh trung tính
```

---

## 9. Hình ảnh và minh họa cũng cần kiểm tra lại

Một số ảnh, logo hoặc minh họa được thiết kế cho nền tối có thể không nổi bật trên nền sáng.

Cần kiểm tra:

```text
Logo có bị mất nét không?
Icon trắng có biến mất trên nền sáng không?
Ảnh có quá sáng không?
Minh họa có còn đủ tương phản không?
```

Nếu cần, tạo hai phiên bản asset:

```text
logo_light.png
logo_dark.png
```

---

## 10. Dùng hệ thống theme thay vì hard-code màu

Trong Android, không nên viết màu trực tiếp trong từng màn hình.

### Không nên

```xml
android:textColor="#000000"
android:background="#FFFFFF"
```

### Nên dùng theme attribute

```xml
android:textColor="?attr/colorOnBackground"
android:background="?attr/colorBackground"
```

### Với Jetpack Compose

```kotlin
MaterialTheme.colorScheme.background
MaterialTheme.colorScheme.onBackground
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.surface
```

Cách này giúp app tự động đổi màu khi chuyển theme.

---

## 11. Quy tắc đặt màu theo Material Design

Với Android hiện đại, nên dùng Material 3 color roles.

```text
primary: màu chính
onPrimary: màu chữ/icon trên primary
background: nền tổng thể
onBackground: chữ/icon trên nền
surface: nền card/dialog/sheet
onSurface: chữ/icon trên surface
error: màu lỗi
onError: chữ/icon trên màu lỗi
outline: đường viền
```

### Ví dụ Light Mode

```text
primary: #6750A4
onPrimary: #FFFFFF
background: #FFFBFE
onBackground: #1C1B1F
surface: #FFFBFE
onSurface: #1C1B1F
outline: #79747E
```

---

## 12. Cần kiểm thử trên nhiều màn hình

Sau khi chuyển sang giao diện sáng, cần kiểm tra lại toàn bộ app:

```text
Màn hình đăng nhập
Trang chủ
Danh sách sản phẩm/bài viết
Chi tiết sản phẩm/bài viết
Form nhập liệu
Dialog
Bottom navigation
Toolbar
Thông báo lỗi
Trạng thái loading/empty/error
```

Đặc biệt chú ý các thành phần:

```text
TextField
Button
Card
RecyclerView item
Dialog
Snackbar
BottomSheet
TabLayout
NavigationBar
```

---

## Bảng tóm tắt nguyên tắc

| Thành phần | Dark Mode | Light Mode |
|---|---|---|
| Nền chính | Xám/đen đậm | Trắng hoặc xám rất nhạt |
| Card | Sáng hơn nền | Trắng, có border/shadow |
| Chữ chính | Trắng/xám sáng | Đen mềm/xám đậm |
| Chữ phụ | Xám sáng | Xám trung bình |
| Icon | Trắng/xám sáng | Đen mềm/xám đậm |
| Button chính | Màu nổi bật | Màu đậm, đủ tương phản |
| Divider | Xám đậm | Xám nhạt |
| Shadow | Ít dùng | Dùng nhẹ để tách lớp |

---

## Checklist khi chuyển từ Dark Mode sang Light Mode

- [ ] Không đảo màu trực tiếp.
- [ ] Nền sáng không quá chói.
- [ ] Card, dialog, bottom sheet có phân cấp rõ.
- [ ] Chữ chính và chữ phụ đủ tương phản.
- [ ] Icon phù hợp với nền sáng.
- [ ] Button có đủ các trạng thái: enabled, pressed, disabled.
- [ ] Màu thương hiệu được điều chỉnh hợp lý.
- [ ] Không dùng màu neon hoặc màu quá rực trên nền sáng.
- [ ] Logo, ảnh và minh họa vẫn nhìn rõ.
- [ ] Không hard-code màu trong từng view.
- [ ] Dùng theme attribute hoặc MaterialTheme.
- [ ] Kiểm thử trên toàn bộ màn hình chính của ứng dụng.

---

## Kết luận

Khi chuyển từ giao diện tối sang giao diện sáng trong Android, nguyên tắc quan trọng nhất là **giữ phân cấp thị giác, đảm bảo tương phản, tránh đảo màu máy móc và sử dụng theme system của Android**.

Một giao diện sáng tốt cần dễ đọc, không chói, nhất quán với thương hiệu và hoạt động tốt trên mọi màn hình của ứng dụng.
