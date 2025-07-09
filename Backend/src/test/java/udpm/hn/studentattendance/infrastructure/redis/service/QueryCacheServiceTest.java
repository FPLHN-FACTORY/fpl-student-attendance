package udpm.hn.studentattendance.infrastructure.redis.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QueryCacheServiceTest {
    @Test
    void testClassExists() {
        assertNotNull(QueryCacheService.class);
    }
}