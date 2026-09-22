# Phase 8 — Validation, Error Handling & Audit

## 1. Objective
Strengthen the `resident-management-service` by introducing comprehensive and standardized DTO validation, consistent API error responses, strict security-safe exceptions, and robust audit logging using the existing SLF4J stack.

## 2. DTO Validation
- **DTOs reviewed**: `ResidentRequest`, `ProfileRequest`, `RelationshipRequest`, `EmailChangeRequest`.
- **Validation Rules added/verified**: Applied `@NotBlank`, `@NotNull`, and `@Email` constraints. Verified that no arbitrary undocumented constraints (e.g., rigid phone patterns or custom business logic) were injected.
- **Allowed Values**: Verified `RelationshipType` enum mappings correctly rely on built-in Java/Jackson parsing, which now triggers a managed `HttpMessageNotReadableException` via the Global Handler if invalid values are passed.

## 3. Error Handling
- **Standard error response**: Introduced `ApiErrorResponse` representing `{timestamp, status, error, message, path, fieldErrors}`.
- **Validation Errors (400)**: Handled `MethodArgumentNotValidException` to extract and populate `fieldErrors` safely.
- **404 Handling**: Caught `ResourceNotFoundException`.
- **409 Handling**: Caught `ConflictException`.
- **400 Handling**: Caught `BadRequestException` and `HttpMessageNotReadableException`.
- **401/403 Handling**: Handled `AccessDeniedException` explicitly (returning 403 Forbidden). Authentication/401 is natively guarded by the `SecurityConfig` OAuth2 Resource Server.
- **Domain Errors**: Managed exclusively through explicit runtime exception boundaries (NotFound/Conflict).
- **External Service Errors**: Caught `RestClientException` throwing 503 instead of a generic 500.
- **Unexpected Errors**: Generic `Exception` fallback returning a sanitized 500 error.

## 4. Security-Safe Errors
- No stack traces exposed to clients.
- No DB credentials, API keys, JWT tokens, or passwords leaked via responses.
- Exceptions explicitly sanitized in `GlobalExceptionHandler`.

## 5. Audit
- **Required audit events**: Maintained existing events: relationship submissions (FR-AUD-004) and profile edits (FR-AUD-006).
- **Actor Identification**: Derived purely from `SecurityUtils.getCurrentUserId()` via the Gateway JWT `sub`.
- **Audit Data**: Standardized `AuditService` to use SLF4J: `AUDIT [{eventCode}] - Actor: {userId} - Action: {details}`.
- **Sensitive-Data Protection**: Logs capture event codes, IDs, and domain-safe actions without leaking authorization headers or PI tokens. A `try/catch` wrapper ensures audit write failures don't disrupt the core business transaction.

## 6. Cross-Service Errors
- Calls to `PropertyClient` and `IdentityClient` via RestTemplate will bubble up standard `RestClientException`s if offline. The GlobalExceptionHandler captures these and returns a clean HTTP 503 Service Unavailable, strictly preserving the boundary and refusing to fake success.

## 7. Verification
- `mvnw clean compile` passed successfully.
- Code-inspected global `log.*` outputs ensuring no raw tokens or passwords are inadvertently logged.
- Verified that `ApiErrorResponse` cleanly serializes to JSON without spilling raw Java class names.

## 8. Known Limitations
- End-to-end integration verifications (e.g., verifying a successful Email Change propagation) remain structurally blocked by the offline status of the Identity and Property microservices in this isolated environment.
- Provisioning of initial profile rules (Phase 5 blocker) remains unaddressed.
