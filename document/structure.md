# Cấu trúc chi tiết dự án (Detailed Project Structure)

Tài liệu này mô tả chi tiết và đầy đủ nhất về cấu trúc thư mục, các package và vai trò của từng file trong hệ thống Backend.

## 📂 Sơ đồ cấu trúc toàn diện (Comprehensive Directory Tree)

```text
backend/
├── .mvn/                        # Maven Wrapper - Đảm bảo phiên bản Maven thống nhất
├── deployment/                  # Quản lý triển khai và hạ tầng Docker
│   └── docker-compose/          # Các kịch bản chạy hạ tầng
│       └── infra.yml            # Khởi tạo Postgres 18 & Kafka 4.2
├── document/                    # Lưu trữ tài liệu thiết kế và quy trình
│   ├── structure.md             # [Tài liệu hiện tại]
│   └── high-level-design.md     # Tài liệu thiết kế hệ thống mức cao
├── shared/                      # Module Foundation (Thư viện dùng chung)
│   ├── src/main/java/sharing/
│   │   ├── base/                # KIẾN TRÚC CỐT LÕI (Abstract Classes)
│   │   │   ├── controller/      # BaseController: Xử lý CRUD mặc định
│   │   │   ├── entity/          # BaseEntity: Audit fields (CreatedBy, UpdatedAt,...)
│   │   │   ├── exception/       # Các lỗi logic cơ bản
│   │   │   ├── mapper/          # Cấu hình mapping chung
│   │   │   ├── repository/      # Interface Repository dùng chung
│   │   │   └── service/         # Interface & Implementation Service cơ sở
│   │   ├── config/              # CẤU HÌNH HỆ THỐNG
│   │   │   ├── JacksonConfig    # Định dạng JSON trả về (Date, Null handling)
│   │   │   └── SecurityConfig   # (Planned) Cấu hình bảo mật tập trung
│   │   ├── constant/            # Định nghĩa các Enum, Status, API Paths
│   │   ├── dto/                 # DATA TRANSFER OBJECTS (Dùng cho toàn hệ thống)
│   │   │   ├── ApiResponse      # Chuẩn hóa dữ liệu JSON trả về Client
│   │   │   ├── PagedResponse    # Chuẩn hóa dữ liệu khi có phân trang
│   │   │   └── user-service/    # Các DTO dùng chung cho User Service
│   │   ├── exception/           # XỬ LÝ LỖI TẬP TRUNG
│   │   │   ├── AppException     # Custom Exception chính của hệ thống
│   │   │   └── GlobalHandler    # Bắt mọi ngoại lệ và trả về lỗi rõ ràng
│   │   └── utils/               # Công cụ bổ trợ (String, Date, Reflection)
│   └── pom.xml                  # Quản lý dependencies của module shared
├── user-service/                # Microservice: Quản lý người dùng & hồ sơ
│   ├── src/main/java/com/motel/user_service/
│   │   ├── controller/          # Tiếp nhận các yêu cầu HTTP liên quan User
│   │   ├── dto/                 # Các yêu cầu/phản hồi đặc thù của User
│   │   │   └── user_profile/    # DTO riêng cho UserProfile
│   │   ├── entity/              # Định nghĩa bảng UserProfile trong DB
│   │   ├── mapper/              # Chuyển đổi UserProfile <-> DTO
│   │   ├── repository/          # Truy vấn dữ liệu từ bảng UserProfile
│   │   └── service/             # Xử lý logic nghiệp vụ cho User
│   │       └── impl/            # Implementation chi tiết của Service
│   └── pom.xml                  # Dependencies: Spring Web, JPA, Postgres, Shared
├── pom.xml                      # ROOT POM: Quản lý tập trung version cho toàn bộ project
├── Taskfile.yml                 # Hệ thống Automation: Build, Run, Format code
└── README.md                    # Cổng vào của dự án (Hướng dẫn nhanh)
```

## 📝 Phân tích chi tiết các thành phần quan trọng

### 1. Kiến trúc Base (trong module `shared`)
Đây là phần quan trọng nhất giúp hệ thống đồng nhất và dễ mở rộng:
- **`BaseController`**: Cung cấp sẵn 6 API chuẩn (Create, GetById, GetList, Search, Update, Delete). Khi tạo một controller mới (như `UserProfileController`), bạn chỉ cần kế thừa và sẽ có ngay các API này mà không cần viết code.
- **`BaseEntity`**: Tự động quản lý các trường như `id` (UUID), `createdAt`, `updatedAt` cho mọi bảng trong cơ sở dữ liệu.

### 2. Luồng xử lý dữ liệu chuẩn
Khi một yêu cầu đến `user-service`:
1. **Controller**: Tiếp nhận và validate dữ liệu từ `UserProfileRequest`.
2. **Service**: Xử lý logic (kiểm tra quyền, dữ liệu hợp lệ,...).
3. **Mapper**: Chuyển DTO sang Entity để lưu vào DB.
4. **Repository**: Thực hiện thao tác với database Postgres thông qua Hibernate.
5. **Response**: Dữ liệu được Mapper chuyển ngược lại thành `UserProfileResponse` và bọc trong `ApiResponse` trước khi gửi về Client.

### 3. Hệ thống Automation (`Taskfile.yml`)
Thay vì phải nhớ hàng chục lệnh Maven phức tạp, bạn chỉ cần sử dụng các lệnh rút gọn:
- `task build_shared`: Tự động dọn dẹp và cài đặt module shared.
- `task run_user`: Tự động khởi chạy service người dùng.
- `task format`: Đảm bảo code của mọi thành viên trong team đều sạch và đúng quy chuẩn chung.

### 4. Quản lý Dependencies (Root `pom.xml`)
Sử dụng `dependencyManagement` để đảm bảo tất cả các microservices đều sử dụng cùng một phiên bản của các thư viện (như Spring Boot 4, MapStruct, Lombok), tránh xung đột phiên bản khi triển khai.

---
[Quay lại README tổng](../README.md)
