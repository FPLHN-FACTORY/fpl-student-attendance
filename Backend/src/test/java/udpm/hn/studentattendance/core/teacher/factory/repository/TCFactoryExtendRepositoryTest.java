package udpm.hn.studentattendance.core.teacher.factory.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TCFactoryExtendRepositoryTest {
    @Autowired
    private TCFactoryExtendRepository tcFactoryExtendRepository;

    @Test
    void testTCFactoryExtendRepositoryExists() {
        assertNotNull(tcFactoryExtendRepository);
    }
}