package udpm.hn.studentattendance.infrastructure.common.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class CommonUserStudentRepositoryTest {
    @Autowired
    CommonUserStudentRepository repository;

    @Test
    void contextLoads() {
        assertThat(repository).isNotNull();
    }

    // Thêm test cho custom method nếu có
    // @Test
    // void testCustomQuery() {
    // // repository.save(...);
    // // assertThat(repository.customMethod(...)).isPresent();
    // }
}