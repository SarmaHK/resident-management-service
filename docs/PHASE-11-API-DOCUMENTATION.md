# Phase 11 — API & Documentation Verification

## Objective
Verify Swagger/OpenAPI documentation, endpoint alignments, schemas, validation, dependencies, authentication, and provide Postman API evidence for the Resident Management Service.

## 1. OpenAPI Configuration
OpenAPI v3 and Swagger UI have been integrated via `springdoc-openapi-starter-webmvc-ui`. 
- **Endpoint**: Local instances host the interactive API documentation at `/swagger-ui.html`.
- **Global Security**: A `BearerAuth` security requirement was added. Users must inject the Gateway-issued RS256 JWT using the "Authorize" button to access endpoints interactively.

## 2. API Contract Alignments (Endpoints)
The REST interfaces conform strictly to the canonical paths assigned to the resident service:

### Profile Management (`/api/v1/profiles/me`)
- `GET /api/v1/profiles/me`: Returns the authenticated resident's profile (`ProfileResponse`).
- `PUT /api/v1/profiles/me`: Edits the authenticated resident's profile fields (Accepts `ProfileRequest`).
- `POST /api/v1/profiles/me/email-change`: Initiates an asynchronous email update flow (`EmailChangeRequest`).

### Relationship Management (`/api/v1/relationships`)
- `POST /api/v1/relationships`: Creates a lease/ownership relationship with a unit (`RelationshipRequest`).
- `GET /api/v1/relationships/me`: Retrieves all relationships assigned to the authenticated user.

### Administrative Management (`/api/v1/residents`)
- `GET /api/v1/residents`: Retrieves all profiles (Requires `APARTMENT_MANAGER` / `SYSTEM_ADMIN`).
- `POST /api/v1/residents`: Manually provisions a new resident profile.
- `GET /api/v1/residents/{residentId}`: View a resident's profile (Accessible by the resident themself or an Admin).
- `PUT /api/v1/residents/{residentId}`: Edit a resident's profile.
- `PATCH /api/v1/residents/{residentId}/status`: Blocks state manipulation since `status` was removed from the domain model per Phase 5 canonical constraints. Returns `501 Not Implemented`.

## 3. Authentication & Roles
- **Identity Enforcement**: This service operates as a stateless Resource Server mapping RS256 JWTs verified against the API Gateway's public key.
- **Roles**: 
  - Standard endpoints require the JWT `type` to be `user` (to extract the `subject` UUID).
  - Administrative endpoints explicitly check for `"roles": ["APARTMENT_MANAGER"]` or `"roles": ["SYSTEM_ADMIN"]` inside the JWT payload via `@PreAuthorize`.

## 4. Request / Response Schemas
Schemas have been annotated via `@Schema` to document:
- Valid examples (e.g., UUID format for User IDs).
- Constraints (`@NotBlank`, `@Email`).
- DTOs including: `ProfileRequest`, `ProfileResponse`, `RelationshipRequest`, `RelationshipResponse`, `ResidentRequest`, `ResidentResponse`, `EmailChangeRequest`.

## 5. Validation and Status Codes
`@ControllerAdvice` wraps all constraint exceptions into standard responses:
- **`200 OK`**: Generic success.
- **`201 Created`**: Asset successfully generated.
- **`202 Accepted`**: Asynchronous task dispatched (Email changes).
- **`400 Bad Request`**: DTO failed `@Valid` regex or null constraints.
- **`401 Unauthorized`**: Missing, expired, or tampered JWT.
- **`403 Forbidden`**: Valid JWT, but lacking administrative `@PreAuthorize` roles, or attempting to fetch another user's profile ID directly.
- **`404 Not Found`**: Profile/Resident UUID not present in the database.
- **`501 Not Implemented`**: Deliberately disabled routes.

## 6. Dependencies & Failure Behavior
- **Database (MySQL)**: Handled transactionally via JPA. If unreachable, service throws `500 Internal Server Error` DataAccessExceptions.
- **Gateway (Public Key)**: If the API gateway public key is malformed or inaccessible during application boot, the service will crash `(IllegalStateException: Failed to load JWT public key)`.
- **Identity/Property (Simulated)**: Remote endpoints (e.g., emitting identity verification) are stubbed via standard SLF4J audit logs in this phase until Feign Clients are completely integrated.

## 7. Postman API Evidence
A complete Postman Collection is included in the repository root:
`Resident-Management-Service.postman_collection.json`
It demonstrates usage of all paths, utilizing pre-configured environments (`{{base_url}}`, `{{jwt_token}}`) for smooth import and execution against the development environment.
