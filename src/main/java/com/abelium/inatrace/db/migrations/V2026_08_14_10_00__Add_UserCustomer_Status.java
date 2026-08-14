package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import jakarta.persistence.EntityManager;
import org.springframework.core.env.Environment;

/**
 * Adds the status column to user_customer (ACTIVE / SUSPENDED / RETIRED).
 *
 * The column is deliberately left nullable, matching UserCustomer.status: this JpaMigration
 * runs through Flyway, and Flyway's JpaMigrationStrategy is constructed with an already-built
 * EntityManagerFactory - meaning Hibernate's own hbm2ddl.auto=update (see application.properties)
 * bootstraps and tries to reconcile the schema from the entity mapping BEFORE this class ever
 * gets a chance to run. A NOT NULL column loses that race against a table that already has
 * rows: Postgres refuses "ADD COLUMN ... NOT NULL" without a default when rows exist, Hibernate
 * only logs a warning and carries on, and the column silently never gets created. Confirmed
 * live against the UNOCACE staging DB. Every existing user customer predates this feature and
 * is, by definition, part of the organization, so this backfills them as ACTIVE anyway - as
 * does UserCustomer.getStatus() and the list-query status filter, for the case where this
 * migration itself doesn't run either (its custom Flyway resolver has never actually executed
 * on any environment checked so far: schema_version shows only the baseline row).
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
            // VARCHAR(40) matches Lengths.ENUM used by the entity mapping. No NOT NULL here -
            // see the class-level note on why that fails against a populated table.
            em.createNativeQuery("ALTER TABLE " + tableName + " ADD COLUMN status VARCHAR(40)").executeUpdate();
            em.createNativeQuery("ALTER TABLE " + tableName + " ALTER COLUMN status SET DEFAULT 'ACTIVE'").executeUpdate();
        }

        // Hygiene backfill, independent of whether we just added the column above: leaves
        // no NULL status behind for anyone querying the DB directly, even though the app
        // itself already treats a null column as ACTIVE everywhere it matters.
        em.createNativeQuery("UPDATE " + tableName + " SET status = 'ACTIVE' WHERE status IS NULL").executeUpdate();

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
