package com.smartreminder.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache Configuration
 * 
 * Enables in-memory caching for frequently queried, low-mutation data
 * (individual reminder lookups, admin system metrics, and user profiles).
 * Reduces unnecessary database round-trips and lowers query latency.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("reminders", "adminStats", "users");
    }
}
