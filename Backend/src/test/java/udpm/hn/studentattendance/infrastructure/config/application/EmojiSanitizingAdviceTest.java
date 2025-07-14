package udpm.hn.studentattendance.infrastructure.config.application;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmojiSanitizingAdviceTest {
    @Test
    void testClassExists() {
        assertNotNull(new EmojiSanitizingAdvice());
    }
}