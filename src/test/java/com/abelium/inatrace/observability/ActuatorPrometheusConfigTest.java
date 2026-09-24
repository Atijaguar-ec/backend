package com.abelium.inatrace.observability;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ActuatorPrometheusConfigTest {

    @Autowired
    private Environment environment;

    @Test
    void testPrometheusEndpointConfigurationIsExposed() {
        String exposedEndpoints = environment.getProperty("management.endpoints.web.exposure.include");
        assertNotNull(exposedEndpoints, "management.endpoints.web.exposure.include must be configured");
        assertTrue(exposedEndpoints.contains("prometheus"), "management.endpoints.web.exposure.include must include 'prometheus'");
        assertTrue(exposedEndpoints.contains("health"), "management.endpoints.web.exposure.include must include 'health'");

        String prometheusEnabled = environment.getProperty("management.endpoint.prometheus.enabled");
        assertEquals("true", prometheusEnabled, "management.endpoint.prometheus.enabled must be true");
    }
}
