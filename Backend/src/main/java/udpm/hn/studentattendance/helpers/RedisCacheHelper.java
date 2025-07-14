package udpm.hn.studentattendance.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.util.function.Supplier;

@Component
public class RedisCacheHelper {

    private final RedisService redisService;

    public RedisCacheHelper(RedisService redisService) {
        this.redisService = redisService;
    }

    public <T> T getOrSet(String key, Supplier<T> dataSupplier, TypeReference<T> type, long ttlMillis) {
            T cached = redisService.getObject(key, type);
            if (cached != null) return cached;

            T data = dataSupplier.get();
            try {
                redisService.set(key, data, ttlMillis);
            } catch (Exception ignored) {}
            return data;
        }

}
