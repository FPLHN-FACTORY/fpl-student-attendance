package udpm.hn.studentattendance.infrastructure.redis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, Object value, long ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("Unable to set Redis key {}: {}", key, e.getMessage());
        }
    }

    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.warn("Unable to get Redis key {}: {}", key, e.getMessage());
            return null;
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            logger.warn("Unable to delete Redis key {}: {}", key, e.getMessage());
        }
    }

    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.warn("Unable to check Redis key {}: {}", key, e.getMessage());
            return false;
        }
    }

    public void deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            logger.warn("Unable to delete pattern {}: {}", pattern, e.getMessage());
        }
    }

    public void setHash(String key, String hashKey, Object value) {
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
        } catch (Exception e) {
            logger.warn("Unable to set Redis hash {}.{}: {}", key, hashKey, e.getMessage());
        }
    }

    public void setHashAll(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
        } catch (Exception e) {
            logger.warn("Unable to set Redis hash {}: {}", key, e.getMessage());
        }
    }

    public void expire(String key, long ttl) {
        try {
            redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("Unable to set expiration for key {}: {}", key, e.getMessage());
        }
    }

    public Object getHash(String key, String hashKey) {
        try {
            return redisTemplate.opsForHash().get(key, hashKey);
        } catch (Exception e) {
            logger.warn("Unable to get Redis hash {}.{}: {}", key, hashKey, e.getMessage());
            return null;
        }
    }

    public Map<Object, Object> getHashAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            logger.warn("Unable to get all Redis hash {}: {}", key, e.getMessage());
            return Map.of();
        }
    }

    public List<String> getAllKeys(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            return keys != null ? new ArrayList<>(keys) : new ArrayList<>();
        } catch (Exception e) {
            logger.warn("Unable to get keys for pattern {}: {}", pattern, e.getMessage());
            return new ArrayList<>();
        }
    }

    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            logger.warn("Unable to increment counter {}: {}", key, e.getMessage());
            return null;
        }
    }

    public void addToList(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception e) {
            logger.warn("Unable to add to list {}: {}", key, e.getMessage());
        }
    }

    public List<Object> getList(String key) {
        try {
            Long size = redisTemplate.opsForList().size(key);
            return size != null ? redisTemplate.opsForList().range(key, 0, size - 1) : new ArrayList<>();
        } catch (Exception e) {
            logger.warn("Unable to get list {}: {}", key, e.getMessage());
            return new ArrayList<>();
        }
    }

    public void flushAll() {
        try {
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            logger.info("Flushed all Redis data");
        } catch (Exception e) {
            logger.warn("Unable to flush all Redis data: {}", e.getMessage());
        }
    }
}