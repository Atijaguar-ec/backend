package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import jakarta.persistence.EntityManager;
import org.springframework.core.env.Environment;

/**
 * Adds production estimate, certification type and cocoa variety to the farmer plot.
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

        if (!columnExists(em, tableName, "certificationtype")) {
            // VARCHAR(255): the longest constant is 49 chars, past the usual 40 char enum column
            em.createNativeQuery("ALTER TABLE " + tableName + " ADD COLUMN certificationtype VARCHAR(255)").executeUpdate();
        }

        if (!columnExists(em, tableName, "cocoavariety")) {
            em.createNativeQuery("ALTER TABLE " + tableName + " ADD COLUMN cocoavariety VARCHAR(40)").executeUpdate();
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
