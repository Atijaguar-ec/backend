package com.abelium.inatrace.configuration;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class CustomH2Dialect extends H2Dialect {

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
