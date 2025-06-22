package udpm.hn.studentattendance.infrastructure.redis.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import udpm.hn.studentattendance.infrastructure.redis.service.QueryCacheService;

/**
 * Interceptor để thêm header 'X-Cache-Hit' vào response
 * để chỉ ra response có từ cache hay không
 */
@Component
public class CacheHeaderInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(CacheHeaderInterceptor.class);
    
    @Autowired
    private QueryCacheService queryCacheService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        logger.debug("CacheHeaderInterceptor.preHandle for {}", request.getRequestURI());
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (handler instanceof HandlerMethod) {
            boolean isCacheHit = queryCacheService.wasCacheHit();
            logger.debug("CacheHeaderInterceptor.postHandle for {}: Cache hit = {}", request.getRequestURI(), isCacheHit);
            
            response.setHeader("X-Cache-Status", isCacheHit ? "HIT" : "MISS");
            logger.debug("Added X-Cache-Status header: {}", isCacheHit ? "HIT" : "MISS");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (handler instanceof HandlerMethod) {
            // Vì đôi khi postHandle không được gọi trong một số trường hợp như exception
            boolean isCacheHit = queryCacheService.wasCacheHit();
            logger.debug("CacheHeaderInterceptor.afterCompletion for {}: Cache hit = {}", request.getRequestURI(), isCacheHit);
            
            // Kiểm tra nếu header chưa được set trong postHandle
            if (!response.containsHeader("X-Cache-Status")) {
                response.setHeader("X-Cache-Status", isCacheHit ? "HIT" : "MISS");
                logger.debug("Added X-Cache-Status header in afterCompletion: {}", isCacheHit ? "HIT" : "MISS");
            }
        }
    }
} 