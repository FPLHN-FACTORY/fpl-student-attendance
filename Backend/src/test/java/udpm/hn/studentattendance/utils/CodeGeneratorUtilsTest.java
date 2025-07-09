package udpm.hn.studentattendance.utils;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CodeGeneratorUtilsTest {
    @Test
    void testCodeGeneratorUtilsExists() {
        assertNotNull(CodeGeneratorUtils.class);
    }

    @Test
    void testGenerateCode() {
        String code = CodeGeneratorUtils.generateCode();
        assertNotNull(code);
        assertFalse(code.isEmpty());
        assertEquals(8, code.length());
    }

    @Test
    void testGenerateCodeWithLength() {
        String code = CodeGeneratorUtils.generateCode(10);
        assertNotNull(code);
        assertFalse(code.isEmpty());
        assertEquals(10, code.length());
    }

    @Test
    void testGenerateCodeWithZeroLength() {
        String code = CodeGeneratorUtils.generateCode(0);
        assertNotNull(code);
        assertTrue(code.isEmpty());
    }

    @Test
    void testGenerateCodeWithNegativeLength() {
        String code = CodeGeneratorUtils.generateCode(-5);
        assertNotNull(code);
        assertTrue(code.isEmpty());
    }

    @Test
    void testGenerateUUID() {
        UUID uuid = CodeGeneratorUtils.generateUUID();
        assertNotNull(uuid);
        assertNotNull(uuid.toString());
        assertEquals(36, uuid.toString().length());
    }

    @Test
    void testGenerateUUIDString() {
        String uuidString = CodeGeneratorUtils.generateUUIDString();
        assertNotNull(uuidString);
        assertEquals(36, uuidString.length());
        assertTrue(uuidString.contains("-"));
    }
}