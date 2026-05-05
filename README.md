# Motel Management System Backend

Hệ thống quản lý bất động sản cho thuê thông minh (Smart Real Estate Management System). Backend được xây dựng theo kiến trúc Microservices, sử dụng Java 25 và Spring Boot 4.

### Chi tiết cấu trúc các thành phần:

- 📦 **[shared/](./shared)**: Module nền tảng của toàn bộ hệ thống.
  - Chứa các lớp **Base** (Controller, Service, Entity), **Config**, **DTOs** và **Exception handling**.
  - **Lưu ý**: Đây là module dùng chung, mọi service khác đều phụ thuộc vào nó. Bạn **phải build module này đầu tiên** để các service khác có thể hoạt động. Xem chi tiết tại [Shared README](./shared/README.md).

- 👤 **[user-service/](./user-service)**: Microservice quản lý người dùng.
  - Phụ trách lưu trữ thông tin cá nhân, hồ sơ và phân quyền.
  - Sử dụng module `shared` để chuẩn hóa các phản hồi API. Xem chi tiết tại [User Service README](./user-service/README.md).

- 🐳 **[deployment/](./deployment)**: Quản lý môi trường triển khai.
  - Chứa các kịch bản **Docker Compose** để khởi tạo cơ sở dữ liệu (PostgreSQL) và các thành phần hạ tầng khác như Kafka.

- 📄 **[document/](./document)**: Hệ thống tài liệu dự án.
  - [Cấu trúc chi tiết (Structure)](./document/structure.md): Mô tả đầy đủ từng file và thư mục.
  - [Thiết kế mức cao (High-level Design)](./document/high-level-design.md): Tài liệu phân tích nghiệp vụ.

---

## 🚀 Development Workflow (Running with Task)

Dự án sử dụng `Taskfile` để tự động hóa quy trình. Hãy thực hiện theo đúng thứ tự sau:

### 1. Build Shared Module (QUAN TRỌNG)

Vì mọi service đều phụ thuộc vào `shared`, bạn phải cài đặt nó vào local Maven repository trước:

```bash
task build_shared
```

_(Tương đương: `mvnw -pl shared clean install`)_

### 2. Khởi chạy hạ tầng (Infrastructure)

Chạy PostgreSQL và Kafka bằng Docker:

```bash
task start_infra
```

_(Tương đương: `docker compose -f deployment/docker-compose/infra.yml up -d`)_

### 3. Chạy các Service

Sau khi build shared và chạy infra, bạn có thể khởi động các service:

```bash
# Chạy User Service
task run_user
```

_(Tương đương: `cd user-service && mvnw spring-boot:run`)_

### 4. Các lệnh hữu ích khác

- **Format code**: `task format` (Sử dụng Spotless)
- **Dừng hạ tầng**: `task stop_infra`
- **Build Docker image**: `task build`

---

## 🛠️ Core Stack

- **Languages**: Java 25, SQL.
- **Frameworks**: Spring Boot 4.0.6, Spring Data JPA, Spring Kafka.
- **Tools**: Maven, Docker, Task, MapStruct, Lombok.

---

© 2026 Motel Management System.
