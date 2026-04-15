## Exploration: Migrate Cacao Fields from staging (MySQL) to agstack_dev (PostgreSQL + Keycloak)

### Current State

**`agstack_dev`** is a clean fork of the original INATrace platform, modernized to run on PostgreSQL with Keycloak authentication. It has no Cacao-specific fields — it represents the vanilla INATrace core with infrastructure upgrades.

**`staging`** contains 6+ months of Cacao specialization work on top of the original INATrace (MySQL). This includes new fields in 5 existing entities, 3 new entities, 4 new enums, business logic for moisture/net-quantity calculations, and ~30 Flyway migrations (many of which are incremental fixes/rollbacks of each other, written in MySQL syntax).

**Goal:** Port the Cacao-specific logic cleanly into `agstack_dev`, adapting all MySQL-specific SQL to PostgreSQL, and consolidating the 30+ incremental migrations into a single clean script.

### Affected Areas

#### Entities (JPA — fields to add)
- `src/main/java/com/abelium/inatrace/db/entities/stockorder/StockOrder.java` — 7 new fields: weekNumber, parcelLot, variety, organicCertification, moisturePercentage, moistureWeightDeduction, netQuantity, finalPriceDiscount
- `src/main/java/com/abelium/inatrace/db/entities/facility/Facility.java` — 3 new fields: level, displayFinalPriceDiscount, displayMoisturePercentage
- `src/main/java/com/abelium/inatrace/db/entities/codebook/FacilityType.java` — 1 new field: order
- `src/main/java/com/abelium/inatrace/db/entities/common/FarmInformation.java` — 1 new field: maxProductionQuantity
- `src/main/java/com/abelium/inatrace/db/entities/common/UserCustomer.java` — 3 new fields: personType, companyName, legalRepresentative

#### New Entities (to create)
- `src/main/java/com/abelium/inatrace/db/entities/codebook/CertificationType.java` — Certification catalog
- `src/main/java/com/abelium/inatrace/db/entities/codebook/CertificationTypeTranslation.java` — i18n for certifications

#### New Enums (to create)
- `src/main/java/com/abelium/inatrace/types/PersonType.java` — NATURAL, LEGAL
- `src/main/java/com/abelium/inatrace/db/enums/CertificationCategory.java` — CERTIFICATE, SEAL
- `src/main/java/com/abelium/inatrace/db/enums/CertificationStatus.java` — ACTIVE, INACTIVE
- `src/main/java/com/abelium/inatrace/db/enums/CodebookStatus.java` — General codebook status

#### DTOs (API models to modify/create)
- `src/main/java/com/abelium/inatrace/components/stockorder/api/ApiStockOrder.java` — Mirror 7 StockOrder fields
- `src/main/java/com/abelium/inatrace/components/facility/api/ApiFacility.java` — Mirror 3 Facility fields
- `src/main/java/com/abelium/inatrace/components/groupstockorder/api/ApiGroupStockOrder.java` — Add weekNumber, parcelLot, variety, organicCertification, facilityName, farmerName
- `src/main/java/com/abelium/inatrace/components/codebook/certification_type/api/ApiCertificationType.java` — [NEW]
- `src/main/java/com/abelium/inatrace/components/codebook/certification_type/api/ApiCertificationTypeTranslation.java` — [NEW]

#### Mappers (to modify/create)
- `src/main/java/com/abelium/inatrace/components/stockorder/mappers/StockOrderMapper.java` — Map 7 fields in 3 mapper methods
- `src/main/java/com/abelium/inatrace/components/facility/FacilityMapper.java` — Map 3 fields
- `src/main/java/com/abelium/inatrace/components/codebook/certification_type/CertificationTypeMapper.java` — [NEW]

#### Services (to modify/create)
- `src/main/java/com/abelium/inatrace/components/stockorder/StockOrderService.java` — Add calculateNetQuantity(), setters for all new fields, finalPriceDiscount logic
- `src/main/java/com/abelium/inatrace/components/groupstockorder/GroupStockOrderService.java` — Update JPQL queries (SELECT + GROUP BY) and Excel export
- `src/main/java/com/abelium/inatrace/components/codebook/certification_type/CertificationTypeService.java` — [NEW]

#### Controllers (to create)
- `src/main/java/com/abelium/inatrace/components/codebook/certification_type/CertificationTypeController.java` — [NEW] CRUD for certifications

#### Flyway Migrations (to create — PostgreSQL)
- `src/main/resources/db/migrations/V2026_04_10_00_00__Add_Cocoa_Core_Fields.sql` — Single consolidated PostgreSQL migration

#### Java Migrations (to port — adapt to PostgreSQL)
- 8 Java migrations from staging to port: Prefill_SemiProducts, Link_SemiProducts_To_ValueChains, Link_FacilityTypes_To_ValueChains, Link_MeasureUnitTypes_To_ValueChains, Ensure_USD_CurrencyType, Fix_ProductType_Initialization_For_Cocoa, Ensure_Cocoa_FacilityTypes, Force_Replace_Coffee_With_Cocoa_Catalogs

### Approaches

1. **Cherry-pick + Adapt** — Cherry-pick specific commits from staging, resolve conflicts, rewrite SQL
   - Pros: Git history preserved, traceability
   - Cons: 30+ commits mixed with shrimp code, massive conflict resolution, MySQL SQL all over
   - Effort: **High** (risky, error-prone)

2. **Manual Port Entity-by-Entity** — Read each entity from staging, manually add the cacao-only fields to agstack_dev files, write fresh PostgreSQL migration
   - Pros: Clean result, no shrimp leakage, PostgreSQL-native from day one, full control
   - Cons: More manual work, no git lineage
   - Effort: **Medium** (methodical, predictable, safe)

3. **Create a Diff Patch** — Generate a diff of only the cacao fields from staging, apply as a patch
   - Pros: Automated extraction
   - Cons: Diffs include MySQL migrations and shrimp-adjacent changes, partial conflicts
   - Effort: **Medium-High** (still need heavy manual filtering)

### Recommendation

**Approach 2: Manual Port Entity-by-Entity.** It is the cleanest and safest approach:
- We avoid any shrimp contamination
- We write PostgreSQL-native SQL from the start (no MySQL→Postgres translation bugs)
- We produce a consolidation of 30+ incremental migrations into 1 clean script
- The `agent-context.md` serves as a checklist to validate every addition
- SDD phases (spec → design → tasks → apply → verify) give us full traceability

### Risks

1. **JPQL compatibility** — The `GroupStockOrderService` queries use complex `CONCAT()` and `GROUP BY` with `MONTH()`, `YEAR()`. The `CustomPostgreSQLDialect` already registers `MONTH`, `YEAR`, `WEEK` functions, but we must verify the exact HQL syntax works with Hibernate 6's strict query parser.

2. **FarmInformation embedding** — `FarmInformation` is an `@Embeddable`, so its column `maxProductionQuantity` maps to `UserCustomer.farm_maxProductionQuantity`. Need to verify the column naming strategy matches agstack_dev's Hibernate config.

3. **CertificationType timestamps** — Staging went through 3 fix migrations to get timestamp column names right (`createdAt` → `creationTimestamp`). In agstack_dev, must ensure `TimestampEntity` convention is followed from the start.

4. **Language enum reduction** — Staging reduced `Language` from `{DE,EN,ES,RW}` to `{EN,ES}`. Need to verify if agstack_dev's `Language` enum already matches or needs the same reduction.

5. **Java migration ordering** — Flyway's `out-of-order=true` is enabled, but the Java seed migrations (Prefill catalogs) must run AFTER the SQL migration that creates the columns/tables they reference.

### Ready for Proposal

**Yes.** The exploration is complete. The next step is `sdd-propose` to formalize the change with scope, rollback plan, and affected modules. Then `sdd-spec` → `sdd-design` → `sdd-tasks` → `sdd-apply` → `sdd-verify`.
