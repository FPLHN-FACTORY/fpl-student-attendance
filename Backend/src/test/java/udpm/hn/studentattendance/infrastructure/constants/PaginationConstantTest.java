package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaginationConstantTest {
    @Test
    void testConstantsNotNullOrEmpty() {
        assertTrue(PaginationConstant.DEFAULT_PAGE > 0);
        assertTrue(PaginationConstant.DEFAULT_SIZE > 0);
        assertNotNull(PaginationConstant.DEFAULT_ORDER_BY);
        assertFalse(PaginationConstant.DEFAULT_ORDER_BY.isEmpty());
        assertNotNull(PaginationConstant.DEFAULT_SORT_BY);
        assertFalse(PaginationConstant.DEFAULT_SORT_BY.isEmpty());
    }
}