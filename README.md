# Motel Management System Backend

Backend cho hệ thống quản lý bất động sản/phòng trọ cho thuê. Dự án đang được tổ chức theo hướng Maven multi-module để có thể mở rộng dần thành nhiều service độc lập.

## Thành phần chính

| Thành phần | Vai trò |
| --- | --- |
| `shared/` | Module dùng chung, chứa base controller/service/repository/entity, mapper, DTO phân trang, exception handling, JPA auditing và UUIDv7 generator. |
| `user-service/` | Service quản lý hồ sơ người dùng, expose API tại `/api/v1/user-profiles`. |
| `deployment/` | Cấu hình hạ tầng local bằng Docker Compose. Hiện đang chạy PostgreSQL cho `user-service`. |
| `document/` | Tài liệu dự án. Xem chi tiết cấu trúc tại [`document/structure.md`](./document/structure.md). |

## Công nghệ sử dụng

- Java 25
- Spring Boot 4.0.6
- Spring WebMVC
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven multi-module
- MapStruct
- Lombok
- Spotless + Palantir Java Format
- Docker Compose
- Taskfile

## Yêu cầu môi trường

Cần cài sẵn:

- JDK 25
- Docker và Docker Compose
- Task

Repo có Maven Wrapper nên không bắt buộc cài Maven global.

## Hướng dẫn chạy nhanh

### 1. Build module dùng chung

`user-service` phụ thuộc vào `shared`, nên cần build `shared` trước:

```bash
task build_shared
```

Lệnh tương đương:

```bash
./mvnw -pl shared -am clean install -DskipTests
```

Trên Windows PowerShell:

```powershell
.\mvnw.cmd -pl shared -am clean install -DskipTests
```

### 2. Chạy database local

```bash
task start_infra
```

Docker Compose sẽ chạy PostgreSQL:

| Thuộc tính | Giá trị |
| --- | --- |
| Container | `user-db` |
| Database | `user-db` |
| Username | `admin` |
| Password | `admin@123` |
| Port | `localhost:15432` |

### 3. Chạy `user-service`

```bash
task run_user
```

Service chạy tại:

```text
http://localhost:8091
```

Base API:

```text
http://localhost:8091/api/v1/user-profiles
```

## Lệnh hữu ích

| Lệnh | Mục đích |
| --- | --- |
| `task build_shared` | Build và install module `shared`. |
| `task start_infra` | Chạy hạ tầng local bằng Docker Compose. |
| `task stop_infra` | Dừng và xóa container hạ tầng local. |
| `task restart_infra` | Restart hạ tầng local. |
| `task run_user` | Build `shared`, sau đó chạy `user-service`. |
| `task format` | Kiểm tra format bằng Spotless. |
| `task format_fix` | Tự sửa format bằng Spotless. |

## Ghi chú phát triển

- `user-service` scan cả package `com.motel.user_service` và `sharing`, vì service cần dùng bean/config từ module `shared`.
- Schema database được quản lý bằng Flyway trong `user-service/src/main/resources/db/migration`.
- Hibernate đang cấu hình `ddl-auto: none`, nên thay đổi entity cần đi kèm migration.
- Delete mặc định trong base service là soft delete qua field `deletedAt`.
- Kafka đã có block mẫu trong Docker Compose nhưng đang được comment, nên chưa phải dependency bắt buộc khi chạy local.

## Tài liệu

- [`document/structure.md`](./document/structure.md): mô tả cấu trúc thư mục, module và các file quan trọng.
- [`document/high-level-design.md`](./document/high-level-design.md): tài liệu thiết kế mức cao.
- `document/backend-design.docx`: tài liệu backend design dạng Word.

© 2026 Motel Management System.
