package udpm.hn.studentattendance.infrastructure.redis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisMonitoringService;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller để quản lý và theo dõi Redis
 * Chỉ admin mới có quyền truy cập
 */
@RestController
@RequestMapping(RouteAdminConstant.URL_API_REDIS)
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class RedisMonitoringController {

    private final RedisMonitoringService redisMonitoringService;

    private final RedisService redisService;

    private final UserActivityLogHelper userActivityLogHelper;

    /**
     * Lấy thông tin tổng quan về Redis
     *
     * @return Thông tin Redis
     */
    @GetMapping("/info")
    public ResponseEntity<?> getRedisInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("info", redisMonitoringService.getRedisInfo());
        result.put("stats", redisMonitoringService.getCacheStats());
        result.put("typeStats", redisMonitoringService.getCacheTypeStats());

        return RouterHelper.responseSuccess("Lấy thông tin Redis thành công", result);
    }

    /**
     * Xóa tất cả cache
     *
     * @return Kết quả xóa cache
     */
    @DeleteMapping("/cache/all")
    public ResponseEntity<?> clearAllCache() {
        redisService.deletePattern("*");
        userActivityLogHelper.saveLog("vừa xóa tất cả cache Redis");
        return RouterHelper.responseSuccess("Xóa tất cả cache thành công");
    }

    /**
     * Xóa cache theo pattern
     *
     * @param pattern Pattern để tìm keys
     * @return Kết quả xóa cache
     */
    @DeleteMapping("/cache")
    public ResponseEntity<?> clearCacheByPattern(@RequestParam String pattern) {
        long countBefore = redisMonitoringService.getKeyCount(pattern);
        redisService.deletePattern(pattern);
        long countAfter = redisMonitoringService.getKeyCount(pattern);

        userActivityLogHelper.saveLog("vừa xóa cache Redis theo pattern: " + pattern);

        Map<String, Object> result = new HashMap<>();
        result.put("pattern", pattern);
        result.put("deletedKeys", countBefore - countAfter);

        return RouterHelper.responseSuccess("Xóa cache theo pattern thành công", result);
    }

    /**
     * Xóa cache cũ
     *
     * @return Kết quả xóa cache
     */
    @DeleteMapping("/cache/expired")
    public ResponseEntity<?> clearExpiredCache() {
        long count = redisMonitoringService.cleanupExpiredKeys();

        userActivityLogHelper.saveLog("vừa xóa " + count + " cache Redis hết hạn");

        Map<String, Object> result = new HashMap<>();
        result.put("deletedKeys", count);

        return RouterHelper.responseSuccess("Xóa cache hết hạn thành công", result);
    }

    /**
     * Reset thống kê cache
     *
     * @return Kết quả reset
     */
    @PostMapping("/stats/reset")
    public ResponseEntity<?> resetCacheStats() {
        redisMonitoringService.resetStats();
        userActivityLogHelper.saveLog("vừa reset thống kê cache Redis");

        return RouterHelper.responseSuccess("Reset thống kê cache thành công");
    }

    /**
     * Lấy giá trị của một key
     *
     * @param key Key cần lấy giá trị
     * @return Giá trị của key
     */
    @GetMapping("/key")
    public ResponseEntity<?> getKeyValue(@RequestParam String key) {
        Object value = redisService.get(key);

        if (value == null) {
            return RouterHelper.responseError("Key không tồn tại");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("value", value);
        // Sử dụng phương thức getKeyTTL
        Long ttl = redisMonitoringService.getKeyTTL(key);
        result.put("ttl", ttl);

        return RouterHelper.responseSuccess("Lấy giá trị key thành công", result);
    }

    /**
     * Xóa một key
     *
     * @param key Key cần xóa
     * @return Kết quả xóa key
     */
    @DeleteMapping("/key")
    public ResponseEntity<?> deleteKey(@RequestParam String key) {
        boolean exists = redisService.hasKey(key);

        if (!exists) {
            return RouterHelper.responseError("Key không tồn tại");
        }

        redisService.delete(key);
        userActivityLogHelper.saveLog("vừa xóa key Redis: " + key);

        return RouterHelper.responseSuccess("Xóa key thành công");
    }

    /**
     * Cập nhật TTL của một key
     *
     * @param key Key cần cập nhật
     * @param ttl TTL mới (giây)
     * @return Kết quả cập nhật TTL
     */
    @PutMapping("/key/ttl")
    public ResponseEntity<?> updateKeyTTL(@RequestParam String key, @RequestParam long ttl) {
        boolean exists = redisService.hasKey(key);

        if (!exists) {
            return RouterHelper.responseError("Key không tồn tại");
        }

        redisService.expire(key, ttl);
        userActivityLogHelper.saveLog("vừa cập nhật TTL của key Redis: " + key + " thành " + ttl + " giây");

        return RouterHelper.responseSuccess("Cập nhật TTL thành công");
    }
}