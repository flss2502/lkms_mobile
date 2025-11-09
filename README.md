# 🧪 Hệ thống Quản lý Tri thức Phòng thí nghiệm (LKMS)

**LKMS (Laboratory Knowledge Management System)** là một **ứng dụng Android solo-project** được xây dựng bằng **Java**, **SQLite**, và **Firebase**, tích hợp **Google Drive API** để hiện đại hóa việc quản lý và lưu trữ dữ liệu trong phòng thí nghiệm.

---

## 🧭 Giới thiệu

**LKMS** là một nền tảng kỹ thuật số toàn diện được thiết kế để thay thế sổ tay phòng thí nghiệm bằng giấy và việc lưu trữ dữ liệu phân mảnh.  
Dự án này cung cấp một hệ thống tập trung giúp:
- Quản lý quy trình thí nghiệm.  
- Theo dõi tồn kho.  
- Ghi chép kết quả.  
- Đặt lịch thiết bị.  

Mục tiêu của dự án là minh họa khả năng xây dựng một ứng dụng Android phức tạp, kết hợp:
- Cơ sở dữ liệu quan hệ (SQLite - 7 bảng).  
- Tích hợp API bên ngoài (Google Drive).  
- Áp dụng kiến trúc **MVVM** một cách chuẩn mực.

---

## 🚀 Tính năng chính

### 🔐 Xác thực (Authentication)
- Đăng nhập / Đăng ký bằng Email & Mật khẩu (kèm xác thực email).
- Đăng nhập nhanh với **Google Sign-In** (Firebase Authentication).

### 📊 Bảng điều khiển (Dashboard)
- Hiển thị thông tin tổng quan khi đăng nhập.
- Danh sách **Thí nghiệm đang chạy** (cá nhân).
- **Cảnh báo tồn kho** (vật tư sắp hết hàng).
- **Lịch đặt thiết bị** sắp tới (toàn bộ lab).

### 📦 Quản lý Tồn kho (Inventory)
- Giao diện **CRUD** đầy đủ (Thêm, Sửa, Xóa vật tư & hóa chất).  
- Cập nhật **real-time** với `LiveData` và `MediatorLiveData`.  
- Hỗ trợ **tìm kiếm & lọc** theo thời gian thực.

### 📓 Sổ tay Kỹ thuật số (ELN - Notebook)
- Quản lý danh sách **Thí nghiệm (Experiments)** cá nhân.  
- Màn hình chi tiết hiển thị:
  - Danh sách ghi chú cũ (RecyclerView).  
  - **Trình soạn thảo Rich Text Editor** (giống Word).  
- Ghi chú được lưu dưới dạng **HTML** cùng **timestamp** trong SQLite.

### 📂 Thư viện Quy trình (SOPs & Protocols)
- Phân loại theo **Tab (SOPs / Protocols)**.  
- Hỗ trợ:
  - Dán **link tài liệu** (Google Doc, link web).  
  - **Tải file** (PDF, Word) từ điện thoại.  
- **Tích hợp Google Drive API**:
  - Upload file an toàn lên Drive người dùng.  
  - Lưu `webViewLink` (URL) vào SQLite.  
- Tự động mở file bằng ứng dụng tương ứng (`Intent.ACTION_VIEW`).

### 🗓️ Đặt thiết bị (Booking)
- Giao diện **CalendarView** chọn ngày.  
- Hiển thị danh sách lịch đặt theo ngày.  
- Chức năng:
  - **Thêm / Hủy lịch đặt** (kiểm tra quyền).  
  - Chọn thiết bị, giờ bắt đầu/kết thúc (`TimePickerDialog`).  
  - Tự động làm mới danh sách khi thay đổi.

### 👤 Quản lý Tài khoản
- Hiển thị thông tin: Ảnh đại diện, Tên, Email, Vai trò (từ Google + SQLite).  
- **Đăng xuất** khỏi Firebase & Google.  
- **Xóa tài khoản**: Xóa toàn bộ dữ liệu cá nhân (Experiments, Notes, Bookings, User) khỏi SQLite.

---

## 🛠️ Kiến trúc & Công nghệ sử dụng

| Thành phần | Mô tả |
|-------------|--------|
| **Ngôn ngữ** | Java |
| **Nền tảng** | Android (XML Layouts) |
| **Kiến trúc** | MVVM (ViewModel, Repository, LiveData, MediatorLiveData, ViewBinding) |
| **Điều hướng** | Android Navigation Component (NavGraph, NavController) |
| **CSDL Cục bộ** | SQLite (7 bảng: Users, Experiments, LabNotes, Inventory, Protocols, Equipment, Bookings) |
| **Xác thực** | Firebase Authentication (Email/Password & Google Sign-In) |
| **Lưu trữ File** | Google Drive API |
| **Thiết kế giao diện** | Material Design 3 |

### 🔗 Thư viện chính
- **Glide** – Hiển thị hình ảnh.  
- **jp.wasabeef:richeditor-android** – Trình soạn thảo Rich Text.  
- **com.google.api-client:google-api-services-drive** – Làm việc với Google Drive.  
- **Material Components** – CardView, Button, Switch, v.v.

---

## ⚙️ Cài đặt & Chạy dự án

### 1️⃣ Clone Repository
```bash
git clone https://github.com/flss2502/lkms_mobile.git
```

### 2️⃣ Mở bằng Android Studio
Sử dụng phiên bản Android Studio Iguana hoặc mới hơn.

### 3️⃣ Kết nối Firebase
1.  Vào **Firebase Console**.
2.  Tạo project mới và thêm app Android (với package name `com.example.lkms`).
3.  Kích hoạt **Authentication** → bật 2 phương thức:
    * Email/Password
    * Google Sign-In
4.  Tải file `google-services.json` và đặt trong thư mục `app/`.

### 4️⃣ Kích hoạt Google Drive API
1.  Truy cập **Google Cloud Console**.
2.  Chọn project Firebase vừa tạo.
3.  Vào **APIs & Services** → **Library**.
4.  Tìm và **Bật (Enable)** "Google Drive API".

### 5️⃣ Cập nhật strings.xml
1.  Lấy `Web Client ID` (dành cho Google Sign-In) từ file `google-services.json` hoặc cài đặt Firebase.
2.  Cập nhật trong `app/src/main/res/values/strings.xml`:

    ```xml
    <string name="default_web_client_id">[YOUR_WEB_CLIENT_ID]</string>
    ```

### 6️⃣ Build & Run
1.  Nhấn **Sync Now** trong Gradle để tải dependencies.
2.  Chọn **Run ▶️** để cài đặt ứng dụng.
3.  💡 **Lưu ý:** Gỡ cài đặt ứng dụng cũ trước khi chạy lần đầu để đảm bảo `DatabaseHelper` (v7) khởi tạo và `seedDatabase()` (chèn dữ liệu mẫu).

---

## 👨‍💻 Tác giả

* **[Xuanbinh]** — Nhà phát triển duy nhất của dự án.
* 🔗 [GitHub](https://github.com/flss2502)
* 🔗 [LinkedIn](https://linkedin.com/in/binhdx)

---

## 📜 Giấy phép

Dự án này được phát hành dưới giấy phép **MIT License**. Bạn có thể tự do sử dụng, chỉnh sửa và phân phối lại code.

© 2025 - Hệ thống Quản lý Tri thức Phòng thí nghiệm (LKMS)
