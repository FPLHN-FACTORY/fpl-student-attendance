package udpm.hn.studentattendance.infrastructure.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HasOrderNumberTest {
    static class TestHasOrderNumber implements HasOrderNumber {
        private final Long orderNumber;

        TestHasOrderNumber(Long orderNumber) {
            this.orderNumber = orderNumber;
        }

        @Override
        public Long getOrderNumber() {
            return orderNumber;
        }
    }

    @Test
    void testGetOrderNumber() {
        HasOrderNumber obj = new TestHasOrderNumber(123L);
        assertEquals(123L, obj.getOrderNumber());
    }
}