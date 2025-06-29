package udpm.hn.studentattendance.infrastructure.redis.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RedisService {

    void set(String key, String value);

    /**
     * Lưu đối tượng vào Redis dưới dạng JSON
     */
    void setObject(String key, Object value);

    /**
     * Lấy đối tượng từ Redis và chuyển đổi từ JSON sang kiểu T
     */
    <T> T getObject(String key, Class<T> clazz);

    void setTimeToLive(String key, long timeoutInDay);

    void hashSet(String key, String field, Object value);

    boolean hashExist(String key, String field);

    Object get(String key);

    Map<String, Object> getField(String key);

    Object hashGet(String key, String field);

    List<Object> hashGetByFieldPrefix(String key, String fieldPrefix);

    Set<String> getFieldPrefixes(String key);

    void delete(String key);

    void delete(String key, String field);

    void delete(String key, List<String> fields);

    /**
     * Legacy methods for backward compatibility
     */
    void set(String key, Object value, long ttl);

    void deletePattern(String pattern);
}