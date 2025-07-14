package udpm.hn.studentattendance.infrastructure.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IsIdentifyTest {
    static class TestIsIdentify implements IsIdentify {
        private final String id;

        TestIsIdentify(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }
    }

    @Test
    void testGetId() {
        IsIdentify obj = new TestIsIdentify("abc123");
        assertEquals("abc123", obj.getId());
    }
}