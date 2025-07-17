package udpm.hn.studentattendance.infrastructure.security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a Redis operation is not allowed due to security
 * restrictions
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class RedisSecurityException extends RuntimeException {

    public RedisSecurityException(String message) {
        super(message);
    }

    public RedisSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}