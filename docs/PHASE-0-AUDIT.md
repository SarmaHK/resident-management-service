# Resident Management Service — Phase 0 Audit

## 1. Repository Overview
- **Project Structure**: Standard Spring Boot layout (`src/main/java`, `src/main/resources`).
- **Dependencies**: Spring Boot Starter Data JPA, Web, Security, Validation, PostgreSQL runtime, H2 runtime, JJWT (io.jsonwebtoken).
- **Environment config**: `application.yml` configured for H2 in-memory DB, even though PostgreSQL dependency exists in `pom.xml`.

## 2. Current Architecture
- The service currently handles **both** Resident Management (profiles, relationships) **and** Identity Access (users, roles, authentication).
- **Architecture Violation**: It manages `User` and `Role` entities directly, violating the constraint that identity/account ownership belongs to `identity-access-service`.

## 3. Database Audit

### PostgreSQL Dependencies
- `postgresql` runtime dependency is present in `pom.xml`.
- No explicit PostgreSQL dialect configured in `application.yml` (currently uses `H2Dialect`).

### Current Schema
- Located in `src/main/resources/db/migration/V1__init_schema.sql`.
- Contains tables: `roles`, `users`, `user_roles`, `profiles`, `apartment_relationships`, `user_status_history`, `email_verification_tokens`.

### Migration Files
- `V1__init_schema.sql` uses standard SQL but Flyway is disabled in `application.yml` (`flyway.enabled: false`).

### Database Problems
- **Cross-service boundaries violated**: `users` and `roles` tables exist here but should belong to `identity-access-service`.
- **Foreign Keys**: `apartment_relationships` and `profiles` have foreign keys / direct references (`user_id`) to the local `users` table, coupling them together.

## 4. Security Audit

### Current JWT Architecture
- Implemented locally using `jjwt`.
- Extracts `userId` directly from JWT subject.
- Validates token locally using a shared secret.

### HS256 Findings
- `JwtUtils.java` uses `Keys.hmacShaKeyFor(jwtSecret.getBytes())` which is HS256.
- The project requirement is for a different security architecture (likely RS256/Gateway model).

### RS256 Findings
- None found. RS256 and public/private key verification are not implemented.

### Test Token Backdoor
- `TestAuthController` exposes `GET /api/v1/auth/test-token`.
- Automatically creates a `SYSTEM_ADMINISTRATOR` role and an `admin@test.com` user, returning a valid admin JWT. This is a severe security risk.

### SecurityConfig Findings
- `SecurityConfig.java` permits all requests to `/api/v1/auth/**`.
- Enforces `.authenticated()` for all other requests.

### Authentication/Authorization Findings
- Uses local `UserDetailsServiceImpl` to load users from the local `users` table.
- Authorization relies on `@PreAuthorize("hasAuthority('...')")`.

## 5. Profile Audit
- **Entities**: `Profile` (Base), `ResidentProfile`, `OwnerProfile`, `StaffProfile`.
- **Database**: `profiles` table.
- **Service/Controller**: `ProfileController` and `ProfileService`.
- **Provisioning trigger**: **MISSING**. There is no endpoint or service logic to create a profile. User creation in `UserService` does not create a corresponding profile. Profile type assumptions are currently hardcoded or missing.
- **Endpoints**: `GET /api/v1/profiles/me`, `PUT /api/v1/profiles/me`, and email change flows.

## 6. Relationship Audit
- **Entity**: `ApartmentRelationship`.
- **Database**: `apartment_relationships` table.
- **Service/Controller**: `RelationshipController` and `RelationshipService`.
- **Problem**: Holds a hard reference to `user_id`, expecting the user to exist in the local database.

## 7. User/Role Audit
- `UserService`, `RoleService`, `UserController`, `RoleController` manage full CRUD for users and roles.
- **Architectural concern**: This is a direct duplication of responsibilities that belong to `identity-access-service`.

## 8. API Audit

| Method | Endpoint | Current Status | Auth | Role | Notes |
|--------|----------|----------------|------|------|-------|
| GET | `/api/v1/auth/test-token` | Implemented | None | None | POTENTIALLY WRONG (Backdoor to be removed) |
| GET | `/api/v1/users` | Implemented | Req. | SYSTEM_ADMINISTRATOR | EXTRA (Belongs to identity service) |
| POST | `/api/v1/users` | Implemented | Req. | SYSTEM_ADMINISTRATOR | EXTRA (Belongs to identity service) |
| GET | `/api/v1/users/{userId}` | Implemented | Req. | SYSTEM_ADMINISTRATOR | EXTRA (Belongs to identity service) |
| PATCH | `/api/v1/users/{userId}/status` | Implemented | Req. | SYSTEM_ADMINISTRATOR | EXTRA (Belongs to identity service) |
| POST | `/api/v1/users/{userId}/roles` | Implemented | Req. | SYSTEM_ADMINISTRATOR (implied) | EXTRA (Belongs to identity service) |
| DELETE | `/api/v1/users/{userId}/roles/{roleName}` | Implemented | Req. | SYSTEM_ADMINISTRATOR (implied) | EXTRA (Belongs to identity service) |
| GET | `/api/v1/profiles/me` | Implemented | Req. | Any Auth | IMPLEMENTED |
| PUT | `/api/v1/profiles/me` | Implemented | Req. | Any Auth | IMPLEMENTED |
| POST | `/api/v1/profiles/me/email-change` | Implemented | Req. | Any Auth | IMPLEMENTED |
| PUT | `/api/v1/profiles/me/email-change/confirm` | Implemented | Req. | Any Auth | IMPLEMENTED |
| POST | `/api/v1/relationships` | Implemented | Req. | Any Auth | IMPLEMENTED |
| GET | `/api/v1/relationships/me` | Implemented | Req. | Any Auth | IMPLEMENTED |

## 9. Testing Audit
- **Tests**: `0` test files found in `src/test/`.
- No unit, integration, or controller tests exist for `ProfileController`, `RelationshipController`, `UserService`, or `RoleService`.

## 10. CI Audit
- **GitHub Actions**: None. `.github/workflows/` directory does not exist.

## 11. Code Quality Findings
- **Incorrect service boundaries**: Complete implementation of User and Role management.
- **Insecure defaults**: `TestAuthController` backdoor.
- **Missing validation / logic**: Profile provisioning is entirely absent.
- **Database coupling**: Direct coupling of relationships/profiles to local users table.
- **Misleading Configuration**: `pom.xml` includes PostgreSQL, but `application.yml` configures H2 in-memory DB and disables Flyway.

## 12. KEEP / REFERENCE / DELETE / REBUILD

KEEP
- Profile and Relationship DTO structures (reference for contracts).
- Custom exception handling (`GlobalExceptionHandler`).

REFERENCE
- `ProfileService` and `RelationshipService` logic for business rules.
- Existing database migration `V1__init_schema.sql` (to extract profile/relationship schema).

DELETE
- `TestAuthController`.
- `UserService`, `RoleService`, `UserController`, `RoleController` (and related entities/repositories).
- `SecurityConfig` and `JwtUtils` (HS256 local auth implementation).

REBUILD
- Security configuration using the project's Gateway JWT RS256 model.
- Profile provisioning logic (based on BA decision).
- Flyway migrations for MySQL (excluding User/Role tables).

## 13. Known Risks
- Transitioning away from local user tables will require refactoring foreign keys to soft references (`userId` strings without database-level constraints).
- Security refactor will break all current manual testing methods that rely on `/test-token`.

## 14. Open Decisions
- **Profile type provisioning**:
  - Is `profileType` derived from a role provided in the JWT?
  - Or is it explicitly provisioned via an admin endpoint during user onboarding?

## 15. Recommended Clean-Rebuild Sequence
1. Remove all identity/user/role entities, controllers, and services.
2. Remove local JWT generation (`TestAuthController`, `JwtUtils`).
3. Reconfigure `application.yml` and `pom.xml` for MySQL and Flyway.
4. Rewrite Flyway migrations for MySQL, keeping only Profile and Relationship tables.
5. Implement RS256 Gateway JWT security configuration.
6. Rebuild Profile and Relationship services using soft references to `userId`.
7. Add Unit and Integration tests.
8. Set up GitHub Actions CI pipeline.
