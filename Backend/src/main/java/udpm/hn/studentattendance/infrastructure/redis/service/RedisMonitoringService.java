package udpm.hn.studentattendance.infrastructure.redis.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service để theo dõi hiệu suất của Redis
 */
@Service
@RequiredArgsConstructor
@Getter
public class RedisMonitoringService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Các biến theo dõi
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong cacheRequests = new AtomicLong(0);

    /**
     * Ghi nhận cache hit
     */
    public void recordHit() {
        cacheHits.incrementAndGet();
        cacheRequests.incrementAndGet();
    }

    /**
     * Ghi nhận cache miss
     */
    public void recordMiss() {
        cacheMisses.incrementAndGet();
        cacheRequests.incrementAndGet();
    }

    /**
     * Lấy thông tin hiệu suất cache
     * 
     * @return Map chứa thông tin hiệu suất
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long requests = cacheRequests.get();

        stats.put("hits", hits);
        stats.put("misses", misses);
        stats.put("requests", requests);
        stats.put("hitRatio", requests > 0 ? (double) hits / requests : 0);

        return stats;
    }

    /**
     * Lấy thông tin Redis
     * 
     * @return Map chứa thông tin Redis
     */
    public Map<String, Object> getRedisInfo() {
        Map<String, Object> info = new HashMap<>();

        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        Properties properties = connection.info();

        // Thông tin bộ nhớ
        info.put("usedMemory", properties.getProperty("used_memory"));
        info.put("usedMemoryHuman", properties.getProperty("used_memory_human"));
        info.put("usedMemoryPeak", properties.getProperty("used_memory_peak"));
        info.put("usedMemoryPeakHuman", properties.getProperty("used_memory_peak_human"));

        // Thông tin kết nối
        info.put("connectedClients", properties.getProperty("connected_clients"));
        info.put("blockedClients", properties.getProperty("blocked_clients"));

        // Thông tin keys
        info.put("dbSize", redisTemplate.getConnectionFactory().getConnection().dbSize());

        // Thông tin server
        info.put("redisVersion", properties.getProperty("redis_version"));
        info.put("uptimeInSeconds", properties.getProperty("uptime_in_seconds"));
        info.put("uptimeInDays", properties.getProperty("uptime_in_days"));

        return info;
    }

    /**
     * Lấy số lượng keys theo pattern
     * 
     * @param pattern Pattern để tìm keys
     * @return Số lượng keys
     */
    public long getKeyCount(String pattern) {
        return redisTemplate.keys(pattern).size();
    }

    /**
     * Lấy thống kê về các loại cache
     * 
     * @return Map chứa thống kê
     */
    public Map<String, Long> getCacheTypeStats() {
        Map<String, Long> stats = new HashMap<>();

        stats.put("attendance", getKeyCount("attendance:*"));
        stats.put("schedule", getKeyCount("schedule:*"));
        stats.put("statistics", getKeyCount("statistics:*"));
        stats.put("student", getKeyCount("student:*"));
        stats.put("user", getKeyCount("user:*"));
        stats.put("token", getKeyCount("token:*"));
        stats.put("session", getKeyCount("session:*"));
        stats.put("admin", getKeyCount("admin:*"));
        stats.put("teacher", getKeyCount("teacher:*"));
        stats.put("staff", getKeyCount("staff:*"));

        return stats;
    }

    /**
     * Xóa tất cả cache cũ (TTL < 10% thời gian còn lại)
     * 
     * @return Số lượng keys đã xóa
     */
    public long cleanupExpiredKeys() {
        long count = 0;
        for (String key : redisTemplate.keys("*")) {
            Long ttl = redisTemplate.getExpire(key);
            if (ttl != null && ttl < 0) {
                redisTemplate.delete(key);
                count++;
            }
        }
        return count;
    }

    /**
     * Tự động xóa cache cũ mỗi giờ
     */
    @Scheduled(fixedRate = 3600000) // 1 giờ
    public void scheduledCleanup() {
        cleanupExpiredKeys();
    }

    /**
     * Reset các biến theo dõi
     */
    public void resetStats() {
        cacheHits.set(0);
        cacheMisses.set(0);
        cacheRequests.set(0);
    }

    /**
     * Lấy TTL của một key
     * 
     * @param key Key cần lấy TTL
     * @return TTL của key (giây)
     */
    public Long getKeyTTL(String key) {
        return redisTemplate.getExpire(key);
    }
}