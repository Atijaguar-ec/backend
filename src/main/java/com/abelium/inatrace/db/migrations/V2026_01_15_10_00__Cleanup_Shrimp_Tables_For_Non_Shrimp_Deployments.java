package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import jakarta.persistence.EntityManager;
import org.springframework.core.env.Environment;

public class V2026_01_15_10_00__Cleanup_Shrimp_Tables_For_Non_Shrimp_Deployments implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        String productTypeConfig = environment.getProperty("INATrace.product.type", "COFFEE");

        if ("SHRIMP".equalsIgnoreCase(productTypeConfig) || "CAMARON".equalsIgnoreCase(productTypeConfig)) {
            return;
        }

        dropShrimpColumnsFromSharedTables(em);
        dropShrimpTables(em);
    }

    private void dropShrimpTables(EntityManager em) {
        String[] tablesToDrop = {
                "ShrimpFlavorDefectTranslation",
                "ShrimpProcessTypeTranslation",
                "ShrimpQualityGradeTranslation",
                "ShrimpTreatmentTypeTranslation",
                "ShrimpPresentationTypeTranslation",
                "ShrimpFreezingTypeTranslation",
                "ShrimpMachineTranslation",
                "ShrimpBrandTranslation",

                "ProcessingClassificationBatchDetail",
                "LaboratoryAnalysis",
                "ProcessingClassificationBatch",

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
        String[] stockOrderColumns = {
                "flavor_defect_type_id",
                "flavorTestResult",
                "purchaseRecommended",
                "inspectionNotes",
                "field_inspection_id",

                "sampleNumber",
                "receptionTime",
                "quality_document_id",

                "numberOfGavetas",
                "numberOfBines",
                "numberOfPiscinas",
                "guiaRemisionNumber",

                "cuttingType",
                "cuttingEntryDate",
                "cuttingExitDate",
                "cuttingTemperatureControl",

                "treatmentType",
                "treatmentEntryDate",
                "treatmentExitDate",
                "treatmentTemperatureControl",
                "treatmentChemicalUsed",

                "tunnelProductionDate",
                "tunnelExpirationDate",
                "tunnelNetWeight",
                "tunnelSupplier",
                "tunnelFreezingType",
                "tunnelEntryDate",
                "tunnelExitDate",

                "washingWaterTemperature",
                "washingShrimpTemperatureControl"
        };
        dropColumnsFromTable(em, "StockOrder", stockOrderColumns);

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
