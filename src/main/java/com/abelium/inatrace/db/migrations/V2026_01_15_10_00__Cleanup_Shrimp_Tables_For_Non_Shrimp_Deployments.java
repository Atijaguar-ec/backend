package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import jakarta.persistence.EntityManager;
import org.springframework.core.env.Environment;

/**
 * Cleanup migration for non-shrimp deployments.
 * <p>
 * This migration ONLY runs when INATrace.product.type is NOT SHRIMP or CAMARON.
 * It removes all shrimp-specific database objects:
 * <ul>
 *   <li>Shrimp catalog tables (ShrimpFlavorDefect, ShrimpSizeGrade, etc.)</li>
 *   <li>Shrimp processing tables (LaboratoryAnalysis, ProcessingClassificationBatch, etc.)</li>
 *   <li>Shrimp-specific columns from shared tables (StockOrder, Facility)</li>
 * </ul>
 * <p>
 * This migration is idempotent and safe to re-run.
 *
 * @author INATrace Team
 */
public class V2026_01_15_10_00__Cleanup_Shrimp_Tables_For_Non_Shrimp_Deployments implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        String productTypeConfig = environment.getProperty("INATrace.product.type", "COFFEE");

        // Only run for NON-SHRIMP deployments
        if ("SHRIMP".equalsIgnoreCase(productTypeConfig) || "CAMARON".equalsIgnoreCase(productTypeConfig)) {
            return;
        }

        // IMPORTANT: Drop FKs and columns from shared tables FIRST
        // This allows us to drop shrimp tables without FK constraint errors
        dropShrimpColumnsFromSharedTables(em);
        // Then drop all shrimp-specific tables
        dropShrimpTables(em);
    }

    private void dropShrimpTables(EntityManager em) {
        // List of all shrimp-specific tables to drop
        // Order matters: drop child tables before parent tables
        String[] tablesToDrop = {
            // Translation tables first (have FKs to main tables)
            "ShrimpFlavorDefectTranslation",
            "ShrimpProcessTypeTranslation",
            "ShrimpQualityGradeTranslation",
            "ShrimpTreatmentTypeTranslation",
            "ShrimpPresentationTypeTranslation",
            "ShrimpFreezingTypeTranslation",
            "ShrimpMachineTranslation",
            "ShrimpBrandTranslation",
            
            // Processing detail tables (have FKs)
            "ProcessingClassificationBatchDetail",
            
            // Laboratory analysis (has FKs to StockOrder and User)
            "LaboratoryAnalysis",
            
            // Processing batch (has FK to StockOrder)
            "ProcessingClassificationBatch",
            
            // Main catalog tables
            "ShrimpFlavorDefect",
            "ShrimpSizeGrade",
            "ShrimpColorGrade",
            "ShrimpProcessType",
            "ShrimpQualityGrade",
            "ShrimpTreatmentType",
            "ShrimpPresentationType",
            "ShrimpFreezingType",
            "ShrimpMachine",
            "ShrimpBrand"
        };

        for (String tableName : tablesToDrop) {
            try {
                Long count = (Long) em.createNativeQuery(
                                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
                                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = :tableName")
                        .setParameter("tableName", tableName)
                        .getSingleResult();

                if (count != null && count > 0L) {
                    em.createNativeQuery("DROP TABLE IF EXISTS `" + tableName + "`").executeUpdate();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void dropShrimpColumnsFromSharedTables(EntityManager em) {
        // Drop shrimp-specific columns from StockOrder
        String[] stockOrderColumns = {
            // Field inspection and sensory testing
            "flavor_defect_type_id",  // FK to ShrimpFlavorDefect
            "flavorTestResult",
            "purchaseRecommended",
            "inspectionNotes",
            "field_inspection_id",  // FK to FieldInspection
            
            // Laboratory fields
            "sampleNumber",
            "receptionTime",
            "quality_document_id",  // FK to Document
            
            // Purchase/delivery fields
            "numberOfGavetas",
            "numberOfBines", 
            "numberOfPiscinas",
            "guiaRemisionNumber",
            
            // Processing fields - Cutting
            "cuttingType",
            "cuttingEntryDate",
            "cuttingExitDate",
            "cuttingTemperatureControl",
            
            // Processing fields - Treatment
            "treatmentType",
            "treatmentEntryDate",
            "treatmentExitDate",
            "treatmentTemperatureControl",
            "treatmentChemicalUsed",
            
            // Processing fields - Tunnel
            "tunnelProductionDate",
            "tunnelExpirationDate",
            "tunnelNetWeight",
            "tunnelSupplier",
            "tunnelFreezingType",
            "tunnelEntryDate",
            "tunnelExitDate",
            
            // Processing fields - Washing
            "washingWaterTemperature",
            "washingShrimpTemperatureControl"
        };

        dropColumnsFromTable(em, "StockOrder", stockOrderColumns);

        // Drop shrimp-specific columns from Facility
        String[] facilityColumns = {
            "isLaboratory",
            "isClassificationProcess",
            "isFreezingProcess",
            "isFieldInspection"
        };
        dropColumnsFromTable(em, "Facility", facilityColumns);
    }

    private void dropColumnsFromTable(EntityManager em, String tableName, String[] columns) {
        for (String columnName : columns) {
            try {
                Long count = (Long) em.createNativeQuery(
                                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                                        + "WHERE TABLE_SCHEMA = DATABASE() "
                                        + "AND TABLE_NAME = :tableName "
                                        + "AND COLUMN_NAME = :columnName")
                        .setParameter("tableName", tableName)
                        .setParameter("columnName", columnName)
                        .getSingleResult();

                if (count != null && count > 0L) {
                    dropForeignKeysForColumn(em, tableName, columnName);
                    em.createNativeQuery("ALTER TABLE `" + tableName + "` DROP COLUMN `" + columnName + "`")
                            .executeUpdate();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void dropForeignKeysForColumn(EntityManager em, String tableName, String columnName) {
        try {
            @SuppressWarnings("unchecked")
            java.util.List<String> fkNames = em.createNativeQuery(
                            "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE "
                                    + "WHERE TABLE_SCHEMA = DATABASE() "
                                    + "AND TABLE_NAME = :tableName "
                                    + "AND COLUMN_NAME = :columnName "
                                    + "AND REFERENCED_TABLE_NAME IS NOT NULL")
                    .setParameter("tableName", tableName)
                    .setParameter("columnName", columnName)
                    .getResultList();

            for (String fkName : fkNames) {
                em.createNativeQuery("ALTER TABLE `" + tableName + "` DROP FOREIGN KEY `" + fkName + "`")
                        .executeUpdate();
            }
        } catch (Exception ignored) {
        }
    }
}
