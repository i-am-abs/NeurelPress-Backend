package com.neurelpress.blogs.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neurelpress.blogs.constants.CodeConstants;
import org.springframework.cache.CacheManager;
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
import java.util.Map;

@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(new ObjectMapper()));
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(new ObjectMapper()));
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(new ObjectMapper())))
                .entryTtl(Duration.ofMinutes(Long.parseLong(String.valueOf(CodeConstants.REDIS_CACHE_TTL))));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .withInitialCacheConfigurations(Map.of(
                        CodeConstants.TRENDING, RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(CodeConstants.TIME_STAMP_MINUTE / 2)),
                        CodeConstants.QUOTE_OF_THE_DAY, RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(CodeConstants.TIME_STAMP_HOUR)),
                        CodeConstants.ARTICLE_VIEWS, RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(CodeConstants.TIME_STAMP_SECOND * 5L)
                        )))
                .build();
    }
}
