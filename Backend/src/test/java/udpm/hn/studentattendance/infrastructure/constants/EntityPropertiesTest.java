package udpm.hn.studentattendance.infrastructure.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EntityPropertiesTest {
    @Test
    void testEntityPropertiesExists() {
        assertNotNull(EntityProperties.LENGTH_ID);
        assertNotNull(EntityProperties.LENGTH_CODE);
        assertNotNull(EntityProperties.LENGTH_NAME);
        assertNotNull(EntityProperties.LENGTH_TEXT);
    }

    @Test
    void testIdLength() {
        assertEquals(36, EntityProperties.LENGTH_ID);
    }

    @Test
    void testCodeLength() {
        assertEquals(50, EntityProperties.LENGTH_CODE);
    }

    @Test
    void testNameLength() {
        assertEquals(255, EntityProperties.LENGTH_NAME);
    }

    @Test
    void testTextLength() {
        assertEquals(65535, EntityProperties.LENGTH_TEXT);
    }

    @Test
    void testAllPropertiesAreValid() {
        assertTrue(EntityProperties.LENGTH_ID > 0);
        assertTrue(EntityProperties.LENGTH_CODE > 0);
        assertTrue(EntityProperties.LENGTH_NAME > 0);
        assertTrue(EntityProperties.LENGTH_TEXT > 0);
    }
}