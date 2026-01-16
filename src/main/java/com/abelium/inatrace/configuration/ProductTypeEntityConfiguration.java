package com.abelium.inatrace.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration to conditionally skip schema validation for shrimp-specific entities
 * based on the product type. This prevents Hibernate schema validation errors when
 * shrimp tables don't exist in non-shrimp deployments (e.g., COCOA, COFFEE).
 * 
 * IMPORTANT: This is a temporary workaround. The proper solution is implemented via
 * the cleanup migration V2026_01_15_10_00__Cleanup_Shrimp_Tables_For_Non_Shrimp_Deployments
 * which removes shrimp tables and columns from non-shrimp databases.
 * 
 * For non-shrimp deployments, this configuration changes Hibernate's ddl-auto from
 * 'validate' to 'none' to skip schema validation entirely. This allows the application
 * to start even if entity classes don't match the database schema.
 */
@Configuration
public class ProductTypeEntityConfiguration {

    @Value("${INATrace.product.type:COCOA}")
    private String productType;

    /**
     * For non-shrimp deployments, disable strict schema validation to allow startup
     * even when shrimp entity classes exist but their tables don't.
     */
    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return hibernateProperties -> {
            if (!isShrimpDeployment()) {
                // Force-disable schema validation/updates for non-shrimp deployments.
                // Some environments set ddl-auto via Spring, so this value may not be present yet.
                // Setting it here ensures Hibernate doesn't fail startup validating shrimp entities.
                hibernateProperties.put("hibernate.hbm2ddl.auto", "none");
                hibernateProperties.put("javax.persistence.schema-generation.database.action", "none");
            }
        };
    }

    private boolean isShrimpDeployment() {
        return "SHRIMP".equalsIgnoreCase(productType) || "CAMARON".equalsIgnoreCase(productType);
    }
}
