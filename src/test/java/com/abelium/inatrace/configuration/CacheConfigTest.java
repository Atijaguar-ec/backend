package com.abelium.inatrace.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CacheConfigTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    void testCacheManagerIsActiveAndCachesConfigured() {
        assertNotNull(cacheManager, "CacheManager bean must be configured");
        assertTrue(cacheManager instanceof CacheConfig.TtlConcurrentMapCacheManager,
                "CacheManager must enforce TTL for HU-15 / ADR-014");

        Cache currenciesCache = cacheManager.getCache("currencies");
        assertNotNull(currenciesCache, "'currencies' cache must be available");

        Cache certificationsCache = cacheManager.getCache("certifications");
        assertNotNull(certificationsCache, "'certifications' cache must be available");

        // Test put and evict
        currenciesCache.put("EUR", "Euro");
        assertEquals("Euro", currenciesCache.get("EUR", String.class));
        currenciesCache.evict("EUR");
        assertNull(currenciesCache.get("EUR"));
    }

    @Test
    void testCacheTtlExpiration() {
        Cache currenciesCache = cacheManager.getCache("currencies");
        assertNotNull(currenciesCache);
        assertTrue(currenciesCache instanceof CacheConfig.TtlConcurrentMapCache);

        CacheConfig.TtlConcurrentMapCache ttlCache = (CacheConfig.TtlConcurrentMapCache) currenciesCache;
        ttlCache.put("USD", "US Dollar");
        assertEquals("US Dollar", ttlCache.get("USD", String.class));

        // Simulate TTL expiration (past 24h)
        long expiredTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(25);
        ttlCache.setWriteTimestampForTesting("USD", expiredTime);

        // Accessing expired item should return null (cache miss / evicted)
        assertNull(ttlCache.get("USD", String.class), "Expired cache item must be evicted on get");
    }
}
