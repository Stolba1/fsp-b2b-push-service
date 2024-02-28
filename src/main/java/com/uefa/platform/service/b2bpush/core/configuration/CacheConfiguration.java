package com.uefa.platform.service.b2bpush.core.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Cache Configuration to enable object level caching
 */
@Configuration
@EnableCaching
public class CacheConfiguration extends CachingConfigurerSupport {

    public static final String FEED_HTTP_CLIENT_FEED_BY_URL = "FeedHttpClient.getFeed";
    public static final String FEED_BY_LIVE_DATA_TYPE = "FeedRepository.getFeedByLiveDataType";

    @Bean
    @Override
    public CacheManager cacheManager() {

        CaffeineCache feedByUrl = new CaffeineCache(FEED_HTTP_CLIENT_FEED_BY_URL, Caffeine.newBuilder()
                .recordStats()
                .expireAfterWrite(90, TimeUnit.SECONDS)
                .maximumSize(5000)
                .build());
        // caching here for 10 mins
        CaffeineCache feedByCode = new CaffeineCache(FEED_BY_LIVE_DATA_TYPE, Caffeine.newBuilder()
                .recordStats()
                .expireAfterWrite(600, TimeUnit.SECONDS)
                .maximumSize(5000)
                .build());

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                feedByUrl, feedByCode));
        return manager;
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }

    @Bean
    @Override
    public CacheResolver cacheResolver() {
        return new SimpleCacheResolver(Objects.requireNonNull(cacheManager()));
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler();
    }

}
