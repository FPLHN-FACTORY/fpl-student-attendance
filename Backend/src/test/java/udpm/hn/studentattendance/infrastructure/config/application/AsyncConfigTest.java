package udpm.hn.studentattendance.infrastructure.config.application;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import static org.junit.jupiter.api.Assertions.*;

class AsyncConfigTest {
    @Test
    void testAsyncConfigExists() {
        AsyncConfig config = new AsyncConfig();
        assertNotNull(config);
    }

    @Test
    void testTaskExecutorBean() throws Exception {
        AsyncConfig config = new AsyncConfig();
        java.util.concurrent.Executor executor = config.taskExecutor();
        assertNotNull(executor);
        assertTrue(executor instanceof org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor);
    }

    @Test
    void testTaskExecutorConfiguration() throws Exception {
        AsyncConfig config = new AsyncConfig();
        java.util.concurrent.Executor executor = config.taskExecutor();
        assertNotNull(executor);
    }
}
