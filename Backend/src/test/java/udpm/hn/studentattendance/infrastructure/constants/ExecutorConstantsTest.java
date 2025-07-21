package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExecutorConstantsTest {
    @Test
    void testConstantsNotNullOrEmpty() {
        assertNotNull(ExecutorConstants.TASK_EXECUTOR);
        assertFalse(ExecutorConstants.TASK_EXECUTOR.isEmpty());
    }
}
