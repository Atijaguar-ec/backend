package com.abelium.inatrace.configuration;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

/**
 * @deprecated Replaced by {@link CustomPostgreSQLDialect} as part of the postgres-migration change.
 * This class is kept for reference only and is no longer registered in application.properties.
 */
@Deprecated(since = "postgres-migration", forRemoval = true)
public class CustomMySQLDialect extends MySQLDialect {

    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {

        super.initializeFunctionRegistry(functionContributions);

        functionContributions.getFunctionRegistry()
                .register("GROUP_CONCAT", new StandardSQLFunction("group_concat", StandardBasicTypes.STRING));
    }

}
