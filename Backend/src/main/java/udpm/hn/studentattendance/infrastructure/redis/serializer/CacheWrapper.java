package udpm.hn.studentattendance.infrastructure.redis.serializer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;


public class CacheWrapper<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final T data;
    private final Class<?> type;
    private final long timestamp;

    @JsonCreator
    public CacheWrapper(
            @JsonProperty("data") T data,
            @JsonProperty("type") Class<?> type) {
        this.data = data;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> CacheWrapper<T> of(T data) {
        return new CacheWrapper<>(data, data != null ? data.getClass() : null);
    }

    public T getData() {
        return data;
    }

    public Class<?> getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }
}