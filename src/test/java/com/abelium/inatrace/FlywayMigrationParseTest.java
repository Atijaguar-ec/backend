package com.abelium.inatrace;

import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resource.classpath.ClassPathResource;
import org.flywaydb.core.internal.sqlscript.SqlStatementIterator;
import org.flywaydb.database.postgresql.PostgreSQLParser;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FlywayMigrationParseTest {

    @Test
    void testAllFlywayMigrationsParseSuccessfully() throws Exception {
        FluentConfiguration config = new FluentConfiguration();
        ParsingContext ctx = new ParsingContext();
        PostgreSQLParser parser = new PostgreSQLParser(config, ctx);

        String[] files = {
            "db/migration/V1__baseline_canonical.sql",
            "db/migration/V2__envers_audit_tables.sql",
            "db/migration/V3__add_fk_indexes.sql",
            "db/migration/V4__add_delivery_receipt_and_unocace_config.sql",
            "db/migration/V5__enable_delivery_receipt_for_all_unocace_orgs.sql",
            "db/migration/V6__add_display_quota_balance.sql",
            "db/migration/V7__add_sortorder_to_processingactionpef.sql",
            "db/migration/V8__configure_unocace_quota_alert_threshold.sql",
            "db/migration/V9__add_stockorder_cancellation.sql"
        };

        for (String file : files) {
            ClassPathResource res = new ClassPathResource(null, file, getClass().getClassLoader(), StandardCharsets.UTF_8);
            assertTrue(res.exists(), "Migration file " + file + " must exist on classpath");
            SqlStatementIterator iterator = parser.parse(res);
            int count = 0;
            while (iterator.hasNext()) {
                iterator.next();
                count++;
            }
            assertTrue(count > 0, "Migration file " + file + " should have at least 1 SQL statement");
            System.out.println("Verified Flyway parsing for " + file + ": " + count + " statements parsed successfully.");
        }
    }
}
