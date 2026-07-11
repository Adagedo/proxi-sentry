package code.adagedo.proxialertengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisConfiguration {
    @Bean
    public RedisTemplate<String, String> redisSetTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        // Serializer for the Set's key name (e.g., "proxy_sentry:known_events")
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // Serializer for the elements stored inside the Set (e.g., "EONET_123_2026-07-11")
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}
