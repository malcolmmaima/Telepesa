package com.maelcolium.telepesa.models.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
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
 * Shared Redis cache configuration for all Telepesa services
 * Provides consistent caching behavior across microservices
 */
@Configuration
//@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisCacheConfig {

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
            .entryTtl(Duration.ofMinutes(30)) // Default 30 minutes
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();

        // Service-specific cache configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // User service caches
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("user-profiles", defaultConfig.entryTtl(Duration.ofMinutes(20)));
        cacheConfigurations.put("user-sessions", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Account service caches
        cacheConfigurations.put("accounts", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("account-balances", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("account-transactions", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Transaction service caches
        cacheConfigurations.put("transactions", defaultConfig.entryTtl(Duration.ofMinutes(20)));
        cacheConfigurations.put("transaction-history", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("transaction-stats", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Transfer service caches
        cacheConfigurations.put("transfers", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("transfer-history", defaultConfig.entryTtl(Duration.ofMinutes(25)));
        cacheConfigurations.put("transfer-limits", defaultConfig.entryTtl(Duration.ofMinutes(60)));

        // Bill payment service caches
        cacheConfigurations.put("bill-payments", defaultConfig.entryTtl(Duration.ofMinutes(20)));
        cacheConfigurations.put("bill-providers", defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put("bill-categories", defaultConfig.entryTtl(Duration.ofHours(4)));

        // Loan service caches
        cacheConfigurations.put("loans", defaultConfig.entryTtl(Duration.ofMinutes(20)));
        cacheConfigurations.put("loan-applications", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("loan-products", defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put("credit-scores", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("loan-calculations", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Notification service caches
        cacheConfigurations.put("notifications", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("notification-templates", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("notification-preferences", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Security and rate limiting caches
        cacheConfigurations.put("rate-limits", defaultConfig.entryTtl(Duration.ofMinutes(1)));
        cacheConfigurations.put("security-tokens", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }

    /**
     * Configure ObjectMapper for Redis serialization
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }
} 