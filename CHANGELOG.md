# Changelog

All notable changes to the INATrace Backend project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] - Cacao Premium Consolidation (`agstack_dev`)

### Added
- **Cacao Core Fields:** Added extensive mapping for Cocoa traceability in entities such as `StockOrder`, `Facility`, `UserCustomer` and `FarmInformation`. This introduces fields like `weekNumber`, `parcelLot`, `variety`, `moisturePercentage`, `maxProductionQuantity`, etc.
- **Python Migration Toolkit:** Introduced `/scripts/migrate_all_cacao_data.py` to seamlessly dump and sanitize operational databases from MySQL variables to pure PostgreSQL schemas. Includes auto-correction features for mismatched `BIT(1)` and missing default validations.
- **Staging PostgreSQL Setup:** Updated Staging configurations (`docker-compose.yml` on remote `test/fortaleza`) to spawn `postgres:16-alpine` on dual-port configuration, effectively supporting continuous transitions to the newer `agstack` framework.
- **Certification Module:** Support for Organic Certification Codebooks via `CertificationType` and its i18n variants, maintaining strict consistency across the new data model payload.

### Changed
- **Persistence Foundation:** Fully transitioned from legacy MySQL persistence configurations into robust PostgreSQL paradigms with Spring Boot 3.3.3 and Hibernate 6 handling strictly `V___` Flyway versioning.
- **Data Pruning:** Intentionally sanitized historical data models by omitting parameters native tightly built for Shrimp traceability operations (`LARVA_GROWING`, `ShrimpFlavorDefect`, etc.) during operations.
- **Business Logic Integration:** Extended the `GroupStockOrderService` aggregation mechanisms and the basic `StockOrderService` to securely handle net weight calculations relying on `moistureWeightDeduction`.

### Removed
- Removed old iterations of explicit codebook relations strictly focused on non-cacao commodities to optimize deployment footprints and strictly keep the domain models unpolluted.
