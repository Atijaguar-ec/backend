package com.abelium.inatrace.configuration;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

/**
 * Custom Hibernate dialect for PostgreSQL.
 * Registers STRING_AGG (equivalent to MySQL's GROUP_CONCAT) as a JPQL function.
 *
 * Usage in JPQL: STRING_AGG(cast(entity.id as string), ',')
 * Translates to: string_agg(cast(entity_id as text), ',')
 */
public class CustomPostgreSQLDialect extends PostgreSQLDialect {

    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        super.initializeFunctionRegistry(functionContributions);

        functionContributions.getFunctionRegistry()
                .register("STRING_AGG", new StandardSQLFunction("string_agg", StandardBasicTypes.STRING));

        functionContributions.getFunctionRegistry()
                .registerPattern("MONTH", "extract(month from ?1)");
        functionContributions.getFunctionRegistry()
                .registerPattern("YEAR", "extract(year from ?1)");
        functionContributions.getFunctionRegistry()
                .registerPattern("WEEK", "extract(week from ?1)");
    }
}
