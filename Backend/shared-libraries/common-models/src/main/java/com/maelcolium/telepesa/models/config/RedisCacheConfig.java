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
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisCacheConfig {

    /**
     * Configure Redis template with proper serialization
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configure serializers
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configure cache manager with different TTL for different cache names
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
        cacheConfigurations.put("user-profiles", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("user-sessions", defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put("user-authentication", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Transaction service caches
        cacheConfigurations.put("transactions", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("transaction-history", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("account-balances", defaultConfig.entryTtl(Duration.ofMinutes(2)));
        cacheConfigurations.put("transaction-limits", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Loan service caches
        cacheConfigurations.put("loans", defaultConfig.entryTtl(Duration.ofMinutes(20)));
        cacheConfigurations.put("loan-applications", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("loan-calculations", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("credit-scores", defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigurations.put("collaterals", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Account service caches
        cacheConfigurations.put("accounts", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("account-details", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("account-statements", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Notification service caches
        cacheConfigurations.put("notifications", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("notification-templates", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("notification-preferences", defaultConfig.entryTtl(Duration.ofHours(1)));

        // System caches
        cacheConfigurations.put("system-config", defaultConfig.entryTtl(Duration.ofHours(24)));
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