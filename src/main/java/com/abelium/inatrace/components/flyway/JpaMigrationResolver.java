package com.abelium.inatrace.components.flyway;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Migration resolver for JPA migrations. 
 * (Modified JDBC migrations)
 */
public class JpaMigrationResolver implements MigrationResolver {

    private static final Logger log = LoggerFactory.getLogger(JpaMigrationResolver.class);

    private final EntityManagerFactory entityManagerFactory;
    
    /**
     * The base package on the classpath where the migrations are located.
     */
    private final Location[] locations;

    /**
     * The ClassLoader to use.
     */
    private final ClassLoader classLoader;
    
    /**
     * The application environment
     */
    private final Environment environment;

    public JpaMigrationResolver(Configuration configuration, EntityManagerFactory entityManagerFactory, Environment environment) {
        this.locations = configuration.getLocations();
        this.classLoader = configuration.getClassLoader();
        this.entityManagerFactory = entityManagerFactory;
        this.environment = environment;
    }

    @Override
    public Collection<ResolvedMigration> resolveMigrations(Context context) {
        List<ResolvedMigration> migrations = new ArrayList<>();

        log.info("[Flyway] JpaMigrationResolver.resolveMigrations - starting resolution for locations: {}", (Object) locations);
        for (Location location : locations) {
            if (!location.isClassPath()) {
                log.debug("[Flyway] Skipping non-classpath location: {}", location);
                continue;
            }

            try {
                log.info("[Flyway] Scanning for JpaMigration classes in location: {}", location);

                String descriptor = location.toString();
                String basePackage = descriptor;
                if (basePackage.startsWith("classpath:")) {
                    basePackage = basePackage.substring("classpath:".length());
                }
                if (basePackage.startsWith("/")) {
                    basePackage = basePackage.substring(1);
                }
                basePackage = basePackage.replace('/', '.');

                if (basePackage.isEmpty()) {
                    log.warn("[Flyway] Skipping empty base package for location: {}", location);
                    continue;
                }

                ClassPathScanningCandidateComponentProvider scanner =
                        new ClassPathScanningCandidateComponentProvider(false);
                scanner.addIncludeFilter(new AssignableTypeFilter(JpaMigration.class));

                int foundCount = 0;
                for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
                    String className = candidate.getBeanClassName();
                    if (className == null) {
                        continue;
                    }

                    try {
                        Class<?> clazz = Class.forName(className, false, classLoader);
                        
                        // Validate that class has a no-arg constructor
                        try {
                            clazz.getDeclaredConstructor();
                        } catch (NoSuchMethodException e) {
                            log.error("[Flyway] Migration class {} must have a no-argument constructor", className);
                            throw new FlywayException("Migration class " + className + " must have a no-argument constructor", e);
                        }
                        
                        JpaMigration migration = (JpaMigration) clazz.getDeclaredConstructor().newInstance();
                        ResolvedMigrationImpl migrationInfo = extractMigrationInfo(migration, clazz);
                        
                        if (migrationInfo != null) {
                            log.info("[Flyway] Resolved JPA migration: version={}, description={}, script={}",
                                    migrationInfo.getVersion(), migrationInfo.getDescription(), migrationInfo.getScript());
                            migrations.add(migrationInfo);
                            foundCount++;
                        }
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | 
                             java.lang.reflect.InvocationTargetException e) {
                        log.error("[Flyway] Failed to instantiate migration class: {}", className, e);
                        throw new FlywayException("Unable to instantiate migration class: " + className, e);
                    }
                }

                log.info("[Flyway] Found {} JpaMigration classes in base package {}", foundCount, basePackage);
            } catch (FlywayException e) {
                throw e;
            } catch (Exception e) {
                log.error("[Flyway] Unexpected error while resolving JPA migrations in location: {}", location, e);
                throw new FlywayException("Unable to resolve Custom JPA migrations in location: " + location, e);
            }
        }

        migrations.sort(new ResolvedMigrationComparator());
        log.info("[Flyway] JpaMigrationResolver.resolveMigrations - total resolved JPA migrations: {}", migrations.size());
        return migrations;
    }

    /**
     * Extracts the migration info from this migration.
     *
     * @param migration The migration to analyze.
     * @param clazz 
     * @return The migration info.
     */
    ResolvedMigrationImpl extractMigrationInfo(JpaMigration migration, Class<?> clazz) {
        String className = getShortName(migration.getClass());
        Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription(className, "V", "__", new String[] { "" }, false);
        MigrationVersion version = info.getLeft();
        String description = info.getRight();

        if (version == null) {
            log.warn("[Flyway] Skipping migration class with invalid naming pattern (expected V<version>__<description>): {}", className);
            return null;
        }

        Integer checksum = null;
        String script = migration.getClass().getName();
        String physicalLocation = ClassUtils.getLocationOnDisk(clazz);
        JpaMigrationExecutor executor = new JpaMigrationExecutor(migration, entityManagerFactory, environment);

        return new ResolvedMigrationImpl(version, description, script, checksum, checksum, CoreMigrationType.CUSTOM, physicalLocation, executor);
    }

    private static String getShortName(Class<?> aClass) {
        String name = aClass.getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }
} 
