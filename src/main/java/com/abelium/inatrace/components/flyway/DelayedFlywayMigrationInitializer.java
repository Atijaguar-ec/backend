/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.abelium.inatrace.components.flyway;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

/**
 * Like {@link FlywayMigrationInitializer}, but had to write new one to support JPA migrations because
 * spring configures {@link EntityManagerFactory} to explicitli depend on {@link FlywayMigrationInitializer}.
 * @see FlywayAutoConfiguration 
 *
 * @author Phillip Webb
 * @since 1.3.0
 */
public class DelayedFlywayMigrationInitializer implements InitializingBean, Ordered {

    private static final Logger log = LoggerFactory.getLogger(DelayedFlywayMigrationInitializer.class);

    private final Flyway flyway;

    private final FlywayMigrationStrategy migrationStrategy;

    private int order = 0;

    /**
     * Create a new {@link DelayedFlywayMigrationInitializer} instance.
     * @param flyway the flyway instance
     */
    public DelayedFlywayMigrationInitializer(Flyway flyway) {
        this(flyway, null);
    }

    /**
     * Create a new {@link DelayedFlywayMigrationInitializer} instance.
     * @param flyway the flyway instance
     * @param migrationStrategy the migration strategy or {@code null}
     */
    @Autowired
    public DelayedFlywayMigrationInitializer(Flyway flyway, FlywayMigrationStrategy migrationStrategy) {
        Assert.notNull(flyway, "Flyway must not be null");
        this.flyway = flyway;
        this.migrationStrategy = migrationStrategy;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("[Flyway] DelayedFlywayMigrationInitializer.afterPropertiesSet - starting migrations (strategy present: {})", this.migrationStrategy != null);
        if (this.migrationStrategy != null) {
            log.info("[Flyway] Using JpaMigrationStrategy to run JPA migrations");
            this.migrationStrategy.migrate(this.flyway);
        }
        else {
            log.info("[Flyway] No custom migration strategy configured, calling flyway.migrate() directly");
            this.flyway.migrate();
        }
        log.info("[Flyway] DelayedFlywayMigrationInitializer.afterPropertiesSet - finished migrations");
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
