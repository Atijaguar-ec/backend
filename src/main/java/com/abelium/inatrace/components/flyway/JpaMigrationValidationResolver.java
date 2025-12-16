package com.abelium.inatrace.components.flyway;

import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.executor.MigrationExecutor;
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
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Resolver de migraciones JPA para la fase de validación de Flyway.
 * 
 * Este resolver permite que Flyway "vea" las migraciones Java (JpaMigration) durante
 * la fase de validación inicial, antes de que exista EntityManagerFactory.
 * 
 * Las migraciones NO se ejecutan aquí (el executor es un stub que lanza excepción si
 * se intenta ejecutar). La ejecución real ocurre en la fase delayed via JpaMigrationResolver.
 */
public class JpaMigrationValidationResolver implements MigrationResolver {

    private static final Logger log = LoggerFactory.getLogger(JpaMigrationValidationResolver.class);

    private final Location[] locations;
    private final ClassLoader classLoader;

    public JpaMigrationValidationResolver(Configuration configuration) {
        this.locations = configuration.getLocations();
        this.classLoader = configuration.getClassLoader();
    }

    @Override
    public Collection<ResolvedMigration> resolveMigrations(Context context) {
        List<ResolvedMigration> migrations = new ArrayList<>();

        log.debug("[Flyway] JpaMigrationValidationResolver - resolving JPA migrations for validation");
        for (Location location : locations) {
            if (!location.isClassPath()) {
                continue;
            }

            try {
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
                    continue;
                }

                ClassPathScanningCandidateComponentProvider scanner =
                        new ClassPathScanningCandidateComponentProvider(false);
                scanner.addIncludeFilter(new AssignableTypeFilter(JpaMigration.class));

                for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
                    String className = candidate.getBeanClassName();
                    if (className == null) {
                        continue;
                    }

                    try {
                        Class<?> clazz = Class.forName(className, false, classLoader);
                        ResolvedMigrationImpl migrationInfo = extractMigrationInfo(clazz);
                        
                        if (migrationInfo != null) {
                            log.debug("[Flyway] Resolved JPA migration for validation: version={}", 
                                    migrationInfo.getVersion());
                            migrations.add(migrationInfo);
                        }
                    } catch (ClassNotFoundException e) {
                        log.warn("[Flyway] Could not load migration class: {}", className);
                    }
                }
            } catch (Exception e) {
                log.error("[Flyway] Error resolving JPA migrations for validation", e);
            }
        }

        migrations.sort(new ResolvedMigrationComparator());
        log.debug("[Flyway] JpaMigrationValidationResolver - resolved {} JPA migrations", migrations.size());
        return migrations;
    }

    private ResolvedMigrationImpl extractMigrationInfo(Class<?> clazz) {
        String className = clazz.getSimpleName();
        Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription(
                className, "V", "__", new String[] { "" }, false);
        MigrationVersion version = info.getLeft();
        String description = info.getRight();

        if (version == null) {
            return null;
        }

        Integer checksum = null;
        String script = clazz.getName();
        String physicalLocation = ClassUtils.getLocationOnDisk(clazz);
        
        // Stub executor - las migraciones JPA se ejecutan en la fase delayed
        MigrationExecutor stubExecutor = new StubMigrationExecutor();

        return new ResolvedMigrationImpl(version, description, script, checksum, checksum, 
                CoreMigrationType.CUSTOM, physicalLocation, stubExecutor);
    }

    /**
     * Executor stub que no ejecuta nada.
     * Las migraciones JPA reales se ejecutan en DelayedFlywayMigrationInitializer.
     */
    private static class StubMigrationExecutor implements MigrationExecutor {
        @Override
        public void execute(org.flywaydb.core.api.executor.Context context) {
            // No-op: la ejecución real ocurre en la fase delayed
            throw new FlywayException("JPA migrations should not be executed in validation phase");
        }

        @Override
        public boolean canExecuteInTransaction() {
            return true;
        }

        @Override
        public boolean shouldExecute() {
            return false; // No ejecutar en esta fase
        }
    }
}
