# User Service

`user-service` manages user profile metadata, staff permissions, ABAC policies, FCM token metadata, and internal claim projection for SRMS.

## Key Rules Applied

- Public API contracts (DTOs), enums, constants, and domain error codes are centralized in `shared`.
- `user-service` keeps only service-owned domain code: entity, repository, mapper, service workflow, controller, auth adapter.
- APIs remain under `/api/v1/...`.

## Main Endpoints

- `POST /api/v1/auth/register`
- `GET /api/v1/users/me`
- `PUT /api/v1/users/me`
- `POST /api/v1/users/me/fcm-token`
- `DELETE /api/v1/users/me/fcm-token/{id}`
- `PUT /api/v1/users/me/zalo-uid`
- `GET /api/v1/landlords/{landlordId}/staff`
- `POST /api/v1/landlords/{landlordId}/staff`
- `PUT /api/v1/landlords/{landlordId}/staff/{staffId}/permissions`
- `DELETE /api/v1/landlords/{landlordId}/staff/{staffId}`
- `GET /api/v1/abac/policies`
- `POST /api/v1/abac/policies`
- `DELETE /api/v1/abac/policies/{id}`
- `GET /api/v1/internal/users/{keycloakId}/claims`

## Flyway Migrations

- `V1__init_user_profile.sql`
- `V2__create_user_building_permissions.sql`
- `V3__create_abac_policies.sql`
- `V4__create_fcm_tokens.sql`
- `V5__create_auth_audit_logs.sql`
