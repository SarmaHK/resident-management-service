# Phase 1 — Foundation

## 1. Objective
To create a clean Spring Boot foundation for the `resident-management-service` by removing legacy identity/account management, stripping out local JWT implementations, and configuring the service for MySQL and Flyway.

## 2. Technology Stack
- Java 17
- Spring Boot 3.2.5
- Maven
- MySQL
- Flyway

## 3. Dependency Changes
**Removed:**
- `org.postgresql:postgresql` (Replaced with MySQL)
- `com.h2database:h2` (Replaced with MySQL)
- `org.springframework.boot:spring-boot-starter-security` (Local security removed for now)
- `org.springframework.security:spring-security-test`
- `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson` (Local JWT generation removed)

**Added:**
- `com.mysql:mysql-connector-j` (MySQL driver)
- `org.flywaydb:flyway-core`, `flyway-mysql` (Database migrations)

## 4. Database Configuration
Configured in `application.yml` and `.env.example` using environment variables:
- `DB_URL`: Configured to `jdbc:mysql://localhost:3306/ams_resident_db`
- `DB_USERNAME`: Database user
- `DB_PASSWORD`: Database password

H2 has been completely removed from configuration.

## 5. Flyway Configuration
- Flyway is now enabled (`flyway.enabled: true` in `application.yml`).
- `src/main/resources/db/migration/V1__init_schema.sql` was rewritten to *only* contain `profiles` and `apartment_relationships`.
- All User and Role tables were removed from the initial migration as they do not belong to this service.

## 6. Service Boundary
The `users`, `roles`, `user_roles`, `user_status_history`, and `email_verification_tokens` tables and entities were completely removed from the project. This enforces the rule that **identity/account ownership belongs to `identity-access-service`**. Local authentication functionality based on these tables has also been removed. 

## 7. Package Structure
```
src/main/java/com/ams/resident/
    controller/
        ProfileController.java
        RelationshipController.java
    service/
        ProfileService.java
        RelationshipService.java
        AuditService.java
    repository/
        ProfileRepository.java
        ApartmentRelationshipRepository.java
    entity/
        Profile.java, ResidentProfile.java, etc.
        ApartmentRelationship.java
    dto/
        ProfileRequest.java, ProfileResponse.java
        RelationshipRequest.java, RelationshipResponse.java
    exception/
        GlobalExceptionHandler.java
        ResourceNotFoundException.java
        ...
```

## 8. Removed Legacy Components
- **TestAuthController**: Removed to eliminate the `/api/v1/auth/test-token` privilege-escalation backdoor.
- **JwtUtils**: Removed completely.
- **SecurityConfig** and **AuthTokenFilter**: Removed (from the `security` package).
- **User/Role Components**: `User.java`, `Role.java`, `UserService.java`, `RoleService.java`, `UserController.java`, `RoleController.java` (and their respective repositories and DTOs) were deleted.
- **Legacy Email Components**: `EmailVerificationToken.java`, `EmailVerificationTokenRepository.java`, and the email change endpoints in `ProfileController` were deleted.
- **PostgreSQL/H2 Configuration**: Removed from `pom.xml`, `application.yml`, and `.env.example`.

## 9. Verification Results
- **Dependencies verification**: No PostgreSQL or H2 dependencies remain in `pom.xml`.
- **Flyway verification**: Enabled and migration updated.
- **Backdoor verification**: `TestAuthController` and its `/test-token` endpoint are completely gone.
- **JWT verification**: Local `jwt.secret` and HS256 logic are gone.
- **Build & Connection Verification**: 
    - *Blocker*: Could not execute `./mvnw clean compile` or start the application because neither Maven (`mvn`) nor the Maven Wrapper (`mvnw`) are installed in the workspace environment.

## 10. Known Next Phase
`PHASE 2` / subsequent security and domain phases will implement the remaining functionality. The `Profile` and `Relationship` service methods have been stubbed to allow compilation, but their business logic, provisioning, and integration with the Gateway JWT are NOT complete yet.
