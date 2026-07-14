package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import jakarta.persistence.EntityManager;
import org.springframework.core.env.Environment;

public class V2026_07_14_13_00__Add_Company_Configuration_Jsonb implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        // Safe check if company table exists
        Number companyTableCount = (Number) em.createNativeQuery(
            "SELECT count(*) FROM information_schema.tables WHERE table_name = 'company'")
            .getSingleResult();
        
        if (companyTableCount == null || companyTableCount.intValue() < 1) {
            return;
        }

        // Check if configuration column exists in the database
        Number columnCount = (Number) em.createNativeQuery(
            "SELECT count(*) FROM information_schema.columns WHERE table_name = 'company' AND LOWER(column_name) = 'configuration'")
            .getSingleResult();

        if (columnCount == null || columnCount.intValue() == 0) {
            em.createNativeQuery("ALTER TABLE company ADD COLUMN configuration JSONB DEFAULT '{}'::jsonb").executeUpdate();
        }
    }
}
