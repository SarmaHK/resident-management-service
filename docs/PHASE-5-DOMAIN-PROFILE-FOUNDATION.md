# Phase 5 — Domain Model & Profile Foundation

## 1. Objective
Implement the Domain Model & Profile Foundation for SCRUM-28 / FR-RES-001, maintaining a profile for each registered user with support for Resident, Owner, and Staff profile types, while ensuring a clean Identity service boundary.

## 2. SCRUM-28 / FR-RES-001
This story covers the core domain mapping for user profiles and how they persist within the resident-management-service.

## 3. Provisioning Decision
**BLOCKED**
The exact trigger for provisioning a `Profile` and the assignment of `profileType` is NOT yet defined by the approved requirements. Documentation in Phase 0 and Phase 1 confirms that no business logic decision exists for whether a profile derives from an Identity JWT role automatically, or if an administrator must explicitly provision the profile first. Because of this, automatic provisioning was NOT invented, and the system waits for an approved trigger.

## 4. Profile Domain Model
The `Profile` domain entity uses a `SINGLE_TABLE` JPA inheritance strategy to encapsulate specific profile types.
- **userId**: Maps logically to the Identity service account ID (`UNIQUE NOT NULL`).
- **profileType**: Enumeration restricted strictly to `RESIDENT`, `OWNER`, and `STAFF`.
- **contact fields**: Includes `firstName`, `lastName`, and `phone` at the abstract level, and extensions like `emergencyContact` or `department` at the subclass level.
- **status**: The approved Phase 1 schema (`V1__init_schema.sql`) does not require a `status` field for profiles, thus it is not implemented as a database column to prevent inventing transitions.

## 5. Identity Boundary
Identity Account
      │
      │ userId reference (String/VARCHAR)
      ▼
Resident Management
      │
      └── Profile

**Explicit Confirmation:** NO cross-service database foreign key exists. The `userId` is persisted as a plain `VARCHAR(255)`.

## 6. Database
- Database: MySQL
- Migrations: Flyway
- The `profiles` table is initialized in `V1__init_schema.sql` matching the entity properties. No duplicate profile tables were generated.

## 7. Security
- **Gateway JWT**: Requests must pass the Gateway RS256 JWT validation configured in Phase 4.
- **JWT sub → userId**: The domain uses `SecurityUtils.getCurrentUserId()` to map the authenticated token `sub` directly to the `userId`.
- No local JWT generation exists.
- No hardcoded `temp-user-id` or mock users remain.

## 8. Verification
- **Compilation**: PASS
- **Flyway Migrations**: PASS
- **No Duplicate Schema**: PASS
- **E2E Runtime Security Verification**: BLOCKED (Gateway environment unavailable locally).
