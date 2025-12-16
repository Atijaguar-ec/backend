package com.abelium.inatrace.configuration;

import com.abelium.inatrace.components.flyway.DelayedFlywayMigrationInitializer;
import com.abelium.inatrace.components.flyway.JpaMigrationStrategy;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
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
    
    /**
     * Ejecuta las migraciones SQL antes de que se cree el EntityManagerFactory.
     * Esto permite que Hibernate valide el schema correctamente (ddl-auto=validate).
     */
    @Bean
    @ConditionalOnBean(Flyway.class)
    FlywayMigrationInitializer flywayInitializer(Flyway flyway) {
        return new FlywayMigrationInitializer(flyway);
    }
    
    /**
     * Ejecuta las migraciones Java (JPA) después de que el EntityManagerFactory esté inicializado.
     * Las migraciones Java implementan {@link com.abelium.inatrace.components.flyway.JpaMigration}
     * y pueden usar EntityManager para operaciones de base de datos complejas.
     */
    @Bean
    @ConditionalOnBean(Flyway.class)
    DelayedFlywayMigrationInitializer delayedFlywayInitializer(Flyway flyway, EntityManagerFactory entityManagerFactory, Environment environment) {
        return new DelayedFlywayMigrationInitializer(flyway, new JpaMigrationStrategy(entityManagerFactory, environment));
    }
}
