package udpm.hn.studentattendance.infrastructure.redis.service.impl;

import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value, redisTimeToLive, TimeUnit.DAYS);
    }

    @Override
    public void setObject(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value, redisTimeToLive, TimeUnit.DAYS);
        } catch (Exception e) {
            System.err.println("Error storing object in Redis: " + e.getMessage());
        }
    }

    @Override
    public <T> T getObject(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }

            if (value instanceof String) {
                return objectMapper.readValue((String) value, clazz);
            } else {
                // Nếu đã là đối tượng, thử chuyển đổi
                return objectMapper.convertValue(value, clazz);
            }
        } catch (Exception e) {
            System.err.println("Error deserializing JSON to object: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void setTimeToLive(String key, long timeoutInDay) {
        redisTemplate.expire(key, timeoutInDay, TimeUnit.DAYS);
    }

    @Override
    public void hashSet(String key, String field, Object value) {
        hashOperations.put(key, field, value);
    }

    @Override
    public boolean hashExist(String key, String field) {
        return hashOperations.hasKey(key, field);
    }

    @Override
    public Object get(String key) {
        Object value = redisTemplate.opsForValue().get(key);

        // If the value is a string that looks like JSON, try to deserialize it
        if (value instanceof String) {
            String strValue = (String) value;
            if (strValue.startsWith("{") || strValue.startsWith("[")) {
                try {
                    return objectMapper.readValue(strValue, Object.class);
                } catch (Exception e) {
                    System.err.println("Error deserializing JSON string: " + e.getMessage());
                }
            }
        }

        // Return original value if not a JSON string or deserialization failed
        return value;
    }

    @Override
    public Map<String, Object> getField(String key) {
        return hashOperations.entries(key);
    }

    @Override
    public Object hashGet(String key, String field) {
        return hashOperations.get(key, field);
    }

    @Override
    public List<Object> hashGetByFieldPrefix(String key, String fieldPrefix) {
        List<Object> objects = new ArrayList<>();

        Map<String, Object> hashEntries = hashOperations.entries(key);
        for (Map.Entry<String, Object> entry : hashEntries.entrySet()) {
            if (entry.getKey().startsWith(fieldPrefix)) {
                objects.add(entry.getValue());
            }
        }

        return objects;
    }

    @Override
    public Set<String> getFieldPrefixes(String key) {
        return hashOperations.entries(key).keySet();
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void delete(String key, String field) {
        hashOperations.delete(key, field);
    }

    @Override
    public void delete(String key, List<String> fields) {
        for (String field : fields) {
            hashOperations.delete(key, field);
        }
    }

    /**
     * Legacy methods for backward compatibility
     */
    @Override
    public void set(String key, Object value, long ttl) {
        redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
    }

    @Override
    public void deletePattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}