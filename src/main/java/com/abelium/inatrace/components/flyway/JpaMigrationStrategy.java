package com.abelium.inatrace.components.flyway;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.core.env.Environment;

public class JpaMigrationStrategy implements FlywayMigrationStrategy {
    private static final Logger log = LoggerFactory.getLogger(JpaMigrationStrategy.class);

    private EntityManagerFactory entityManagerFactory;
    
    private Environment environment;

    public JpaMigrationStrategy(EntityManagerFactory entityManagerFactory, Environment environment) {
        this.entityManagerFactory = entityManagerFactory;
        this.environment = environment;
    }

    @Override
    public void migrate(Flyway flyway) {
        log.info("[Flyway] JpaMigrationStrategy.migrate - configuring Flyway for JPA migrations");
        JpaMigrationResolver jpaMigrationResolver = 
                new JpaMigrationResolver(flyway.getConfiguration(), entityManagerFactory, environment);
        
        ClassicConfiguration configuration = new ClassicConfiguration(flyway.getConfiguration());
        configuration.setResolvers(jpaMigrationResolver);
        log.info("[Flyway] JpaMigrationStrategy.migrate - starting Flyway.migrate() with custom JPA resolver");
        Flyway.configure().configuration(configuration).load().migrate();
        log.info("[Flyway] JpaMigrationStrategy.migrate - finished Flyway.migrate()");
    }
}