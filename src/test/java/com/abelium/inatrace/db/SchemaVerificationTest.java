package com.abelium.inatrace.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SchemaVerificationTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private int singleInt(String sql) {
        Integer val = jdbcTemplate.queryForObject(sql, Integer.class);
        return val == null ? 0 : val;
    }

    @Test
    @DisplayName("CompanyProcessingAction: tabla, columnas timestamps e índices")
    void companyProcessingActionStructure() {
        String schema = "SELECT DATABASE()";
        String tableExists = "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = (" + schema + ") AND table_name = 'CompanyProcessingAction'";
        assertEquals(1, singleInt(tableExists), "Tabla CompanyProcessingAction debe existir");

        String creationCol = "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_schema = (" + schema + ") AND table_name = 'CompanyProcessingAction' AND column_name = 'createdAt'";
        String updateCol = "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_schema = (" + schema + ") AND table_name = 'CompanyProcessingAction' AND column_name = 'updatedAt'";
        assertEquals(1, singleInt(creationCol), "Columna createdAt faltante");
        assertEquals(1, singleInt(updateCol), "Columna updatedAt faltante");

        String idxCompanyEnabled = "SELECT COUNT(DISTINCT index_name) FROM information_schema.statistics " +
                "WHERE table_schema = (" + schema + ") AND table_name = 'CompanyProcessingAction' " +
                "AND index_name = 'idx_company_processing_action_company_enabled' AND non_unique = 1";
        String idxProcessingAction = "SELECT COUNT(DISTINCT index_name) FROM information_schema.statistics " +
                "WHERE table_schema = (" + schema + ") AND table_name = 'CompanyProcessingAction' " +
                "AND index_name = 'idx_company_processing_action_processing_action' AND non_unique = 1";
        assertEquals(1, singleInt(idxCompanyEnabled), "Índice idx_company_processing_action_company_enabled no encontrado o es único");
        assertEquals(1, singleInt(idxProcessingAction), "Índice idx_company_processing_action_processing_action no encontrado o es único");

        String uniqueUk = "SELECT COUNT(DISTINCT tc.constraint_name) FROM information_schema.table_constraints tc " +
                "JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name " +
                "AND tc.table_schema = kcu.table_schema AND tc.table_name = kcu.table_name " +
                "WHERE tc.table_schema = (" + schema + ") AND tc.table_name = 'CompanyProcessingAction' " +
                "AND tc.constraint_type = 'UNIQUE' AND tc.constraint_name = 'uk_company_processing_action_company_action'";
        assertEquals(1, singleInt(uniqueUk), "UNIQUE (company_id, processing_action_id) faltante");
    }

    @Test
    @DisplayName("Transaction: índices no únicos y claves foráneas correctas")
    void transactionIndexesAndFKs() {
        String schema = "SELECT DATABASE()";

        // No debe existir índice único sobre sourceStockOrder_id ni inputMeasureUnitType_id
        String uniqueIdxSource = "SELECT COUNT(*) FROM information_schema.statistics " +
                "WHERE table_schema = (" + schema + ") AND table_name = 'Transaction' " +
                "AND column_name = 'sourceStockOrder_id' AND non_unique = 0";
        String uniqueIdxMeasure = "SELECT COUNT(*) FROM information_schema.statistics " +
                "WHERE table_schema = (" + schema + ") AND table_name = 'Transaction' " +
                "AND column_name = 'inputMeasureUnitType_id' AND non_unique = 0";
        assertEquals(0, singleInt(uniqueIdxSource), "No debe haber índice ÚNICO en Transaction.sourceStockOrder_id");
        assertEquals(0, singleInt(uniqueIdxMeasure), "No debe haber índice ÚNICO en Transaction.inputMeasureUnitType_id");

        // Deben existir índices no únicos con los nombres esperados
        String idxSource = "SELECT COUNT(*) FROM information_schema.statistics " +
                "WHERE table_schema = (" + schema + ") AND table_name = 'Transaction' " +
                "AND index_name = 'idx_transaction_source_stock_order' AND non_unique = 1";
        String idxMeasure = "SELECT COUNT(*) FROM information_schema.statistics " +
                "WHERE table_schema = (" + schema + ") AND table_name = 'Transaction' " +
                "AND index_name = 'idx_transaction_input_measure_unit_type' AND non_unique = 1";
        assertEquals(1, singleInt(idxSource), "Falta índice no único idx_transaction_source_stock_order");
        assertEquals(1, singleInt(idxMeasure), "Falta índice no único idx_transaction_input_measure_unit_type");

        // FKs deben existir
        String fkSource = "SELECT COUNT(*) FROM information_schema.key_column_usage " +
                "WHERE table_schema = (" + schema + ") AND table_name = 'Transaction' AND column_name = 'sourceStockOrder_id' " +
                "AND referenced_table_name = 'StockOrder' AND referenced_column_name = 'id'";
        String fkMeasure = "SELECT COUNT(*) FROM information_schema.key_column_usage " +
                "WHERE table_schema = (" + schema + ") AND table_name = 'Transaction' AND column_name = 'inputMeasureUnitType_id' " +
                "AND referenced_table_name = 'MeasureUnitType' AND referenced_column_name = 'id'";
        assertEquals(1, singleInt(fkSource), "FK sourceStockOrder_id -> StockOrder(id) faltante");
        assertEquals(1, singleInt(fkMeasure), "FK inputMeasureUnitType_id -> MeasureUnitType(id) faltante");
    }

    @Test
    @DisplayName("FacilityType: columna 'order' e índice no único")
    void facilityTypeOrderColumnAndIndex() {
        String schema = "SELECT DATABASE()";

        String tableExists = "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = (" + schema + ") AND table_name = 'FacilityType'";
        assertEquals(1, singleInt(tableExists), "Tabla FacilityType debe existir");

        String columnExists = "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_schema = (" + schema + ") AND table_name = 'FacilityType' AND column_name = 'order'";
        assertEquals(1, singleInt(columnExists), "Columna 'order' en FacilityType faltante");

        String indexExists = "SELECT COUNT(DISTINCT index_name) FROM information_schema.statistics " +
                "WHERE table_schema = (" + schema + ") AND table_name = 'FacilityType' " +
                "AND index_name = 'idx_facility_type_order' AND non_unique = 1";
        assertEquals(1, singleInt(indexExists), "Índice no único idx_facility_type_order faltante");
    }
}
