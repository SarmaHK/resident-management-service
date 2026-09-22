# Phase 3 — Gateway JWT Security

## Objective
Implement a stateless, secure Spring Security architecture that relies on the central Gateway for JWT issuance and verification. The `resident-management-service` must act strictly as a Resource Server, verifying asymmetric (RS256) JWTs signed by the Gateway using a provided public key, extracting user roles, and enforcing role-based access control on business endpoints.

## Existing Problem Discovered
Previously, the `resident-management-service` incorrectly functioned as an Identity Provider. It contained:
- `JwtUtils` and `HS256` symmetric key generation for local tokens.
- Hardcoded test endpoints (`/api/v1/auth/test-token`).
- `temp-user-id` placeholders hardcoded into service logic to bypass authentication context.

These were all completely removed during Phase 2.

## Security Architecture
**Architecture Flow:**
Frontend → Gateway (Authentication/JWT Issuance) → Resident Management Service (Resource Server / JWT Validation)

## JWT Types Supported
1. **User JWT:** Contains `type="user"`, a `sub` claim (the real user ID), and a `roles` array (e.g. `["TENANT", "OWNER"]`).
2. **Service JWT:** Contains `type="service"` and a `sub` claim representing the calling service name.

## Gateway Trust Model
- The service explicitly **trusts only tokens signed by the Gateway**.
- Trust is established by decoding the JWT using the `GATEWAY_JWT_PUBLIC_KEY` provided via environment variables.
- Private keys are **not** stored or loaded within this service.

## Configuration
- Added `spring-boot-starter-security` and `spring-boot-starter-oauth2-resource-server`.
- Configured `.env` and `application.yml` to supply `GATEWAY_JWT_PUBLIC_KEY`.

## Authentication Flow
- Configured `SecurityFilterChain` with `SessionCreationPolicy.STATELESS`.
- Configured a `JwtDecoder` bean that natively parses the provided RSA public key.
- A custom `DelegatingOAuth2TokenValidator` enforces standard timestamp claims (expiration, not-before) alongside a custom validator to ensure the `type` claim is exactly `user` or `service`.

## Authorization Flow & Role Mapping
- A custom `JwtAuthenticationConverter` extracts the `roles` array from the JWT claims and maps them to standard Spring Security `GrantedAuthority` objects (e.g., `ROLE_TENANT`).
- All business endpoints (`/api/v1/profiles/me/**`, `/api/v1/relationships/**`) require valid authentication and explicitly require roles such as `TENANT`, `OWNER`, `RESIDENT`, `APARTMENT_MANAGER`, or `SYSTEM_ADMIN`.
- Any unmapped endpoint defaults to `authenticated()`.

## User Identity Extraction
- The legacy `"temp-user-id"` was stripped from `ProfileService` and `RelationshipService`.
- A new `SecurityUtils.getCurrentUserId()` extracts the user ID directly from `SecurityContextHolder.getContext().getAuthentication().getName()` (which is populated from the JWT `sub` claim).

## Service-to-Service Validation
- The `type` validator ensures that a token is explicitly flagged as `service` or `user`.
- Service tokens can be further role-mapped if required by future service-to-service contracts.

## Removed Insecure Mechanisms
- `temp-user-id` is fully deleted.
- No trace of symmetric `HS256` signing remains.
- No backdoor test endpoints exist.

## Verification Commands
```powershell
.\mvnw.cmd clean compile
```

## Verification Results
- Compilation is successful.
- Spring Security configuration initializes the `NimbusJwtDecoder` and standard filter chains seamlessly.

## Remaining Blockers
- **Runtime Security:** E2E runtime verification (hitting endpoints with Postman or a frontend app) is **BLOCKED** because the identity-access-service and gateway environments are not available in this workspace. The security layer will block all requests with HTTP 401 until a valid signed token is supplied by a live Gateway.
