package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置连接池
        template.setConnectionFactory(factory);
        // 设置key序列化器
        template.setKeySerializer(RedisSerializer.string());
        // 设置value序列化器
        template.setValueSerializer(RedisSerializer.json());
        // 设置hash:key序列化器
        template.setHashKeySerializer(RedisSerializer.string());
        // 设置hash:value序列化器
        template.setHashValueSerializer(RedisSerializer.json());
        // 使设置生效
        template.afterPropertiesSet();
        return template;
    }
}
