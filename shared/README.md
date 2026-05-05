# Shared Module

Module `shared` là thư viện nền tảng chứa các thành phần cốt lõi được sử dụng bởi tất cả các microservices trong hệ thống.

## 📦 Package Structure (`sharing`)

- **`base`**: Chứa các lớp trừu tượng và interface cơ sở.
  - `entity`: `BaseEntity` (ID, Audit fields).
  - `repository`: `BaseRepository`.
  - `service`: `BaseService` & `BaseServiceImpl`.
  - `controller`: `BaseController`.
  - `mapper`: Interface mapper chung.
- **`config`**: Các lớp cấu hình dùng chung (Jackson, Serialization, Global CORS).
- **`constant`**: Định nghĩa các hằng số, Enum chung cho toàn hệ thống.
- **`dto`**: Các lớp DTO phản hồi chuẩn:
  - `ApiResponse<T>`: Cấu trúc JSON trả về thống nhất.
  - `PageResponse<T>`: Dữ liệu phân trang.
- **`exception`**: Quản lý lỗi tập trung:
  - `AppException`: Ngoại lệ tùy chỉnh.
  - `GlobalExceptionHandler`: Bắt và xử lý ngoại lệ trả về Client.
- **`utils`**: Các hàm tiện ích bổ trợ.

## 🛠️ Cách sử dụng

Để sử dụng module này trong một service mới, hãy thêm dependency vào `pom.xml` của service đó:

```xml
<dependency>
    <groupId>shared</groupId>
    <artifactId>shared</artifactId>
</dependency>
```

*Lưu ý: Module này phải được build và cài đặt (`mvn install`) trước khi build các service phụ thuộc.*

---
[Quay lại README tổng](../README.md)
