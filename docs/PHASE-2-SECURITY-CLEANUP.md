# Phase 2 — Security Cleanup

## 1. Objective
To completely remove the obsolete and insecure legacy authentication architecture from `resident-management-service`. This ensures the application foundation is fully prepped for the target RS256 / Gateway JWT architecture that will be implemented in Phase 3.

## 2. Removed Backdoor
- The `TestAuthController` class and its privileged unauthenticated `/api/v1/auth/test-token` endpoint were entirely removed. 
- The application can no longer generate administrative tokens or bypass authentication through this mechanism.

## 3. Removed Local JWT
The old locally managed JWT logic was entirely removed:
- `JwtUtils` class has been deleted.
- Local `HS256` token signing using `Keys.hmacShaKeyFor` is gone.
- The `jwt.secret` configuration was removed from `application.yml` and `.env.example`.
- `Jwts.builder` token generation code is no longer present.

## 4. Removed Local Identity Authentication
The service no longer attempts to authenticate users via a local database check:
- Local `UserDetailsService` and `UserDetailsImpl` were removed.
- All dependencies on local `User` and `Role` lookup for authentication are gone.

## 5. Security Configuration Cleanup
- `SecurityConfig` and `AuthTokenFilter` were deleted.
- The obsolete behavior that applied a broad `permitAll()` to `/api/v1/auth/**` no longer exists.
- The service currently runs without Spring Security enforcement, awaiting the fresh Phase 3 Gateway JWT implementation.

## 6. Repository Search Results

| Search Item | Result | Action |
|---|---|---|
| TestAuthController | NOT FOUND | Removed in Phase 1 |
| test-token | NOT FOUND | Removed in Phase 1 |
| JwtUtils | NOT FOUND | Removed in Phase 1 |
| HS256 | NOT FOUND | Removed in Phase 1 |
| jwt.secret | NOT FOUND | Removed in Phase 1 |
| hmacShaKeyFor | NOT FOUND | Removed in Phase 1 |
| local UserDetailsService | NOT FOUND | Removed in Phase 1 |
| privileged permitAll | NOT FOUND | Removed in Phase 1 |
| SYSTEM_ADMINISTRATOR | NOT FOUND | Removed in Phase 1 |
| admin@test.com | NOT FOUND | Removed in Phase 1 |

*(Note: These terms were verified using a repository-wide grep search and only appear in our Phase 0/1 audit documentation now).*

## 7. Verification
- **Security Searches:** Complete repository scan confirms zero remaining instances of the legacy JWT, privileged test accounts, and local user authentication.
- **Maven Compile:** Compilation was previously verified to be blocked by the missing Maven (`mvn` / `mvnw`) installation in the workspace. However, the codebase was structurally cleansed during Phase 1 to stub out breaking `User` and `SecurityContextHolder` imports.
- **Remaining Known Dependencies:** `ProfileService` and `RelationshipService` currently use a hardcoded `"temp-user-id"` stub where they previously fetched the User ID from `SecurityContextHolder`.

## 8. Deferred to Phase 3
The correct RS256 / Gateway JWT security architecture has **NOT** been implemented yet. This phase only focused on demolition of the old implementation. Phase 3 will introduce the proper Gateway JWT security filters, public key validation, and token parsing.
