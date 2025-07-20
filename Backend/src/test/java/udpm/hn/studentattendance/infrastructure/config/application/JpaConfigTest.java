package udpm.hn.studentattendance.infrastructure.config.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.sql.DataSource;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JpaConfigTest {
    @Mock
    private DataSource dataSource;

    @Test
    void testClassExists() {
        assertNotNull(new JpaConfig(dataSource));
    }
}
