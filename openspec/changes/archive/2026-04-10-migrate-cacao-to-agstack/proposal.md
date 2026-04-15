# Proposal: Migrate Cacao Core Fields to agstack_dev

## Intent

Port all Cacao-specific business logic from `staging` (MySQL) into `agstack_dev` (PostgreSQL + Keycloak) to create a clean, premium Cacao-only version of INATrace for Fortaleza del Valle.

## Scope

### In Scope
- Add 15 fields across 5 existing entities (StockOrder, Facility, FacilityType, FarmInformation, UserCustomer)
- Create 3 new entities (CertificationType, CertificationTypeTranslation) + enums
- Port `calculateNetQuantity()` business logic and `finalPriceDiscount` calculation
- Update GroupStockOrder JPQL queries and Excel export
- Create 1 consolidated PostgreSQL Flyway migration
- Port 8 Java seed migrations adapted for PostgreSQL

### Out of Scope
- `CompanyProcessingAction` (deferred — not required for Cacao)
- Shrimp flags on Facility (isFieldInspection, isLaboratory, etc.)
- `quality_document_id` on StockOrder (was shrimp-specific)
- Frontend changes (Phase 2)
- `Import_Plots_From_GeoJSON` (manual post-install)

## Approach

Manual port entity-by-entity. Read each entity from staging, add only Cacao fields to agstack_dev, write fresh PostgreSQL SQL. Consolidate 30+ incremental MySQL migrations into 1 clean script.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `db/entities/stockorder/StockOrder.java` | Modified | +7 fields |
| `db/entities/facility/Facility.java` | Modified | +3 fields |
| `db/entities/codebook/FacilityType.java` | Modified | +1 field |
| `db/entities/common/FarmInformation.java` | Modified | +1 field |
| `db/entities/common/UserCustomer.java` | Modified | +3 fields |
| `db/entities/codebook/CertificationType.java` | New | Full entity + translation |
| `db/enums/CertificationCategory.java` | New | Enum |
| `types/PersonType.java` | New | Enum |
| `components/stockorder/` | Modified | Service, Mapper, DTO |
| `components/groupstockorder/` | Modified | Service, DTO |
| `components/facility/` | Modified | Mapper, DTO |
| `components/codebook/certification_type/` | New | Controller, Service, Mapper, DTOs |
| `resources/db/migrations/` | New | 1 SQL + 8 Java migrations |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| JPQL `MONTH()`/`YEAR()` syntax with Hibernate 6 | Medium | CustomPostgreSQLDialect already registers them; test queries first |
| FarmInformation column naming (`farm_*` prefix) | Low | Verify `@Embeddable` strategy matches |
| Java seed migration ordering (tables must exist first) | Medium | Use Flyway version ordering (SQL before Java seeds) |

## Rollback Plan

1. `git revert` the commit(s) on `agstack_dev`
2. Drop added columns via rollback migration: `ALTER TABLE StockOrder DROP COLUMN IF EXISTS weekNumber, ...`
3. Drop new tables: `DROP TABLE IF EXISTS CertificationTypeTranslation, CertificationType CASCADE;`

## Success Criteria

- [ ] `mvn clean compile` passes with all new entities
- [ ] `mvn clean test` passes (no regression)
- [ ] Flyway migration applies cleanly on a fresh PostgreSQL database
- [ ] All 15 fields are mapped end-to-end (Entity → DTO → Mapper)
- [ ] `calculateNetQuantity()` produces correct results
