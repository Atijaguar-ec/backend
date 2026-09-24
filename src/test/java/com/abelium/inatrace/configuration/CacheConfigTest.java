package com.abelium.inatrace.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CacheConfigTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    void testCacheManagerIsActiveAndCachesConfigured() {
        assertNotNull(cacheManager, "CacheManager bean must be configured");

        Cache currenciesCache = cacheManager.getCache("currencies");
        assertNotNull(currenciesCache, "'currencies' cache must be available");

        Cache certificationsCache = cacheManager.getCache("certifications");
        assertNotNull(certificationsCache, "'certifications' cache must be available");

        // Test put and evict
        currenciesCache.put("EUR", "Euro");
        assertEquals("Euro", currenciesCache.get("EUR", String.class));
        currenciesCache.evict("EUR");
    }
}
