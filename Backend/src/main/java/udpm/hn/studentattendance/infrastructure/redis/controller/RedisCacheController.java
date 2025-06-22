package udpm.hn.studentattendance.infrastructure.redis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.redis.service.QueryCacheService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cache")
public class RedisCacheController {

    @Autowired
    private QueryCacheService queryCacheService;

    /**
     * Endpoint để test cache - không yêu cầu xác thực
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse> testCache() {
        ApiResponse response = testCacheWithKey("test-cache-key");

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Cache-Status", queryCacheService.wasCacheHit() ? "HIT" : "MISS");
        headers.add("Access-Control-Expose-Headers", "X-Cache-Status");

        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }

    /**
     * Endpoint để test cache với key tùy chỉnh - không yêu cầu xác thực
     */
    @GetMapping("/test/{key}")
    public ResponseEntity<ApiResponse> testCacheWithCustomKey(@PathVariable String key) {
        ApiResponse response = testCacheWithKey("test-" + key);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Cache-Status", queryCacheService.wasCacheHit() ? "HIT" : "MISS");
        headers.add("Access-Control-Expose-Headers", "X-Cache-Status");

        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }

    /**
     * Helper method để test cache
     */
    private ApiResponse testCacheWithKey(String cacheKey) {
        // Thử lấy từ cache
        Map<String, Object> result = queryCacheService.getCachedResult(cacheKey);

        // Nếu không có trong cache, tạo dữ liệu mới
        if (result == null) {
            result = new HashMap<>();
            result.put("timestamp", System.currentTimeMillis());
            result.put("uuid", UUID.randomUUID().toString());
            result.put("message", "This response is freshly generated");

            // Lưu vào cache trong 30 giây
            queryCacheService.cacheResult(cacheKey, result, 30);
        } else {
            // Nếu lấy từ cache, cập nhật thông điệp
            result.put("message", "This response is from cache");
        }

        return ApiResponse.success("Test cache", result);
    }

    /**
     * Lấy thông tin thống kê về cache
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("enabled", queryCacheService.isQueryCacheEnabled());
        stats.put("keyStats", queryCacheService.getCacheStats());

        ApiResponse response = ApiResponse.success("Thống kê cache", stats);

        return ResponseEntity.ok(response);
    }

    /**
     * Xóa toàn bộ cache
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse> clearCache() {
        queryCacheService.clearAllCaches();
        ApiResponse response = ApiResponse.success("Đã xóa toàn bộ cache");

        return ResponseEntity.ok(response);
    }
}