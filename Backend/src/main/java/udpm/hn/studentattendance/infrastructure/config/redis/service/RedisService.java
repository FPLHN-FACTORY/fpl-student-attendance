package udpm.hn.studentattendance.infrastructure.config.redis.service;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RedisService {

    void set(String key, String value);

    <T> T getObject(String key, TypeReference<T> type);

    boolean hashExist(String key, String field);

    Object get(String key);

    void delete(String key);

    void delete(String key, String field);

    void delete(String key, List<String> fields);

    void set(String key, Object value, long ttl);

    void deletePattern(String pattern);
}