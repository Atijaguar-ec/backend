# Tasks: Migrate Cacao Core Fields to agstack_dev

## Phase 1: Foundation (Enums + New Entities)

- [ ] 1.1 Create `src/main/java/com/abelium/inatrace/types/PersonType.java` — enum `NATURAL`, `LEGAL`
- [ ] 1.2 Create `src/main/java/com/abelium/inatrace/db/enums/CertificationCategory.java` — enum `CERTIFICATE`, `SEAL`
- [ ] 1.3 Create `src/main/java/com/abelium/inatrace/db/enums/CertificationStatus.java` — enum `ACTIVE`, `INACTIVE`
- [ ] 1.4 Create `src/main/java/com/abelium/inatrace/db/entities/codebook/CertificationType.java` — JPA entity extending `TimestampEntity`, with `code`, `name`, `category`, `status`, `@OneToMany translations`, NamedQueries
- [ ] 1.5 Create `src/main/java/com/abelium/inatrace/db/entities/codebook/CertificationTypeTranslation.java` — JPA entity with `name`, `language`, `@ManyToOne certificationType`
- [ ] 1.6 Create `src/main/resources/db/migrations/V2026_04_10_00_00__Add_Cocoa_Core_Fields.sql` — consolidated PostgreSQL migration: ALTER TABLE StockOrder/Facility/FacilityType/UserCustomer + CREATE TABLE CertificationType/CertificationTypeTranslation

## Phase 2: Modify Existing Entities

- [ ] 2.1 Modify `StockOrder.java` — add 7 fields: `weekNumber`, `parcelLot`, `variety`, `organicCertification`, `moisturePercentage`, `moistureWeightDeduction`, `netQuantity`, `finalPriceDiscount` with `@Column` annotations
- [ ] 2.2 Modify `Facility.java` — add 3 fields: `level` (Integer), `displayFinalPriceDiscount` (Boolean), `displayMoisturePercentage` (Boolean)
- [ ] 2.3 Modify `FacilityType.java` — add `order` field with `@Column(name="\"order\"")`, index, constructor overload
- [ ] 2.4 Modify `FarmInformation.java` — add `maxProductionQuantity` (BigDecimal)
- [ ] 2.5 Modify `UserCustomer.java` — add `personType` (PersonType enum), `companyName` (String), `legalRepresentative` (String)

## Phase 3: DTOs + Mappers

- [x] 3.1 Modify `ApiStockOrder.java` — mirror 7 new fields with getters/setters
- [x] 3.2 Modify `StockOrderMapper.java` — map 7 fields in all 3 toApi methods (base, list, history)
- [x] 3.3 Modify `StockOrderService.java` — add setters for 7 fields in `createOrUpdate`, implement `calculateNetQuantity()`, add `finalPriceDiscount` subtraction in cost calc
- [x] 3.4 Modify `ApiGroupStockOrder.java` — add `weekNumber`, `parcelLot`, `variety`, `organicCertification`, `facilityName`, `farmerName`
- [x] 3.5 Modify `GroupStockOrderService.java` — update JPQL SELECT/GROUP BY + Excel export columns
- [x] 3.6 Modify `ApiFacility.java` — mirror 3 new fields
- [x] 3.7 Modify `FacilityMapper.java` — map `level`, `displayFinalPriceDiscount`, `displayMoisturePercentage`
- [x] 3.8 Create `components/codebook/certification_type/api/ApiCertificationType.java` — DTO
- [x] 3.9 Create `components/codebook/certification_type/api/ApiCertificationTypeTranslation.java` — DTO
- [x] 3.10 Create `components/codebook/certification_type/CertificationTypeMapper.java` — follow `FacilityTypeMapper` pattern
- [x] 3.11 Create `components/codebook/certification_type/CertificationTypeService.java` — follow `FacilityTypeService` pattern
- [x] 3.12 Create `components/codebook/certification_type/CertificationTypeController.java` — CRUD endpoints

## Phase 4: Data Seeds

- [x] 4.1 Create `db/migrations/V2026_04_10_01_00__Seed_Cocoa_Catalogs.java` — consolidated JpaMigration: prefill SemiProducts, link to ValueChains, link FacilityTypes, link MeasureUnitTypes, ensure USD currency, initialize Cocoa ProductType, ensure Cocoa FacilityTypes, replace Coffee→Cocoa catalogs

## Phase 5: Verification

- [x] 5.1 Run `mvn clean compile` — confirm all entities, DTOs, mappers compile
- [x] 5.2 Create `src/test/java/.../groupstockorder/GroupStockOrderServiceTest.java` — test `calculateNetQuantity()` with edge cases (null moisture, zero tare, full deduction)
- [x] 5.3 Verify Flyway migration against fresh PostgreSQL: `mvn clean test` or app startup with `ddl-auto=validate`
- [x] 5.4 Update `backend/agent-context.md` if any field names changed during implementation
