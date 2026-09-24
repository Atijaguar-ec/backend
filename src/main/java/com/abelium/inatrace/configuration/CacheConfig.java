package com.abelium.inatrace.configuration;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * HU-15 / ADR-014: Static catalog caching configuration with @EnableCaching.
 * Implements a resilient in-memory cache with 24-hour TTL (expireAfterWrite)
 * and 1000 max entries for static catalogs (currencies, certifications) to eliminate redundant database queries.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final long CATALOG_CACHE_TTL_MILLIS = TimeUnit.HOURS.toMillis(24);
    public static final int CATALOG_CACHE_MAX_SIZE = 1000;

    @Bean
    public CacheManager cacheManager() {
        return new TtlConcurrentMapCacheManager(CATALOG_CACHE_TTL_MILLIS, CATALOG_CACHE_MAX_SIZE, "currencies", "certifications");
    }

    public static class TtlConcurrentMapCacheManager extends ConcurrentMapCacheManager {
        private final long ttlMillis;
        private final int maxSize;

        public TtlConcurrentMapCacheManager(long ttlMillis, int maxSize, String... cacheNames) {
            super(cacheNames);
            this.ttlMillis = ttlMillis;
            this.maxSize = maxSize;
        }

        @Override
        protected Cache createConcurrentMapCache(String name) {
            return new TtlConcurrentMapCache(name, ttlMillis, maxSize);
        }

        public long getTtlMillis() {
            return ttlMillis;
        }

        public int getMaxSize() {
            return maxSize;
        }
    }

    public static class TtlConcurrentMapCache extends ConcurrentMapCache {
        private final long ttlMillis;
        private final int maxSize;
        private final ConcurrentMap<Object, Long> writeTimestamps = new ConcurrentHashMap<>();

        public TtlConcurrentMapCache(String name, long ttlMillis, int maxSize) {
            super(name);
            this.ttlMillis = ttlMillis;
            this.maxSize = maxSize;
        }

        @Override
        public ValueWrapper get(Object key) {
            Long timestamp = writeTimestamps.get(key);
            if (timestamp != null && (System.currentTimeMillis() - timestamp > ttlMillis)) {
                evict(key);
                return null;
            }
            return super.get(key);
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            Long timestamp = writeTimestamps.get(key);
            if (timestamp != null && (System.currentTimeMillis() - timestamp > ttlMillis)) {
                evict(key);
                return null;
            }
            return super.get(key, type);
        }

        @Override
        public void put(Object key, Object value) {
            if (getNativeCache().size() >= maxSize && !getNativeCache().containsKey(key)) {
                Object firstKey = writeTimestamps.keySet().stream().findFirst().orElse(null);
                if (firstKey != null) {
                    evict(firstKey);
                }
            }
            super.put(key, value);
            writeTimestamps.put(key, System.currentTimeMillis());
        }

        @Override
        public void evict(Object key) {
            writeTimestamps.remove(key);
            super.evict(key);
        }

        @Override
        public void clear() {
            writeTimestamps.clear();
            super.clear();
        }

        public long getTtlMillis() {
            return ttlMillis;
        }

        public void setWriteTimestampForTesting(Object key, long timestamp) {
            writeTimestamps.put(key, timestamp);
        }
    }
}
