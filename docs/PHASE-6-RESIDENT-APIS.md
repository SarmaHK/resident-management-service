# Phase 6 — Resident APIs

## 1. Objective
Implement and clean the Resident Management APIs according to the approved canonical API contract boundaries, adhering to the Phase 4 JWT security model and Phase 5 Domain foundation. Due to the missing Canonical API registry, Owner, Tenant, and Staff APIs are blocked, but the requested Resident APIs were fully implemented.

## 2. Canonical Endpoints
Below is the status of the implemented Resident endpoints. Owner, Tenant, and Staff APIs are currently BLOCKED because the Canonical API Registry was not found in the workspace, meaning their exact paths, DTOs, and roles cannot be safely verified.

---

## 3. Resident APIs

### `POST /api/v1/residents`
- **Purpose**: Creates a new Resident Profile.
- **Authorization**: `APARTMENT_MANAGER` or `SYSTEM_ADMIN`.
- **Request DTO**: `ResidentRequest` (userId, firstName, lastName, phone, emergencyContact).
- **Response DTO**: `ResidentResponse`.
- **Validation**: Ensures all `@NotBlank` fields are present. Checks if profile already exists for the `userId`.
- **Domain validation**: Boundary enforced. Does not directly query the Identity DB for userId existence due to microservice constraints (and local environment limitations without Gateway).
- **Error behavior**: Returns 400 for bad input, 401/403 for unauthorized, and throws a duplicate check error if the profile already exists.

### `GET /api/v1/residents`
- **Purpose**: Lists all resident profiles.
- **Authorization**: `APARTMENT_MANAGER` or `SYSTEM_ADMIN`.
- **Request DTO**: N/A
- **Response DTO**: List of `ResidentResponse`.
- **Error behavior**: 401/403 for unauthorized.

### `GET /api/v1/residents/{residentId}`
- **Purpose**: Retrieves a specific resident profile by its internal UUID.
- **Authorization**: Admin roles OR the resident themselves (by verifying if JWT `sub` == `userId`).
- **Request DTO**: N/A
- **Response DTO**: `ResidentResponse`.
- **Error behavior**: 403 (Access Denied) if trying to read someone else's profile without admin rights. 404 (Not Found) if ID does not exist.

### `PUT /api/v1/residents/{residentId}`
- **Purpose**: Updates the client-editable fields of a resident.
- **Authorization**: Admin roles OR the resident themselves.
- **Request DTO**: `ResidentRequest`
- **Response DTO**: `ResidentResponse`
- **Validation**: Enforces basic validation, updates only `firstName`, `lastName`, `phone`, and `emergencyContact`. System-managed fields like `userId` and `profileType` are protected.
- **Error behavior**: 403 (Access Denied), 404 (Not Found).

### `PATCH /api/v1/residents/{residentId}/status`
- **Purpose**: Updates the status of a resident.
- **Authorization**: `APARTMENT_MANAGER` or `SYSTEM_ADMIN`.
- **Limitation / Blocked**: The approved Phase 1 schema (`V1__init_schema.sql`) explicitly lacks a `status` field for `profiles`. Per requirements, no status transitions or schema changes were invented. Thus, this endpoint returns `501 Not Implemented`.

## 4. Owner APIs
**BLOCKED** (Canonical API Registry not found)

## 5. Tenant APIs
**BLOCKED** (Canonical API Registry not found)

## 6. Staff APIs
**BLOCKED** (Canonical API Registry not found)

## 7. Identity Service Boundary
- Maintains strict decoupling. 
- Profiles are created with a string reference `userId`.
- No direct database query or cross-service FK to `users` or `identity` table exists. 
- Identity APIs would be called for existence validation in a live gateway ecosystem.

## 8. Cross-Service Validation
- Documented as an expected API call (in `ResidentService`) rather than a direct database query. The local implementation skips the call to avoid failure since the identity service is offline.

## 9. Security
- Relies on Phase 4's Gateway JWT Architecture.
- Used `@EnableMethodSecurity` and `@PreAuthorize` in Spring to enforce method-level role validation. 
- Manual self-authorization logic checks `SecurityUtils.getCurrentUserId()`.

## 10. Database
- `ResidentProfileRepository` created utilizing standard Spring Data JPA over `ResidentProfile`.
- No duplicate schemas or PostgreSQL references introduced.

## 11. Verification
- Compilation succeeds (`mvnw clean compile`).
- Flyway migrations load successfully.
- Code search verifies no rogue hardcoded security keys (`jwt.secret`, `temp-user-id`).

## 12. Known Limitations
- End-to-end verification via API client is blocked because the local Gateway is offline, hence tokens cannot be properly issued.
- Automatic profile provisioning trigger remains blocked.
