# Phase 7 — Profile & Relationship Functionality

## 1. Objective
Implement and finalize the Profile and Apartment Relationship functionalities within `resident-management-service`. This phase strictly enforces boundaries with the external Identity and Property services and ensures all logic aligns with the Phase 4 JWT security model and Phase 5 Domain foundation.

## 2. Profile APIs

### `GET /api/v1/profiles/me`
- **Method**: GET
- **Purpose**: Retrieves the profile for the authenticated user.
- **Authorization**: Self-only, derived automatically from JWT `sub` via `SecurityUtils.getCurrentUserId()`.
- **Request DTO**: N/A
- **Response DTO**: `ProfileResponse`
- **Validation**: Enforces that the user must be authenticated.

### `PUT /api/v1/profiles/me`
- **Method**: PUT
- **Purpose**: Updates the permitted, non-system fields of the authenticated user's profile.
- **Authorization**: Self-only, derived automatically from JWT `sub`.
- **Request DTO**: `ProfileRequest` (firstName, lastName, phone).
- **Response DTO**: `ProfileResponse`
- **Validation**: Enforces that `userId` and `profileType` cannot be altered.

## 3. Profile Editing
Only non-system fields such as `firstName`, `lastName`, and `phone` are editable. Protected identifiers are preserved securely.

## 4. Email Change
- **Endpoint**: `POST /api/v1/profiles/me/email-change`
- **Purpose**: Initiates an email change. Because `email` belongs to the Identity domain, this triggers an internal API call to the Identity service via `IdentityClient`.
- **Note**: Locally, this will fail with a 503/500 error because the Identity service is offline, enforcing the rule against faking successful external validations.

## 5. Profile Provisioning Dependency
**Provisioning trigger: BLOCKED**
The approved requirements still do not define whether `profileType` is derived from an Identity role or explicitly provisioned by an administrator. Consequently, automatic profile provisioning is not implemented to avoid inventing unapproved rules.

## 6. Apartment Relationships

### `POST /api/v1/relationships`
- **Method**: POST
- **Purpose**: Submits a new relationship request for a specific unit.
- **Authorization**: Extracted dynamically from JWT.
- **Request DTO**: `RelationshipRequest` (relationshipType, unitReference, supportingInfo).
- **Response DTO**: `RelationshipResponse`
- **Validation**: Enforces strict unit validation via an internal call to the Property service (`PropertyClient`).

### `GET /api/v1/relationships/me`
- **Method**: GET
- **Purpose**: Retrieves all relationships for the authenticated user.
- **Authorization**: Self-only, derived from JWT `sub`.
- **Request DTO**: N/A
- **Response DTO**: `List<RelationshipResponse>`

## 7. Relationship Authorization
All relationship requests are securely tied to the identity represented by the authenticated JWT. The system relies entirely on the Gateway JWT architecture to assert identity.

## 8. Property Service Boundary
To avoid direct database access and duplicate source-of-truth records, unit validation is performed via `PropertyClient`, pointing to `GET http://property-service/api/v1/internal/units/{unitId}/exists`. This fails properly when the Property Service is offline rather than faking success.

## 9. Database
Used MySQL with Flyway migrations. No PostgreSQL or H2 remnants exist. The `ApartmentRelationshipRepository` and `ProfileRepository` rely purely on Spring Data JPA, adhering to the established schema. No cross-service foreign keys exist.

## 10. Security
A strict security regression search was conducted. No `TestAuthController`, `HS256`, `temp-user-id`, or manual password/Identity validations were found. 

## 11. Verification
- Compilation succeeded via `mvnw clean compile`.
- Codebase was verified for security regression.
- Manual verification of cross-service REST calls is limited to observing expected connection refusal errors due to the missing Gateway/Identity/Property ecosystem.

## 12. Known Limitations
- End-to-end runtime validations for Email Change and Relationship Requests will fail with 503/500 locally because the required external services (Identity and Property) are offline.
- Profile provisioning trigger is blocked.
