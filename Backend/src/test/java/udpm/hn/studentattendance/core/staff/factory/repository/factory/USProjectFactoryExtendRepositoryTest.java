package udpm.hn.studentattendance.core.staff.factory.repository.factory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(TestDatabaseConfig.class)
@ActiveProfiles("test")
class USProjectFactoryExtendRepositoryTest {
    @Autowired
    USProjectFactoryExtendRepository repository;

    @Test
    void contextLoads() {
        assertThat(repository).isNotNull();
    }
    // ThÃªm test cho custom method náº¿u cÃ³
}