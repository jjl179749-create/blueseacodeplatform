package org.blueseacode.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        // 注册 JavaTimeModule 以支持 LocalDateTime 等 Java 8 时间类型
        mapper.registerModule(new JavaTimeModule());
        // 禁止将日期序列化为时间戳（保持 ISO 字符串格式）
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    /**
     * 配置缓存管理器，为不同缓存设置不同 TTL
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory,
                                     GenericJackson2JsonRedisSerializer serializer) {
        // 默认配置：30分钟过期
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer))
                .disableCachingNullValues();

        // 不同缓存名称的 TTL 配置
        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();

        // 分类/标签缓存：变更极少，缓存1小时
        configMap.put("categories", defaultConfig.entryTtl(Duration.ofHours(1)));
        configMap.put("tags", defaultConfig.entryTtl(Duration.ofHours(1)));

        // 热门/推荐数据：缓存5分钟
        configMap.put("hotData", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 首页数据：缓存3分钟
        configMap.put("homeData", defaultConfig.entryTtl(Duration.ofMinutes(3)));

        // 用户信息：缓存10分钟
        configMap.put("userInfo", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configMap)
                .build();
    }
}
