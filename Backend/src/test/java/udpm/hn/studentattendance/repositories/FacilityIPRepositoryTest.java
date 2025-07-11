package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestDatabaseConfig.class)
@ActiveProfiles("test")
class FacilityIPRepositoryTest {

    @Test
    void testFacilityIPRepositoryExists() {
        assertNotNull(FacilityIPRepository.class);
    }

    @Test
    void testFindByFacility() throws Exception {
        assertNotNull(FacilityIPRepository.class.getMethod("findByFacility",
                Class.forName("udpm.hn.studentattendance.entities.Facility")));
    }

    @Test
    void testFindByIpAddress() throws Exception {
        assertNotNull(FacilityIPRepository.class.getMethod("findByIpAddress", String.class));
    }

    @Test
    void testFindByFacilityAndIpAddress() throws Exception {
        assertNotNull(FacilityIPRepository.class.getMethod("findByFacilityAndIpAddress",
                Class.forName("udpm.hn.studentattendance.entities.Facility"), String.class));
    }

    @Test
    void testFindByStatus() throws Exception {
        assertNotNull(FacilityIPRepository.class.getMethod("findByStatus",
                Class.forName("udpm.hn.studentattendance.infrastructure.constants.EntityStatus")));
    }
}