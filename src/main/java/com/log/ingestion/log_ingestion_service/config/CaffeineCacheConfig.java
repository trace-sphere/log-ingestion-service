package com.log.ingestion.log_ingestion_service.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CaffeineCacheConfig {

    @Value("${api.key.cache.name}")
    private String cacheName;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(cacheName);

        manager.setCaffeine(
                Caffeine.newBuilder()
                        .maximumSize(10000)
                        .expireAfterAccess(Duration.ofHours(1))
                        .recordStats());
        return manager;
    }
}
