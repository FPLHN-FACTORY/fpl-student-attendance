package udpm.hn.studentattendance.core.student.attendance.repositories;

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
class SAFacilityLocationRepositoryTest {
    @Autowired
    SAFacilityLocationRepository repository;

    @Test
    void contextLoads() {
        assertThat(repository).isNotNull();
    }

    // ThÃªm test cho custom method náº¿u cÃ³
    // @Test
    // void testCustomQuery() {
    // // repository.save(...);
    // // assertThat(repository.customMethod(...)).isPresent();
    // }
}