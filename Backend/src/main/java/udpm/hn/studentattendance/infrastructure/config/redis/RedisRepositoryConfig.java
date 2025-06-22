//package udpm.hn.studentattendance.infrastructure.config.redis;
//
//import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Import;
//import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
//
////@Configuration
////@EnableRedisRepositories(basePackages = "udpm.hn.studentattendance.infrastructure.redis")
////@Import(RedisRepositoriesAutoConfiguration.class)
////public class RedisRepositoryConfig {
////    // Cấu hình này giới hạn việc quét Redis repositories chỉ trong package cụ thể
////    // để tránh Spring Data cố gắng khởi tạo các JPA repository dưới dạng Redis
////    // Repository
////}