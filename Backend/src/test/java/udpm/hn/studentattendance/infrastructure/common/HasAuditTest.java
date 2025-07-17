package udpm.hn.studentattendance.infrastructure.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HasAuditTest {
    static class TestHasAudit implements HasAudit {
        private final Long createdAt;
        private final Long updatedAt;

        TestHasAudit(Long createdAt, Long updatedAt) {
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        @Override
        public Long getCreatedAt() {
            return createdAt;
        }

        @Override
        public Long getUpdatedAt() {
            return updatedAt;
        }
    }

    @Test
    void testGetCreatedAtAndUpdatedAt() {
        HasAudit obj = new TestHasAudit(100L, 200L);
        assertEquals(100L, obj.getCreatedAt());
        assertEquals(200L, obj.getUpdatedAt());
    }
}