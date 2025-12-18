package com.abelium.inatrace.configuration;

import com.abelium.inatrace.components.flyway.DelayedFlywayMigrationInitializer;
import com.abelium.inatrace.components.flyway.JpaMigrationStrategy;
import com.abelium.inatrace.components.flyway.JpaMigrationValidationResolver;
import jakarta.persistence.EntityManagerFactory;
import java.util.Arrays;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Configuración de migraciones Flyway con soporte para migraciones SQL y Java (JPA).
 * 
 * <p>Flujo de ejecución:</p>
 * <ol>
 *   <li><b>flywayInitializer</b> - Ejecuta migraciones SQL (crea tablas)</li>
 *   <li><b>Hibernate</b> - Valida el schema (ddl-auto=validate)</li>
 *   <li><b>delayedFlywayInitializer</b> - Ejecuta migraciones Java (requiere EntityManager)</li>
 * </ol>
 * 
 * <p>Este orden es necesario porque:</p>
 * <ul>
 *   <li>Las migraciones SQL deben ejecutarse ANTES de que Hibernate valide el schema</li>
 *   <li>Las migraciones Java necesitan EntityManager, que se crea DESPUÉS de validar el schema</li>
 * </ul>
 * 
 * @see DelayedFlywayMigrationInitializer
 * @see JpaMigrationStrategy
 */
@Configuration
public class MigrationsConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MigrationsConfiguration.class);
    
    /**
     * Configura Flyway para:
     * 1. Ignorar migraciones faltantes (para compatibilidad con historial)
     * 2. Registrar el resolver de migraciones JPA para que sean visibles durante validate
     */
    @Bean
    FlywayConfigurationCustomizer flywayConfigurationCustomizer(Environment environment) {
        return configuration -> {
            // Configurar ignore patterns
            String patterns = environment.getProperty("spring.flyway.ignore-migration-patterns");
            if (patterns == null || patterns.isBlank()) {
                patterns = environment.getProperty("INATRACE_FLYWAY_IGNORE_MIGRATION_PATTERNS");
            }
            if (patterns == null || patterns.isBlank()) {
                patterns = "*:missing";
            }

            String[] split = Arrays.stream(patterns.split("\\s*,\\s*"))
                    .filter(p -> p != null && !p.isBlank())
                    .toArray(String[]::new);

            log.info("[Flyway] Applying ignoreMigrationPatterns={}", Arrays.toString(split));
            configuration.ignoreMigrationPatterns(split);
            
            // Registrar resolver de migraciones JPA para que sean visibles durante validate
            // Esto evita el error "applied migration not resolved locally" para migraciones Java
            JpaMigrationValidationResolver validationResolver = new JpaMigrationValidationResolver(configuration);
            configuration.resolvers(validationResolver);
            log.info("[Flyway] Registered JpaMigrationValidationResolver for JPA migration visibility");
        };
    }
    
    /**
     * Ejecuta las migraciones SQL antes de que se cree el EntityManagerFactory.
     * Esto permite que Hibernate valide el schema correctamente (ddl-auto=validate).
     */
    @Bean
    @ConditionalOnClass(Flyway.class)
    @ConditionalOnProperty(value = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
    FlywayMigrationInitializer flywayInitializer(Flyway flyway, Environment environment) {
        boolean repairOnStartup = Boolean.parseBoolean(
                environment.getProperty("INATRACE_FLYWAY_REPAIR_ON_STARTUP", "false"));

        FlywayMigrationStrategy strategy = flywayInstance -> {
            if (repairOnStartup) {
                log.warn("[Flyway] INATRACE_FLYWAY_REPAIR_ON_STARTUP=true - running flyway.repair() before migrate()");
                flywayInstance.repair();
            }
            flywayInstance.migrate();
        };

        return new FlywayMigrationInitializer(flyway, strategy);
    }
    
    /**
     * Ejecuta las migraciones Java (JPA) después de que el EntityManagerFactory esté inicializado.
     * Las migraciones Java implementan {@link com.abelium.inatrace.components.flyway.JpaMigration}
     * y pueden usar EntityManager para operaciones de base de datos complejas.
     */
    @Bean
    @ConditionalOnClass(Flyway.class)
    @ConditionalOnProperty(value = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
    DelayedFlywayMigrationInitializer delayedFlywayInitializer(Flyway flyway, EntityManagerFactory entityManagerFactory, Environment environment) {
        log.info("[Flyway] Creating DelayedFlywayMigrationInitializer bean (JPA migrations phase)");
        return new DelayedFlywayMigrationInitializer(flyway, new JpaMigrationStrategy(entityManagerFactory, environment));
    }
}