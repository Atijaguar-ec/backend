# AGENTS.md · INATrace Backend Core

> **Audience:** AI Coding Agents, Copilots, and Automated Reviewers.  
> **Repository:** `backend` (Java 17 / Spring Boot 3.3.3 / PostgreSQL 16).

---

## 1. Primary Entrypoint for AI Agents

👉 **Read [`agent-context.md`](agent-context.md) first.** It is the canonical 1800-line specification covering all architectural invariants, Flyway database rules, Keycloak OIDC authentication, TorpedoQuery criteria, and multi-tenant domain logic.

---

## 2. Quick Invariants

1. **Database:** PostgreSQL 16 (never MySQL). Multi-schema: `public`, `shrimp`, `bi`.
2. **Flyway:** `ddl-auto = validate`. Schema changes ONLY via Flyway migrations in `src/main/resources/db/migration/`. All migrations must be registered in `FlywayMigrationParseTest.java`.
3. **Authentication:** Keycloak 26 OIDC (`KeycloakJwtAuthenticationConverter`), stateless JWT Bearer.
4. **Queries & Pagination:** Count queries with Torpedo must always strip `ORDER BY` using `PaginationTools.stripOrderBy()`.
5. **Cacao Domain:** `COCOA` is the internal product type key. Weight is in pounds (`PESOLIBRA`). Quotas in quintales (`qq`).
6. **Shrimp Domain:** Follows `RF01` rules and the OKF specification.
