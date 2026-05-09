# Plan: Rewrite `user-service` Thành Service Đầy Đủ Theo SRMS

## Summary

- Tạo plan file tại `document/plan/user-service-rebuild-plan.md`, rồi rewrite `user-service` theo hướng “full bounded”: đầy đủ domain/API/schema/service logic cho User Service, nhưng Keycloak/Redis/Kafka chỉ đi qua adapter interface + local no-op/mock implementation để repo hiện tại vẫn build/test được.
- Reset migration `V1__init_user_profile.sql` vì repo đang dev và đã chọn bỏ code/schema hiện tại.
- Không đổi kiến trúc microservices, không thêm cross-service DB FK, không phụ thuộc infra Keycloak/Redis/Kafka thật trong phase này.
- Không dùng generic `BaseController/BaseServiceImpl` cho user-service mới vì các workflow user/auth/permission/audit cần validation, audit và transition explicit.
- Giữ `shared` cho exception, mapper config, pagination/base infra hiện có; xóa hoặc ngừng dùng DTO user cũ trong `shared.dto.user_service`.

## Key Changes

### Architecture

- Replace current generic CRUD `UserProfileController/UserProfileServiceImpl` with explicit controllers/services:
  - `AuthController`: landlord registration.
  - `CurrentUserController`: `/users/me`, profile update, Zalo UID, FCM token.
  - `LandlordStaffController`: staff list/invite/update permissions/deactivate.
  - `AbacPolicyController`: CRUD-lite for ABAC policies.
  - `InternalClaimsController`: internal claims endpoint for future Keycloak protocol mapper.
- Add service-local DTOs under `com.motel.user_service.dto`; do not expose JPA entities.
- Add domain enums:
  - `UserRole`: `LANDLORD`, `MANAGER`, `ACCOUNTANT`, `TENANT`, `SYSTEM`.
  - `SensitivityClearance`: `STANDARD`, `ELEVATED`.
  - `PolicyEffect`: `ALLOW`, `DENY`.
  - `AuditEventType`: login/permission/account lifecycle events from design.
  - `AuditResult`: `SUCCESS`, `DENIED`, `ERROR`.
  - `DeviceType`: `ANDROID`, `IOS`, `WEB`.
- Add adapter interfaces with bounded implementations:
  - `IdentityProviderClient`: create/disable Keycloak users; local implementation returns generated external IDs and never stores passwords.
  - `PermissionCache`: invalidate permission cache; local no-op implementation.
  - `UserEventPublisher`: publish `user.created` or permission-change events later; local no-op implementation.
  - `PiiCryptoService`: encrypt/hash/mask sensitive values; test/local config must not log plaintext PII.

### Database

- Rewrite `V1__init_user_profile.sql` to create:
  - `user_profiles`
  - `user_building_permissions`
  - `abac_policies`
  - `fcm_tokens`
  - `auth_audit_logs`
- Use current repo soft-delete convention:
  - `deleted_at BIGINT NOT NULL DEFAULT 0`
  - active rows use `deleted_at = 0`
  - append-only `auth_audit_logs` has no soft delete.
- Do not extend `BaseEntity` for append-only/audit entities. Use explicit fields so user-service is not forced into fake `code` values.
- Store external building references as plain `UUID` only. No FK to `property-service`.
- Store sensitive fields encrypted or hash-backed:
  - phone: ciphertext + hash + masked response.
  - Zalo UID: ciphertext + hash or nullable ciphertext.
  - FCM token: ciphertext + hash for idempotent registration.
- Required DB constraints:
  - unique `keycloak_id`.
  - unique active lower-case email via partial index where `deleted_at = 0`.
  - `role`, `sensitivity_clearance`, `policy_effect`, device/audit enums enforced by checks.
  - active permission uniqueness on `(user_id, building_id)` where `deleted_at = 0`.
  - valid permission date range: `valid_until IS NULL OR valid_from IS NULL OR valid_until >= valid_from`.
  - `auth_audit_logs` is insert-only by service code.

### Public/Internal API

- `POST /api/v1/auth/register`
  - Public landlord registration.
  - Request: email, password, fullName, phone optional, avatarUrl optional.
  - Behavior: validate active email uniqueness, call `IdentityProviderClient`, create LANDLORD profile, audit account creation, return profile/claims summary.
  - Never store password in DB or logs.
- `GET /api/v1/users/me`
  - Uses `ActorContextResolver`, not request body user ID.
  - Bounded mode resolves actor from headers such as `X-Actor-Keycloak-Id` and `X-Request-ID`; future mode can resolve JWT subject.
  - Returns profile, role, landlord context, assigned building permissions, sensitivity clearance.
- `PUT /api/v1/users/me`
  - Allows only safe self-update fields: `fullName`, `avatarUrl`, `phone`, `zaloUid`.
  - Does not allow role, landlordId, active status, sensitivity clearance, or permissions.
- `POST /api/v1/users/me/fcm-token`
  - Registers or reactivates a token idempotently by token hash.
- `DELETE /api/v1/users/me/fcm-token/{id}`
  - Soft-deactivates the actor’s token only.
- `PUT /api/v1/users/me/zalo-uid`
  - Updates encrypted Zalo UID and audit metadata.
- `GET /api/v1/landlords/{landlordId}/staff`
  - LANDLORD-only, returns active MANAGER/ACCOUNTANT staff for that landlord with building permissions.
- `POST /api/v1/landlords/{landlordId}/staff`
  - LANDLORD-only.
  - Allowed roles: `MANAGER`, `ACCOUNTANT`.
  - Creates identity-provider user, profile, initial building permissions, audit entries.
- `PUT /api/v1/landlords/{landlordId}/staff/{staffId}/permissions`
  - LANDLORD-only.
  - Atomic replace of active permission set: soft-delete removed rows, upsert submitted rows, audit old/new permission JSON, invalidate permission cache.
- `DELETE /api/v1/landlords/{landlordId}/staff/{staffId}`
  - LANDLORD-only.
  - Disables identity-provider account, marks `is_active=false`, soft-deletes profile-related permissions/tokens, writes audit.
- `GET /api/v1/abac/policies`
  - LANDLORD-only, returns active system and landlord policies.
- `POST /api/v1/abac/policies`
  - LANDLORD-only, creates custom policy with validated JSON conditions.
- `DELETE /api/v1/abac/policies/{id}`
  - LANDLORD-only, soft-deletes custom policy; system policies cannot be deleted.
- `GET /api/v1/internal/users/{keycloakId}/claims`
  - Internal endpoint for future Keycloak protocol mapper.
  - Returns JWT-claim-shaped data: `sub`, `role`, `landlord_id`, `assigned_buildings`, `permissions`, `sensitivity_clearance`, `is_active`.
  - Protect with an internal-token check when configured; local/test profile may use a no-op internal auth gate.

## Implementation Steps

1. Create `document/plan/user-service-rebuild-plan.md` with this plan content before code changes.
2. Replace user-service DTOs/entities/repositories/mappers/services/controllers with explicit user-service-local packages.
3. Remove user-service dependency on `sharing.dto.user_service.*`; delete old shared user DTOs only after `rg` confirms no references remain.
4. Rewrite migration V1 with the new schema because reset strategy was selected.
5. Update `user-service/pom.xml` only for required dependencies:
   - Keep webmvc, validation, JPA, PostgreSQL, Flyway, MapStruct, Lombok.
   - Add test dependencies for Mockito and Testcontainers PostgreSQL if not already managed.
   - Do not add real Keycloak/Redis/Kafka clients in this bounded phase.
6. Add `application-test.yaml` or dynamic test datasource config so `mvnw -pl user-service test` does not depend on the manually started `user-db`.
7. Add explicit domain error codes implementing `ErrorCode`, for example:
   - `USER_PROFILE_NOT_FOUND`
   - `USER_EMAIL_ALREADY_EXISTS`
   - `USER_INACTIVE`
   - `STAFF_ROLE_INVALID`
   - `LANDLORD_ACCESS_DENIED`
   - `PERMISSION_DATE_RANGE_INVALID`
   - `ABAC_POLICY_SYSTEM_READ_ONLY`
   - `INTERNAL_AUTH_INVALID`
8. Preserve `/api/v1/...` paths and Spring `ProblemDetail` error handling.
9. Apply Spotless formatting after Java edits.

## Test Plan

- Unit tests:
  - landlord registration rejects duplicate active email.
  - landlord registration creates profile, calls identity adapter, does not expose/store password.
  - `/users/me` resolves actor by keycloak ID and rejects inactive/deleted user.
  - self-update cannot mutate role, landlord ID, active status, sensitivity clearance, or permissions.
  - staff invite allows only `MANAGER`/`ACCOUNTANT`.
  - staff permission replace soft-deletes removed permissions and upserts active rows.
  - staff deactivate disables identity provider and soft-deletes related active records.
  - FCM registration is idempotent by token hash.
  - ABAC policy system policy cannot be deleted.
  - PII crypto service masks responses and never returns ciphertext.
- Repository/integration tests with PostgreSQL/Testcontainers:
  - Flyway applies V1 cleanly.
  - active email uniqueness works with `deleted_at = 0`.
  - active `(user_id, building_id)` permission uniqueness works.
  - date-range check rejects invalid permission validity.
  - cross-service `building_id` has no DB FK.
- Verification commands:
  - `mvnw -pl shared clean install`
  - `mvnw -pl user-service test`
  - `mvnw spotless:apply`
  - optional with infra: `task start_infra`, `task run_user`

## Assumptions

- “Bỏ code hiện tại” means rewrite the user-service implementation and reset its V1 migration, not delete the whole Maven module or shared infrastructure.
- User-service IDs remain local UUIDs; `keycloak_id` is the external identity-provider subject used to resolve actors and claims.
- Keycloak, Redis, and Kafka are integration targets, not hard runtime requirements in this phase.
- The API is not production-authenticated yet; actor/internal auth seams are implemented now so Spring Security/JWT can replace header-based bounded resolution later without rewriting business logic.
- `document/plan/user-service-rebuild-plan.md` is the chosen location for the implementation handoff file.
