package udpm.hn.studentattendance.helpers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GenerateNameHelperTest {
    @Test
    void testGenerateCodeFromName_basic() {
        String input = "Nguyễn Văn Đạt";
        String code = GenerateNameHelper.generateCodeFromName(input);
        assertEquals("NGUYEN_VAN_DAT", code);
    }

    @Test
    void testGenerateCodeFromName_specialChars() {
        String input = "Đặng  Văn   Lâm";
        String code = GenerateNameHelper.generateCodeFromName(input);
        assertEquals("DANG_VAN_LAM", code);
    }

    @Test
    void testGenerateCodeFromName_withAccentAndSpace() {
        String input = "  Trần   Thị  Thuỷ  ";
        String code = GenerateNameHelper.generateCodeFromName(input);
        assertEquals("_TRAN_THI_THUY_", code);
    }

    @Test
    void testReplaceManySpaceToOneSpace() {
        String input = "A   B    C";
        String result = GenerateNameHelper.replaceManySpaceToOneSpace(input);
        assertEquals("A B C", result);
    }

    @Test
    void testReplaceSpaceToEmpty() {
        String input = "A   B    C";
        String result = GenerateNameHelper.replaceSpaceToEmpty(input);
        assertEquals("ABC", result);
    }

    @Test
    void testGenerateCodeFromName_empty() {
        String input = "   ";
        String code = GenerateNameHelper.generateCodeFromName(input);
        assertEquals("_", code);
    }

    @Test
    void testReplaceManySpaceToOneSpace_empty() {
        String input = "   ";
        String result = GenerateNameHelper.replaceManySpaceToOneSpace(input);
        assertEquals(" ", result);
    }

    @Test
    void testReplaceSpaceToEmpty_empty() {
        String input = "   ";
        String result = GenerateNameHelper.replaceSpaceToEmpty(input);
        assertEquals("", result);
    }
}
