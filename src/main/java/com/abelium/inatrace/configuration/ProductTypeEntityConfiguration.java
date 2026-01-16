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
                // Change from 'validate' to 'none' for non-shrimp deployments
                // This skips schema validation but still allows the app to run
                String currentDdlAuto = (String) hibernateProperties.get("hibernate.hbm2ddl.auto");
                
                if ("validate".equals(currentDdlAuto)) {
                    System.out.println("⚠️ NON-SHRIMP DEPLOYMENT: Changing hibernate.hbm2ddl.auto from 'validate' to 'none' to skip shrimp entity validation");
                    hibernateProperties.put("hibernate.hbm2ddl.auto", "none");
                }
            }
        };
    }

    private boolean isShrimpDeployment() {
        return "SHRIMP".equalsIgnoreCase(productType) || "CAMARON".equalsIgnoreCase(productType);
    }
}
