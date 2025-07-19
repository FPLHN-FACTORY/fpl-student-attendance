package udpm.hn.studentattendance.infrastructure.config.redis.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;

    private HashOperations<String, String, Object> hashOperations;

    private final static long redisTimeToLive = 3;

    @Autowired
    public RedisServiceImpl(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void set(String key, Object value, long ttlMillis) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttlMillis, TimeUnit.MILLISECONDS);
        } catch (Exception ignored) {
        }
    }

    @Override
    public <T> T getObject(String key, TypeReference<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            String json = value.toString();
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            return null;
        }
    }

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public void set(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value, redisTimeToLive, TimeUnit.DAYS);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void delete(String key, String field) {
        try {
            hashOperations.delete(key, field);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void delete(String key, List<String> fields) {
        try {
            for (String field : fields) {
                hashOperations.delete(key, field);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean hashExist(String key, String field) {
        try {
            return hashOperations.hasKey(key, field);
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);

            if (value instanceof String strValue) {
                if (strValue.startsWith("{") || strValue.startsWith("[")) {
                    try {
                        return objectMapper.readValue(strValue, Object.class);
                    } catch (Exception ignored) {
                    }
                }
            }
            return value;
        } catch (Exception e) {
            return null;
        }
    }

}