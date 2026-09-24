package com.abelium.inatrace.configuration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HU-15: Static catalog caching configuration with @EnableCaching.
 * Caches static catalogs (currencies, certifications) to eliminate redundant database queries.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("currencies", "certifications");
        return cacheManager;
    }
}
