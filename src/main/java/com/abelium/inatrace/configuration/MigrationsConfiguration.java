package com.abelium.inatrace.configuration;

import com.abelium.inatrace.components.flyway.DelayedFlywayMigrationInitializer;
import com.abelium.inatrace.components.flyway.JpaMigrationStrategy;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
public class MigrationsConfiguration {
    
    // Override default Flyway initializer to do nothing
    @Bean
    @ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
    FlywayMigrationInitializer flywayInitializer(Flyway flyway) {
        return new FlywayMigrationInitializer(flyway, f -> { /* empty */ });
    }
    
    // Create a second Flyway initializer to run after EntityManagerFactory was initialized
    @Bean
    @ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
    DelayedFlywayMigrationInitializer delayedFlywayInitializer(Flyway flyway, EntityManagerFactory entityManagerFactory, Environment environment) {
        return new DelayedFlywayMigrationInitializer(flyway, new JpaMigrationStrategy(entityManagerFactory, environment));
    }
}
