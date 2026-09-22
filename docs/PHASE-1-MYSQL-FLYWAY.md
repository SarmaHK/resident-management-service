# Phase 1 — MySQL + Flyway Database Foundation

## 1. Objective
To completely remove all obsolete PostgreSQL and H2 implementations from the repository, configure the foundation for MySQL with Flyway, and establish a clean schema focused strictly on `resident-management-service` domain tables without any identity/access crossover.

## 2. Database Ownership
The database belongs strictly to the `resident-management-service`. Tables for Identity management (`users`, `roles`, `user_roles`) have been actively excluded from the schema as they belong to the `identity-access-service`.

## 3. PostgreSQL Removal
PostgreSQL dependencies (`org.postgresql:postgresql`) have been removed from `pom.xml`, and its dialact configuration has been cleared from application properties. The source code contains no PostgreSQL footprint.

## 4. H2 Removal
H2 runtime dependencies have been removed from `pom.xml` and `application.yml` is no longer pointing to an in-memory `jdbc:h2` URL.

## 5. MySQL Configuration
Configured in `application.yml` utilizing environment variables to avoid hardcoded credentials:
- `DB_URL`: defaults to `jdbc:mysql://localhost:3306/ams_resident_db`
- `DB_USERNAME`: defaults to `root`
- `DB_PASSWORD`: defaults to `root`
- Driver: `com.mysql.cj.jdbc.Driver`
- Dialect: `org.hibernate.dialect.MySQLDialect`

## 6. Flyway Configuration
Flyway has been explicitly enabled in the configuration (`flyway.enabled: true`). 

## 7. Migration Design
The initial migration (`V1__init_schema.sql`) was redesigned to drop the legacy identity tables and now focuses exclusively on `profiles` and `apartment_relationships`. 

## 8. Resident-Owned Tables

| Table | Purpose | Owner | Cross-Service FK |
|---|---|---|---|
| `profiles` | Stores resident/owner profile details | `resident-management-service` | None (uses soft reference `user_id`) |
| `apartment_relationships` | Stores unit occupancy associations | `resident-management-service` | None (uses soft reference `user_id`) |

## 9. Identity Data Boundary
The `users` and `roles` are not stored here because they represent the core authentication domain which belongs strictly to the `identity-access-service` according to the database-per-service architecture.

## 10. Foreign Key Verification
- A full search of the migration file confirmed **0** cross-service foreign keys.
- Neither `REFERENCES users` nor `REFERENCES roles` are present in the SQL.
- `user_id` is tracked strictly as a soft domain value.

## 11. Fresh Database Verification
- **database used**: MySQL 8.0 (Local, `ams_resident_db`)
- **migration result**: SUCCESS (Flyway applied `V1__init_schema.sql` successfully)
- **tables created**: `apartment_relationships`, `profiles`, `flyway_schema_history`
- **Flyway status**: Schema `ams_resident_db` is up to date.

## 12. Application Connection Verification
SUCCESS — Application successfully retrieved credentials via environment variables, HikariCP connected to MySQL, and Spring Boot started normally.

## 13. Search Verification
Repository scan confirmed:
- `postgresql` / `jdbc:postgresql`: Only found in `docs/`
- `h2` / `jdbc:h2`: Only found in `docs/`
- `users` / `roles` tables: Only found in `docs/`
- `REFERENCES users`: Not found in active code.

## 14. Known Limitations
- The business requirement for `profileType` provisioning remains **unresolved** (whether it derives from a role claim or an explicit admin trigger). No business logic has been implemented for it.

## 15. Phase Completion Status
- [x] MySQL configured
- [x] PostgreSQL removed from application dependencies
- [x] PostgreSQL removed from application configuration
- [x] H2 removed from application runtime configuration
- [x] Flyway enabled
- [x] Flyway migration directory configured
- [x] Clean resident-only schema created
- [x] users table absent
- [x] roles table absent
- [x] user_roles table absent
- [x] No cross-service database FK
- [x] userId references are soft/domain references

**VERIFICATION:**
- [x] Fresh MySQL database created 
- [x] Flyway migration executed successfully
- [x] Flyway history confirms success
- [x] Actual tables inspected 
- [x] Foreign keys inspected 
- [x] Application connected to MySQL 
- [x] Application started successfully 
- [x] Build/compile executed successfully
