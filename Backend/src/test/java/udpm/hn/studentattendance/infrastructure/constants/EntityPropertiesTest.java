package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EntityPropertiesTest {
    @Test
    void testConstantsNotZero() {
        assertTrue(EntityProperties.LENGTH_ID > 0);
        assertTrue(EntityProperties.LENGTH_CODE > 0);
        assertTrue(EntityProperties.LENGTH_NAME > 0);
        assertTrue(EntityProperties.LENGTH_TEXT > 0);
    }
}
