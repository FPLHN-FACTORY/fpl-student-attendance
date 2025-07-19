package udpm.hn.studentattendance.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;

import java.util.function.Supplier;

@Component
public class RedisCacheHelper {

    private final RedisService redisService;

    @Value("${spring.cache.redis.time-to-live}")
    private long redisTTL;

    public RedisCacheHelper(RedisService redisService) {
        this.redisService = redisService;
    }

    public <T> T getOrSet(String key, Supplier<T> dataSupplier, TypeReference<T> type) {
            T cached = redisService.getObject(key, type);
            if (cached != null) return cached;

            T data = dataSupplier.get();
            try {
                redisService.set(key, data, redisTTL);
            } catch (Exception ignored) {}
            return data;
        }

}
