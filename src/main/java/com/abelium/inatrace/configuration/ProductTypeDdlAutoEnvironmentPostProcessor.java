package com.abelium.inatrace.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class ProductTypeDdlAutoEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String productTypeConfig = environment.getProperty("INATrace.product.type", "COFFEE");
        boolean isShrimp = "SHRIMP".equalsIgnoreCase(productTypeConfig) || "CAMARON".equalsIgnoreCase(productTypeConfig);

        if (isShrimp) {
            return;
        }

        Map<String, Object> props = new HashMap<>();
        props.put("spring.jpa.hibernate.ddl-auto", "none");

        environment.getPropertySources().addFirst(new MapPropertySource("productTypeDdlAutoOverride", props));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
