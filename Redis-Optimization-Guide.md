# Hướng dẫn Tối ưu hệ thống FPL Student Attendance với Redis

## Giới thiệu

Tài liệu này cung cấp hướng dẫn chi tiết về việc tối ưu hệ thống FPL Student Attendance bằng Redis. Redis được sử dụng như một giải pháp cache để giảm tải cho database, cải thiện thời gian phản hồi và tăng khả năng mở rộng của hệ thống.

## Cấu trúc đã triển khai

Hiện tại, hệ thống đã được tích hợp Redis với các thành phần sau:

1. **Docker Compose**: Đã thêm Redis service vào docker-compose.yml
2. **Configuration**: Đã cấu hình Redis trong application.properties
3. **Redis Service**: Đã tạo RedisService để quản lý các thao tác với Redis
4. **Cache Manager**: Đã tạo CacheManager để quản lý và xóa cache một cách tập trung
5. **Session Cache**: Đã tạo SessionCacheService để quản lý cache cho phiên đăng nhập
6. **Redis Monitoring**: Đã tạo RedisMonitoringService và RedisMonitoringController để theo dõi và quản lý Redis
7. **Tích hợp vào các service chính**:
   - STDStatisticsServiceImpl (Thống kê)
   - STDScheduleAttendanceServiceImpl (Lịch học)
   - ADStatisticsServiceImpl (Thống kê Admin)
   - STStudentServiceImpl (Quản lý sinh viên)
   - ADSubjectManagementServiceImpl (Quản lý môn học)
   - TCTeachingScheduleServiceImpl (Lịch dạy của giảng viên)
   - SSStatisticsServiceImpl (Thống kê của nhân viên)

## Các service cần tối ưu thêm

Các service sau đây cần được tối ưu thêm bằng Redis:

### 1. Module Teacher

- **TeacherStudentAttendanceServiceImpl**: Quản lý điểm danh sinh viên của giảng viên
- **TCFactoryServiceImpl**: Quản lý xưởng của giảng viên

### 2. Module Staff

- **USStudentFactoryServiceImpl**: Quản lý sinh viên trong xưởng
- **STAttendanceRecoveryServiceImpl**: Quản lý điểm danh bù

### 3. Module Admin

- **ADUserAdminServiceImpl**: Quản lý người dùng admin
- **ADStaffServiceImpl**: Quản lý nhân viên
- **AFFacilityServiceImpl**: Quản lý cơ sở

## Chiến lược Cache

### 1. Cache Key Pattern

Sử dụng các pattern sau cho cache key:

- **Lịch học**: `schedule:list:{userId}:{requestParams}`
- **Thống kê**: `statistics:{userId}:{semesterId}`
- **Xưởng**: `factory:{userId}:{requestParams}`
- **Sinh viên**: `student:{facilityId}:{requestParams}`
- **Người dùng**: `user:{userId}`
- **Token**: `token:{tokenValue}`
- **Phiên**: `session:{sessionId}`
- **Môn học**: `admin:subject:{id}`
- **Cơ sở**: `admin:facility:{id}`

### 2. TTL (Time To Live)

- **Dữ liệu thường xuyên thay đổi**: 30 phút - 1 giờ
- **Dữ liệu ít thay đổi**: 2 - 4 giờ
- **Dữ liệu tham chiếu**: 6 - 12 giờ
- **Thông tin người dùng**: 24 giờ
- **Thống kê**: 3 - 6 giờ

### 3. Invalidation Strategy

- **Invalidate khi có thay đổi**: Xóa cache khi dữ liệu được cập nhật
- **Pattern-based invalidation**: Sử dụng `redisService.deletePattern("pattern:*")` để xóa nhiều key cùng lúc
- **TTL-based expiration**: Để Redis tự động xóa cache khi hết hạn
- **Scheduled cleanup**: Sử dụng `@Scheduled` để xóa cache cũ định kỳ

## Khi nào nên và không nên cache

### Nên cache:

1. **Dữ liệu đọc nhiều, thay đổi ít**:
   - Danh sách sinh viên trong lớp/xưởng
   - Thông tin môn học, cơ sở
   - Lịch học/lịch dạy

2. **Kết quả tính toán tốn kém**:
   - Báo cáo thống kê tổng hợp
   - Dữ liệu phân tích

3. **Session / Token**:
   - Thông tin phiên đăng nhập
   - JWT tokens

4. **Dữ liệu tham chiếu**:
   - Danh mục, lookup tables
   - Cấu hình hệ thống

### Không nên cache:

1. **Dữ liệu điểm danh realtime**:
   - Thao tác điểm danh trực tiếp
   - Dữ liệu điểm danh cần độ chính xác cao
   - Trạng thái điểm danh hiện tại

2. **Dữ liệu write-heavy & time-sensitive**:
   - Thông tin cập nhật liên tục
   - Dữ liệu cần độ chính xác thời gian

3. **Dữ liệu đa dạng theo từng người dùng**:
   - Queries tùy biến sâu với hit rate thấp

4. **Business logic phức tạp**:
   - Logic cần invalidate cache phức tạp

## Hướng dẫn triển khai

### 1. Tối ưu một service mới

```java
@Service
@RequiredArgsConstructor
public class ExampleServiceImpl implements ExampleService {

    private final ExampleRepository repository;
    private final SessionHelper sessionHelper;
    private final RedisService redisService;
    
    @Value("${spring.cache.redis.time-to-live:3600}")
    private long redisTTL;

    @Override
    public ResponseEntity<?> getData(ExampleRequest request) {
        // Tạo cache key
        String cacheKey = "example:data:" + sessionHelper.getUserId() + ":" + request.toString();
        
        // Thử lấy từ cache
        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            return RouterHelper.responseSuccess("Lấy dữ liệu thành công (cached)", cachedData);
        }
        
        // Nếu không có trong cache, truy vấn từ database
        Object result = repository.getData(request);
        
        // Lưu vào cache
        redisService.set(cacheKey, result, redisTTL);
        
        return RouterHelper.responseSuccess("Lấy dữ liệu thành công", result);
    }
    
    @Override
    public ResponseEntity<?> updateData(ExampleUpdateRequest request) {
        // Xử lý cập nhật dữ liệu
        Object result = repository.updateData(request);
        
        // Xóa cache liên quan
        invalidateCache(request.getId());
        
        return RouterHelper.responseSuccess("Cập nhật dữ liệu thành công", result);
    }
    
    private void invalidateCache(String id) {
        // Xóa cache theo pattern
        redisService.deletePattern("example:data:*:" + id + ":*");
    }
}
```

### 2. Tối ưu Authentication

```java
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final SessionCacheService sessionCacheService;
    
    public String generateToken(User user) {
        // Tạo token
        String token = jwtUtil.generateToken(user);
        
        // Cache thông tin user
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getName());
        userInfo.put("role", user.getRole());
        
        // Lưu vào cache
        sessionCacheService.cacheToken(token, user.getId());
        sessionCacheService.cacheSession(token, user.getId(), userInfo);
        
        return token;
    }
    
    public boolean validateToken(String token) {
        // Kiểm tra token trong cache
        String userId = sessionCacheService.getUserIdFromToken(token);
        if (userId != null) {
            return true;
        }
        
        // Nếu không có trong cache, kiểm tra JWT
        boolean isValid = jwtUtil.validateToken(token);
        
        // Nếu token hợp lệ, lưu vào cache
        if (isValid) {
            String userId = jwtUtil.getUserIdFromToken(token);
            sessionCacheService.cacheToken(token, userId);
        }
        
        return isValid;
    }
}
```

### 3. Tối ưu API Response

```java
@RestController
@RequestMapping("/api/v1/example")
public class ExampleController {

    private final ExampleService service;
    private final RedisService redisService;
    
    @Value("${spring.cache.redis.time-to-live:3600}")
    private long redisTTL;
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        // Tạo cache key
        String cacheKey = "api:example:" + id;
        
        // Thử lấy từ cache
        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            return ResponseEntity.ok(cachedData);
        }
        
        // Nếu không có trong cache, gọi service
        ResponseEntity<?> response = service.getById(id);
        
        // Lưu vào cache nếu thành công
        if (response.getStatusCode().is2xxSuccessful()) {
            redisService.set(cacheKey, response.getBody(), redisTTL);
        }
        
        return response;
    }
}
```

## Monitoring và Maintenance

### 1. Redis Monitoring Service

Hệ thống đã tích hợp RedisMonitoringService để theo dõi hiệu suất của Redis:

```java
@Service
@RequiredArgsConstructor
@Getter
public class RedisMonitoringService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    // Các biến theo dõi
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong cacheRequests = new AtomicLong(0);
    
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("hits", cacheHits.get());
        stats.put("misses", cacheMisses.get());
        stats.put("requests", cacheRequests.get());
        stats.put("hitRatio", cacheRequests.get() > 0 ? (double) cacheHits.get() / cacheRequests.get() : 0);
        return stats;
    }
    
    public Map<String, Object> getRedisInfo() {
        // Lấy thông tin Redis
    }
    
    public Map<String, Long> getCacheTypeStats() {
        // Lấy thống kê về các loại cache
    }
    
    @Scheduled(fixedRate = 3600000) // 1 giờ
    public void scheduledCleanup() {
        cleanupExpiredKeys();
    }
}
```

### 2. Redis Monitoring Controller

Hệ thống đã tích hợp RedisMonitoringController để quản lý Redis thông qua API:

```java
@RestController
@RequestMapping("/api/v1/admin/redis")
@PreAuthorize("hasAuthority('ADMIN')")
public class RedisMonitoringController {

    private final RedisMonitoringService redisMonitoringService;
    private final RedisService redisService;
    
    @GetMapping("/info")
    public ResponseEntity<?> getRedisInfo() {
        // Lấy thông tin Redis
    }
    
    @DeleteMapping("/cache/all")
    public ResponseEntity<?> clearAllCache() {
        // Xóa tất cả cache
    }
    
    @DeleteMapping("/cache")
    public ResponseEntity<?> clearCacheByPattern(@RequestParam String pattern) {
        // Xóa cache theo pattern
    }
    
    @DeleteMapping("/cache/expired")
    public ResponseEntity<?> clearExpiredCache() {
        // Xóa cache cũ
    }
    
    @GetMapping("/key")
    public ResponseEntity<?> getKeyValue(@RequestParam String key) {
        // Lấy giá trị của một key
    }
    
    @DeleteMapping("/key")
    public ResponseEntity<?> deleteKey(@RequestParam String key) {
        // Xóa một key
    }
    
    @PutMapping("/key/ttl")
    public ResponseEntity<?> updateKeyTTL(@RequestParam String key, @RequestParam long ttl) {
        // Cập nhật TTL của một key
    }
}
```

### 3. Theo dõi Redis

- **Memory Usage**: Theo dõi lượng bộ nhớ sử dụng của Redis thông qua RedisMonitoringService
- **Hit Rate**: Tỷ lệ cache hit/miss được theo dõi bởi RedisMonitoringService
- **Connections**: Số lượng kết nối đến Redis được hiển thị trong thông tin Redis
- **Latency**: Độ trễ của các lệnh Redis có thể được theo dõi thông qua Redis INFO command

### 4. Bảo trì

- **Xóa cache định kỳ**: Sử dụng scheduledCleanup() trong RedisMonitoringService
- **Cập nhật TTL**: Sử dụng API updateKeyTTL để điều chỉnh TTL
- **Backup**: Cấu hình Redis persistence để đảm bảo dữ liệu không bị mất khi restart
- **Theo dõi hiệu suất**: Sử dụng API getRedisInfo để theo dõi hiệu suất Redis

## Best Practices

1. **Không cache dữ liệu nhạy cảm**: Tránh lưu thông tin nhạy cảm trong Redis
2. **Sử dụng TTL phù hợp**: Đặt TTL phù hợp với tần suất thay đổi dữ liệu
3. **Invalidate cache khi cần thiết**: Xóa cache khi dữ liệu thay đổi
4. **Sử dụng pattern-based invalidation**: Xóa nhiều key cùng lúc khi cần thiết
5. **Cache warming**: Khởi tạo cache cho dữ liệu quan trọng khi khởi động hệ thống
6. **Tránh cache quá nhiều**: Chỉ cache dữ liệu thường xuyên được truy cập
7. **Sử dụng compression**: Nén dữ liệu lớn trước khi lưu vào Redis
8. **Theo dõi hiệu suất**: Sử dụng RedisMonitoringService để theo dõi hiệu suất cache
9. **Xóa cache cũ định kỳ**: Sử dụng scheduled cleanup để giải phóng bộ nhớ
10. **Phân tích hit rate**: Điều chỉnh chiến lược cache dựa trên tỷ lệ hit/miss

## Kết luận

Việc tối ưu hệ thống FPL Student Attendance bằng Redis giúp cải thiện hiệu suất, giảm tải cho database và tăng khả năng mở rộng của hệ thống. Bằng cách áp dụng các chiến lược cache phù hợp và sử dụng các công cụ monitoring, hệ thống có thể xử lý nhiều người dùng đồng thời hơn và phản hồi nhanh hơn.

Các tính năng mới như RedisMonitoringService và RedisMonitoringController giúp quản trị viên theo dõi và quản lý Redis một cách hiệu quả, đảm bảo hệ thống luôn hoạt động ổn định và tối ưu. 