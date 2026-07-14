package code.adagedo.proxialertengine.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


// this class sits here for testing purposes
//@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    private void clearCache() {
        assert redisTemplate.getConnectionFactory() != null;
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }
}
