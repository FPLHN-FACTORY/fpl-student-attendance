package udpm.hn.studentattendance.infrastructure.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.infrastructure.common.HasAudit;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuditEntityListenerTest {
    @Mock
    private HasAudit auditEntity;

    private AuditEntityListener listener = new AuditEntityListener();

    @Test
    void testAuditEntityListenerExists() {
        assertNotNull(listener);
    }
    // No prePersist or preUpdate methods to test
}
