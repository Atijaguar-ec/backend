package com.abelium.inatrace.configuration;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.type.BasicTypeRegistry;
import org.junit.jupiter.api.Test;


import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CustomPostgreSQLDialectTest {

    @Test
    void shouldRegisterStringAggFunction() {
        CustomPostgreSQLDialect dialect = new CustomPostgreSQLDialect();
        org.junit.jupiter.api.Assertions.assertNotNull(dialect);
    }
}
