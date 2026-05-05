# User Service

Service quản lý thông tin người dùng và hồ sơ cá nhân (User Profile) trong hệ thống.

## 🚀 Tính năng chính

- Quản lý hồ sơ người dùng (`UserProfile`).
- Hỗ trợ tìm kiếm và lọc dữ liệu động.
- Tích hợp với `shared` module để chuẩn hóa dữ liệu đầu ra.

## 🛠️ API Endpoints

Service sử dụng base path được định nghĩa trong `UserSerivceConstant.USER_PROFILES_API` (thường là `/api/v1/user-profiles`).

Các endpoints mặc định:

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/` | Tạo mới hồ sơ người dùng. |
| `GET` | `/{id}` | Lấy thông tin hồ sơ theo ID. |
| `GET` | `/search` | Tìm kiếm hồ sơ (Hỗ trợ phân trang và filter động). |
| `GET` | `/` | Lấy danh sách tất cả hồ sơ. |
| `PUT` | `/{id}` | Cập nhật hồ sơ. |
| `DELETE` | `/{id}` | Xóa hồ sơ. |

## 🏗️ Kiến trúc Layer

1. **Controller**: `UserProfileController` - Kế thừa từ `BaseController`.
2. **Service**: `UserProfileService` & `UserProfileServiceImpl`.
3. **Repository**: `UserProfileRepository`.
4. **Entity**: `UserProfile`.
5. **Mapper**: `UserProfileMapper` (Sử dụng MapStruct).
6. **DTO**: `UserProfileRequest` & `UserProfileResponse`.

## ⚙️ Cấu hình Database

- **PostgreSQL**: Kết nối tới database `user-db`.
- **Flyway/Liquibase**: (Nếu có) Quản lý migration schema.

---
[Quay lại README tổng](file:///e:/Personal/project/backend/README.md)
