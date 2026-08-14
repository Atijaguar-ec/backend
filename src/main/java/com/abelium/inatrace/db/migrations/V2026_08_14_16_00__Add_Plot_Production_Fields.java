package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import jakarta.persistence.EntityManager;
import org.springframework.core.env.Environment;

/**
 * Adds production estimate, certification type (FK al catálogo administrable) y cocoa
 * variety a la parcela del agricultor.
 *
 * All three columns are nullable: existing plots simply don't have this data yet, and
 * a NOT NULL column would be rejected by Postgres against a populated table - the exact
 * failure that silently left UserCustomer.status uncreated (see that migration's notes;
 * Hibernate's hbm2ddl.auto=update runs before these JpaMigration classes and only logs
 * a warning when its own DDL fails).
 */
public class V2026_08_14_16_00__Add_Plot_Production_Fields implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {

        String tableName = (String) em.createNativeQuery(
                "SELECT table_name FROM information_schema.tables " +
                "WHERE LOWER(table_name) = 'plot' LIMIT 1")
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (tableName == null) {
            // Fresh database: hbm2ddl creates the columns from the entity mapping.
            return;
        }

        if (!columnExists(em, tableName, "productionestimate")) {
            // numeric(38,2) is what a plain @Column BigDecimal maps to in this codebase,
            // which is also the two decimal places the field was specified with.
            em.createNativeQuery("ALTER TABLE " + tableName + " ADD COLUMN productionestimate NUMERIC(38,2)").executeUpdate();
        }

        if (!columnExists(em, tableName, "cocoavariety")) {
            em.createNativeQuery("ALTER TABLE " + tableName + " ADD COLUMN cocoavariety VARCHAR(40)").executeUpdate();
        }

        // El tipo de certificación de la parcela referencia el catálogo administrable
        // (mismo que usa Recepción), no un enum del código. Una versión anterior de esta
        // misma migración, desplegada solo en staging el 2026-08-14, creó una columna
        // 'certificationtype' VARCHAR con un enum propio; se elimina si existe. Es seguro:
        // se verificó que ninguna parcela llegó a tener valor en ella.
        if (columnExists(em, tableName, "certificationtype")) {
            em.createNativeQuery("ALTER TABLE " + tableName + " DROP COLUMN certificationtype").executeUpdate();
        }

        if (!columnExists(em, tableName, "certificationtype_id")) {
            em.createNativeQuery("ALTER TABLE " + tableName + " ADD COLUMN certificationtype_id BIGINT").executeUpdate();
        }
    }

    private boolean columnExists(EntityManager em, String tableName, String columnName) {

        Number columnCount = (Number) em.createNativeQuery(
                "SELECT count(*) FROM information_schema.columns " +
                "WHERE LOWER(table_name) = LOWER(:tableName) AND LOWER(column_name) = :columnName")
                .setParameter("tableName", tableName)
                .setParameter("columnName", columnName)
                .getSingleResult();

        return columnCount != null && columnCount.intValue() > 0;
    }
}
