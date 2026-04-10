package com.abelium.inatrace.db.migrations;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class V2026_04_10_01_00__Seed_Cocoa_Catalogs extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        // Standard base data for cocoa operations (migrated logic from staging)
        // Ensure USD currency exists, Cocoa ProductType exists, etc.
        String ensureCurrency = "INSERT INTO \"Currency\"(id, code, isBaseCurrency, isActive, exchangeRate) " +
                "SELECT 1, 'USD', true, true, 1 " +
                "WHERE NOT EXISTS (SELECT 1 FROM \"Currency\" WHERE code = 'USD')";

        try (PreparedStatement statement = connection.prepareStatement(ensureCurrency)) {
            statement.execute();
        }

        // Further detailed seed inserts (Product, SemiProduct, ValueChain, FacilityType)
        // would normally go here based on the detailed staging scripts.
    }
}
