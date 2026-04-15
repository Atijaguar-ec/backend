# Design: Migrate Cacao Core Fields to agstack_dev

## Technical Approach

Manual entity-by-entity port following INATrace's existing layered pattern (Entity → DTO → Mapper → Service → Controller). A single consolidated Flyway SQL migration creates all schema changes. Java seed migrations follow, versioned after the SQL to ensure tables/columns exist first.

## Architecture Decisions

| Decision | Alternatives | Rationale |
|---|---|---|
| Single SQL migration for all schema | One migration per table | Clean bootstrap; no fix/rollback chain. PostgreSQL `ADD COLUMN IF NOT EXISTS` ensures idempotency |
| `CertificationType` extends `TimestampEntity` | Extend `CodebookBaseEntity` | Needs `creationTimestamp`/`updateTimestamp` for audit; `CodebookBaseEntity` has only `code`/`label` and no timestamps |
| `Language` enum kept as `{EN,DE,RW,ES}` | Reduce to `{EN,ES}` now | Enum reduction is a separate migration; avoid scope creep. CertificationTypeTranslation uses `Language` as-is |
| `PersonType` in `com.abelium.inatrace.types` | Put in `db.enums` | Follows existing pattern: `CompanyStatus`, `UserCustomerType` are in `types/` package |
| `FacilityType.order` uses quoted column name | Rename to `sortOrder` | `order` is a SQL reserved word; staging solved this with `` `order` `` quoting. In PostgreSQL we use `"order"`. Maintaining field name consistency with staging |

## Data Flow

```
POST /api/stock-order  (ApiStockOrder with new fields)
    │
    ▼
StockOrderService.createOrUpdateStockOrder()
    ├── entity.setWeekNumber(api.getWeekNumber())
    ├── entity.setParcelLot(api.getParcelLot())  
    ├── entity.setVariety / organicCert / moisture...
    ├── calculateNetQuantity(gross, tare, damaged, moisture%)
    │   └── net = (gross - tare - damaged) × (moisture / 100)
    ├── entity.setNetQuantity(net)
    ├── cost = pricePerUnit × net
    │   └── if finalPriceDiscount != null: cost -= discount
    └── em.persist(entity)
    
GET /api/group-stock-orders  (includes new columns in JPQL + Excel)
    └── SELECT ... SO.weekNumber, SO.parcelLot, SO.variety, SO.organicCertification ...
        GROUP BY ... SO.weekNumber, SO.parcelLot, SO.variety, SO.organicCertification ...
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `db/entities/stockorder/StockOrder.java` | Modify | +7 `@Column` fields + getters/setters |
| `db/entities/facility/Facility.java` | Modify | +3 fields: `level`, `displayFinalPriceDiscount`, `displayMoisturePercentage` |
| `db/entities/codebook/FacilityType.java` | Modify | +1 field `order` with `@Column(name="\"order\"")`, `@Table(indexes=...)` |
| `db/entities/common/FarmInformation.java` | Modify | +1 field `maxProductionQuantity` |
| `db/entities/common/UserCustomer.java` | Modify | +3 fields: `personType`, `companyName`, `legalRepresentative` |
| `db/entities/codebook/CertificationType.java` | Create | Entity with `code`, `name`, `category`, `status`, translations |
| `db/entities/codebook/CertificationTypeTranslation.java` | Create | i18n entity with `name`, `language`, FK to parent |
| `types/PersonType.java` | Create | Enum: `NATURAL`, `LEGAL` |
| `db/enums/CertificationCategory.java` | Create | Enum: `CERTIFICATE`, `SEAL` |
| `db/enums/CertificationStatus.java` | Create | Enum: `ACTIVE`, `INACTIVE` |
| `components/stockorder/api/ApiStockOrder.java` | Modify | Mirror 7 fields |
| `components/stockorder/mappers/StockOrderMapper.java` | Modify | Map 7 fields in 3 methods |
| `components/stockorder/StockOrderService.java` | Modify | Add `calculateNetQuantity()`, setters, discount logic |
| `components/groupstockorder/api/ApiGroupStockOrder.java` | Modify | +6 fields |
| `components/groupstockorder/GroupStockOrderService.java` | Modify | Update JPQL queries + Excel columns |
| `components/facility/api/ApiFacility.java` | Modify | +3 fields |
| `components/facility/FacilityMapper.java` | Modify | Map 3 fields |
| `components/codebook/certification_type/CertificationTypeController.java` | Create | CRUD endpoints |
| `components/codebook/certification_type/CertificationTypeService.java` | Create | Business logic (follows `FacilityTypeService` pattern) |
| `components/codebook/certification_type/CertificationTypeMapper.java` | Create | Entity↔DTO mapping |
| `components/codebook/certification_type/api/ApiCertificationType.java` | Create | DTO |
| `components/codebook/certification_type/api/ApiCertificationTypeTranslation.java` | Create | DTO |
| `resources/db/migrations/V2026_04_10_00_00__Add_Cocoa_Core_Fields.sql` | Create | Consolidated PostgreSQL migration |
| `db/migrations/V2026_04_10_01_00__Seed_Cocoa_Catalogs.java` | Create | Consolidated Java seed (combines 8 staging migrations) |

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Compile | All entities, DTOs, mappers | `mvn clean compile` |
| Unit | `calculateNetQuantity()` | JUnit 5 test with boundary cases (null moisture, zero tare, etc.) |
| Integration | Flyway migration | Start app against fresh PostgreSQL; Hibernate `validate` confirms schema match |
| Smoke | GroupStockOrder JPQL | Verify `MONTH()`, `YEAR()`, `WEEK()` queries parse with Hibernate 6 |

## Migration / Rollout

**Flyway versioning order:**
1. `V2026_04_10_00_00` — SQL: schema changes (ADD COLUMN, CREATE TABLE)
2. `V2026_04_10_01_00` — Java: seed data (Cocoa catalogs, FacilityTypes, SemiProducts, links)

**Rollback SQL** (if needed):
```sql
ALTER TABLE "StockOrder" DROP COLUMN IF EXISTS "weekNumber", 
  DROP COLUMN IF EXISTS "parcelLot", DROP COLUMN IF EXISTS "variety", ...;
DROP TABLE IF EXISTS "CertificationTypeTranslation" CASCADE;
DROP TABLE IF EXISTS "CertificationType" CASCADE;
```

## Open Questions

- [x] ~~Is `CompanyProcessingAction` needed?~~ → **No** (decided in exploration)
- [ ] Confirm: should Java seed migration be 1 consolidated file or keep as 8 separate files matching staging versioning?
