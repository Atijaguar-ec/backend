package com.abelium.inatrace.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;

/**
 * Logs all registered REST endpoints at startup for debugging purposes.
 */
@Component
public class EndpointLoggerConfig {

    private static final Logger logger = LoggerFactory.getLogger(EndpointLoggerConfig.class);

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public EndpointLoggerConfig(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logEndpoints() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        
        // Log user-related endpoints specifically
        logger.info("=== Registered REST Endpoints ({} total) ===", handlerMethods.size());
        
        handlerMethods.forEach((mapping, method) -> {
            String patterns = mapping.getPatternValues().toString();
            if (patterns.contains("/user") || patterns.contains("/login")) {
                logger.info("USER ENDPOINT: {} -> {}.{}", 
                    patterns, 
                    method.getBeanType().getSimpleName(), 
                    method.getMethod().getName());
            }
        });
        
        logger.info("=== End of Endpoint Registration ===");
    }
}
