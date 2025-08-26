package com.abelium.inatrace;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ContextLoadsTest {

    @Test
    void contextLoads() {
        // If the Spring context loads, Flyway should have run successfully.
    }
}
