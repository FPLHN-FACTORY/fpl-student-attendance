package udpm.hn.studentattendance.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CodeGeneratorUtilsTest {
    @Test
    void testGenerateRandom_defaultLength() {
        String code = CodeGeneratorUtils.generateRandom();
        assertNotNull(code);
        assertEquals(6, code.length());
    }

    @Test
    void testGenerateRandom_customLength() {
        int len = 8;
        String code = CodeGeneratorUtils.generateRandom(len);
        assertNotNull(code);
        assertEquals(len, code.length());
    }

    @Test
    void testGenerateCodeFromString_basic() {
        String input = "Nguyễn Văn Đạt";
        String code = CodeGeneratorUtils.generateCodeFromString(input);
        assertEquals("NGUYEN_VAN_DAT", code);
    }

    @Test
    void testGenerateCodeFromString_specialChars() {
        String input = "Đặng!@# Văn   Lâm";
        String code = CodeGeneratorUtils.generateCodeFromString(input);
        assertEquals("DANG_VAN_LAM", code);
    }

    @Test
    void testGenerateCodeFromString_trimAndUpper() {
        String input = "  abc xyz  ";
        String code = CodeGeneratorUtils.generateCodeFromString(input);
        assertEquals("ABC_XYZ", code);
    }
}