package udpm.hn.studentattendance.infrastructure.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.entities.base.IsIdentified;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PrimaryEntityListenerTest {
    @Mock
    private IsIdentified primaryEntity;

    private PrimaryEntityListener listener = new PrimaryEntityListener();

    @Test
    void testPrimaryEntityListenerExists() {
        assertNotNull(listener);
    }
    // No prePersist method to test
}