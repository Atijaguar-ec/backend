package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import jakarta.persistence.EntityManager;
import org.springframework.core.env.Environment;

/**
 * Adds the status column to user_customer (ACTIVE / SUSPENDED / RETIRED).
 *
 * Every existing user customer predates the feature and is, by definition, part of the
 * organization, so they are backfilled as ACTIVE. The column is created NOT NULL with a
 * default so that the Hibernate mapping (nullable = false) validates on startup.
 */
public class V2026_08_14_10_00__Add_UserCustomer_Status implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {

        // Resolve the actual table name - Hibernate's naming strategy decides between
        // 'usercustomer' and 'user_customer', so don't assume either one.
        String tableName = (String) em.createNativeQuery(
                "SELECT table_name FROM information_schema.tables " +
                "WHERE LOWER(table_name) IN ('usercustomer', 'user_customer') LIMIT 1")
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (tableName == null) {
            // Fresh database: hbm2ddl creates the column from the entity mapping.
            return;
        }

        if (!columnExists(em, tableName, "status")) {
            // VARCHAR(40) matches Lengths.ENUM used by the entity mapping
            em.createNativeQuery("ALTER TABLE " + tableName + " ADD COLUMN status VARCHAR(40)").executeUpdate();
            em.createNativeQuery("UPDATE " + tableName + " SET status = 'ACTIVE' WHERE status IS NULL").executeUpdate();
            em.createNativeQuery("ALTER TABLE " + tableName + " ALTER COLUMN status SET DEFAULT 'ACTIVE'").executeUpdate();
            em.createNativeQuery("ALTER TABLE " + tableName + " ALTER COLUMN status SET NOT NULL").executeUpdate();
        }

        // Audit of the last status change. Left NULL for pre-existing rows: nobody
        // changed their status, they were backfilled as ACTIVE by this migration.
        if (!columnExists(em, tableName, "statusreason")) {
            em.createNativeQuery("ALTER TABLE " + tableName + " ADD COLUMN statusreason VARCHAR(255)").executeUpdate();
        }

        if (!columnExists(em, tableName, "statusupdatetimestamp")) {
            em.createNativeQuery("ALTER TABLE " + tableName + " ADD COLUMN statusupdatetimestamp TIMESTAMP WITH TIME ZONE").executeUpdate();
        }

        if (!columnExists(em, tableName, "statusupdatedby_id")) {
            em.createNativeQuery("ALTER TABLE " + tableName + " ADD COLUMN statusupdatedby_id BIGINT").executeUpdate();
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
