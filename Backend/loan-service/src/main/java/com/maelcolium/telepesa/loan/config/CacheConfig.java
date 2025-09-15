package com.maelcolium.telepesa.loan.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache configuration for Loan Service
 * Provides Redis caching with optimized TTL for different loan data types
 */
@Configuration
//@EnableCaching - Disabled to prevent SpEL cache errors
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class CacheConfig {

    /**
     * Configure Redis template with proper serialization
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configure cache manager with different TTL for different cache types
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(20)) // Default 20 minutes
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();

        // Loan-specific cache configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Loan data caches
        cacheConfigurations.put("loans", defaultConfig.entryTtl(Duration.ofMinutes(20)));
        cacheConfigurations.put("loan-applications", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("loan-products", defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put("credit-scores", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("loan-calculations", defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}
