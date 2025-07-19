package udpm.hn.studentattendance.infrastructure.redis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class QueryCacheService {

    private final RedisService redisService;

    public QueryCacheService(RedisService redisService,
            @Value("${app.config.cache.enable-query-cache:true}") boolean enableQueryCache) {
        this.redisService = redisService;
    }

    public void clearAllCaches() {
        try {
            redisService.deletePattern("query:*");
        } catch (Exception e) {
        }
    }
}