package udpm.hn.studentattendance.infrastructure.redis.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service manages cache for login sessions and user information
 */
@Service
@RequiredArgsConstructor
public class SessionCacheService {

    private static final Logger logger = LoggerFactory.getLogger(SessionCacheService.class);
    private final RedisService redisService;

    @Value("${app.config.auth.expiration:1440}")
    private long sessionExpirationMinutes;

    /**
     * Cache session information
     * 
     * @param sessionId ID session
     * @param userId    ID user
     * @param userInfo  User information
     */
    public void cacheSession(String sessionId, String userId, Map<String, Object> userInfo) {
        try {
            // Create deep copy of maps to avoid reference issues
            Map<String, Object> userInfoCopy = new HashMap<>(userInfo);

            // Cache session with TTL equal to session expiration time
            redisService.set("session:" + sessionId, userId, sessionExpirationMinutes * 60);

            // Cache user info with slightly longer TTL
            redisService.set("user:" + userId, userInfoCopy, (sessionExpirationMinutes + 30) * 60);
        } catch (Exception e) {
            logger.warn("Failed to cache session: {}", e.getMessage());
        }
    }

    /**
     * Get user ID from session ID
     * 
     * @param sessionId Session ID
     * @return User ID or null if session doesn't exist
     */
    public String getUserIdFromSession(String sessionId) {
        try {
            Object userId = redisService.get("session:" + sessionId);
            return userId != null ? userId.toString() : null;
        } catch (Exception e) {
            logger.warn("Failed to get user ID from session: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get user information from ID
     * 
     * @param userId User ID
     * @return User information or null if doesn't exist
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserInfo(String userId) {
        try {
            Object userInfo = redisService.get("user:" + userId);

            if (userInfo instanceof Map) {
                // Create a deep copy to avoid reference issues
                return new HashMap<>((Map<String, Object>) userInfo);
            }

            return null;
        } catch (Exception e) {
            logger.warn("Failed to get user info: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Update user information
     * 
     * @param userId ID user
     * @param key    Information key
     * @param value  Information value
     */
    public void updateUserInfo(String userId, String key, Object value) {
        try {
            Map<String, Object> userInfo = getUserInfo(userId);
            if (userInfo == null) {
                userInfo = new HashMap<>();
            }
            userInfo.put(key, value);
            redisService.set("user:" + userId, userInfo, (sessionExpirationMinutes + 30) * 60);
        } catch (Exception e) {
            logger.warn("Failed to update user info: {}", e.getMessage());
        }
    }

    /**
     * Delete login session
     * 
     * @param sessionId Session ID
     */
    public void invalidateSession(String sessionId) {
        try {
            String userId = getUserIdFromSession(sessionId);
            if (userId != null) {
                redisService.delete("session:" + sessionId);
            }
        } catch (Exception e) {
            logger.warn("Failed to invalidate session: {}", e.getMessage());
        }
    }

    /**
     * Cache JWT token
     * 
     * @param token  JWT Token
     * @param userId User ID
     */
    public void cacheToken(String token, String userId) {
        try {
            // Cache token with TTL equal to session expiration time
            redisService.set("token:" + token, userId, sessionExpirationMinutes * 60);
        } catch (Exception e) {
            logger.warn("Failed to cache token: {}", e.getMessage());
        }
    }

    /**
     * Get user ID from token
     * 
     * @param token JWT Token
     * @return User ID or null if token doesn't exist
     */
    public String getUserIdFromToken(String token) {
        try {
            Object userId = redisService.get("token:" + token);
            return userId != null ? userId.toString() : null;
        } catch (Exception e) {
            logger.warn("Failed to get user ID from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Delete token
     * 
     * @param token JWT Token
     */
    public void invalidateToken(String token) {
        try {
            redisService.delete("token:" + token);
        } catch (Exception e) {
            logger.warn("Failed to invalidate token: {}", e.getMessage());
        }
    }

    /**
     * Clear all session data in Redis
     */
    public void clearAllSessions() {
        try {
            redisService.deletePattern("session:*");
            redisService.deletePattern("user:*");
            redisService.deletePattern("token:*");
            logger.info("Cleared all session data");
        } catch (Exception e) {
            logger.warn("Failed to clear all sessions: {}", e.getMessage());
        }
    }
}