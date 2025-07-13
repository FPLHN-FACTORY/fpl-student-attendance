package udpm.hn.studentattendance.infrastructure.redis.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);

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
        try {
            redisTemplate.opsForValue().set(key, value, redisTimeToLive, TimeUnit.DAYS);
        } catch (Exception e) {
            logger.error("Error setting Redis key: {} - {}", key, e.getMessage());
        }
    }

    @Override
    public void setObject(String key, Object value) {
        try {
            // Convert proxy objects to JSON before caching
            Object cacheValue = value;
            if (value != null) {
                if (value.getClass().getName().contains("$Proxy")) {
                    String json = objectMapper.writeValueAsString(value);
                    cacheValue = objectMapper.readValue(json, Object.class);
                } else if (value instanceof java.util.Collection) {
                    String json = objectMapper.writeValueAsString(value);
                    cacheValue = objectMapper.readValue(json, Object.class);
                } else if (value.getClass().getSimpleName().equals("PageableObject")) {
                    String json = objectMapper.writeValueAsString(value);
                    cacheValue = objectMapper.readValue(json, Object.class);
                }
            }

            redisTemplate.opsForValue().set(key, cacheValue, redisTimeToLive, TimeUnit.DAYS);
        } catch (Exception e) {
            logger.error("Error storing object in Redis: {} - {}", key, e.getMessage());
        }
    }

    @Override
    public <T> T getObject(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }

            // Handle proxy objects and complex types
            if (value instanceof String) {
                return objectMapper.readValue((String) value, clazz);
            } else {
                // Nếu đã là đối tượng, thử chuyển đổi

                // Handle proxy objects by converting to JSON first
                if (value.getClass().getName().contains("$Proxy")) {
                    String json = objectMapper.writeValueAsString(value);
                    return objectMapper.readValue(json, clazz);
                }

                // Handle collections that might contain proxy objects
                if (value instanceof java.util.Collection) {
                    String json = objectMapper.writeValueAsString(value);
                    return objectMapper.readValue(json, clazz);
                }

                return objectMapper.convertValue(value, clazz);
            }
        } catch (Exception e) {
            logger.error("Error deserializing JSON to object for key: {} - {}", key, e.getMessage());
            // Clear cache when deserialization fails
            clearCacheOnError(key);
            return null;
        }
    }

    @Override
    public void setTimeToLive(String key, long timeoutInDay) {
        try {
            redisTemplate.expire(key, timeoutInDay, TimeUnit.DAYS);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void hashSet(String key, String field, Object value) {
        try {
            hashOperations.put(key, field, value);
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

            if (value == null) {
                return null;
            }

            // If the value is a string that looks like JSON, try to deserialize it
            if (value instanceof String) {
                String strValue = (String) value;
                if (strValue.startsWith("{") || strValue.startsWith("[")) {
                    try {
                        return objectMapper.readValue(strValue, Object.class);
                    } catch (Exception e) {
                        logger.error("Error deserializing JSON string for key: {} - {}", key, e.getMessage());
                    }
                }
            }
            // Return original value if not a JSON string or deserialization failed
            return value;
        } catch (Exception e) {
            logger.error("Error getting Redis key: {} - {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, Object> getField(String key) {
        try {
            return hashOperations.entries(key);
        } catch (Exception ignored) {
            return new HashMap<>();
        }
    }

    @Override
    public Object hashGet(String key, String field) {
        try {
            return hashOperations.get(key, field);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public List<Object> hashGetByFieldPrefix(String key, String fieldPrefix) {
        List<Object> objects = new ArrayList<>();
        try {
            Map<String, Object> hashEntries = hashOperations.entries(key);
            for (Map.Entry<String, Object> entry : hashEntries.entrySet()) {
                if (entry.getKey().startsWith(fieldPrefix)) {
                    objects.add(entry.getValue());
                }
            }
        } catch (Exception ignored) {
        }
        return objects;
    }

    @Override
    public Set<String> getFieldPrefixes(String key) {
        try {
            return hashOperations.entries(key).keySet();
        } catch (Exception ignored) {
            return new HashSet<>();
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            logger.error("Error deleting Redis key: {} - {}", key, e.getMessage());
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

    /**
     * Legacy methods for backward compatibility
     */
    @Override
    public void set(String key, Object value, long ttl) {
        try {
            // Convert proxy objects to JSON before caching
            Object cacheValue = value;
            if (value != null) {
                if (value.getClass().getName().contains("$Proxy")) {
                    String json = objectMapper.writeValueAsString(value);
                    cacheValue = objectMapper.readValue(json, Object.class);
                } else if (value instanceof java.util.Collection) {
                    String json = objectMapper.writeValueAsString(value);
                    cacheValue = objectMapper.readValue(json, Object.class);
                } else if (value.getClass().getSimpleName().equals("PageableObject")) {
                    String json = objectMapper.writeValueAsString(value);
                    cacheValue = objectMapper.readValue(json, Object.class);
                }
            }

            redisTemplate.opsForValue().set(key, cacheValue, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Error setting Redis key: {} - {}", key, e.getMessage());
        }
    }

    @Override
    public void deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            logger.error("Error deleting Redis keys with pattern: {} - {}", pattern, e.getMessage());
        }
    }

    /**
     * Clear all cache when there are deserialization errors
     */
    public void clearAllCache() {
        try {
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            logger.error("Error clearing all cache: {}", e.getMessage());
        }
    }

    /**
     * Clear cache when deserialization fails
     */
    public void clearCacheOnError(String key) {
        try {
            delete(key);
        } catch (Exception e) {
            logger.error("Error clearing cache for key: {} - {}", key, e.getMessage());
        }
    }
}