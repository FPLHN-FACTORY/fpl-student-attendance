package udpm.hn.studentattendance.infrastructure.redis.serializer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CacheWrapperTest {
    @Test
    void testCacheWrapperCreation() {
        String testData = "test string";
        CacheWrapper<String> wrapper = CacheWrapper.of(testData);
        assertEquals(testData, wrapper.getData());
        assertEquals(String.class, wrapper.getType());
        assertTrue(wrapper.getTimestamp() > 0);
    }

    @Test
    void testCacheWrapperWithNull() {
        CacheWrapper<String> wrapper = CacheWrapper.of(null);
        assertNull(wrapper.getData());
        assertNull(wrapper.getType());
        assertTrue(wrapper.getTimestamp() > 0);
    }

    @Test
    void testCacheWrapperConstructor() {
        String testData = "test data";
        CacheWrapper<String> wrapper = new CacheWrapper<>(testData, String.class);
        assertEquals(testData, wrapper.getData());
        assertEquals(String.class, wrapper.getType());
        assertTrue(wrapper.getTimestamp() > 0);
    }
}