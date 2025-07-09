package udpm.hn.studentattendance.helpers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GenerateNameHelperTest {
    @Test
    void testGenerateNameHelperExists() {
        assertNotNull(GenerateNameHelper.class);
    }

    @Test
    void testGenerateName() {
        String name = GenerateNameHelper.generateName();
        assertNotNull(name);
        assertFalse(name.isEmpty());
        assertTrue(name.length() > 0);
    }

    @Test
    void testGenerateNameWithLength() {
        String name = GenerateNameHelper.generateName(10);
        assertNotNull(name);
        assertEquals(10, name.length());
    }

    @Test
    void testGenerateNameWithZeroLength() {
        String name = GenerateNameHelper.generateName(0);
        assertNotNull(name);
        assertTrue(name.isEmpty());
    }

    @Test
    void testGenerateNameWithNegativeLength() {
        String name = GenerateNameHelper.generateName(-5);
        assertNotNull(name);
        assertTrue(name.isEmpty());
    }

    @Test
    void testGenerateNameMultipleTimes() {
        String name1 = GenerateNameHelper.generateName();
        String name2 = GenerateNameHelper.generateName();
        assertNotNull(name1);
        assertNotNull(name2);
        // Names should be different (random)
        assertNotEquals(name1, name2);
    }

    @Test
    void testGenerateCodeFromNameWithNormalText() {
        String result = GenerateNameHelper.generateCodeFromName("Hello World");
        assertEquals("HELLO_WORLD", result);
    }

    @Test
    void testGenerateCodeFromNameWithVietnameseCharacters() {
        String result = GenerateNameHelper.generateCodeFromName("Nguyễn Văn Đức");
        assertEquals("NGUYEN_VAN_DUC", result);
    }

    @Test
    void testGenerateCodeFromNameWithSpecialCharacters() {
        String result = GenerateNameHelper.generateCodeFromName("Trần Thị Ớt");
        assertEquals("TRAN_THI_OT", result);
    }

    @Test
    void testGenerateCodeFromNameWithMultipleSpaces() {
        String result = GenerateNameHelper.generateCodeFromName("  Multiple   Spaces  ");
        assertEquals("MULTIPLE_SPACES", result);
    }

    @Test
    void testGenerateCodeFromNameWithEmptyString() {
        String result = GenerateNameHelper.generateCodeFromName("");
        assertEquals("", result);
    }

    @Test
    void testGenerateCodeFromNameWithNull() {
        assertThrows(NullPointerException.class, () -> GenerateNameHelper.generateCodeFromName(null));
    }

    @Test
    void testGenerateCodeFromNameWithOnlySpaces() {
        String result = GenerateNameHelper.generateCodeFromName("   ");
        assertEquals("", result);
    }

    @Test
    void testGenerateCodeFromNameWithNumbers() {
        String result = GenerateNameHelper.generateCodeFromName("User 123");
        assertEquals("USER_123", result);
    }

    @Test
    void testReplaceManySpaceToOneSpace() {
        String result = GenerateNameHelper.replaceManySpaceToOneSpace("  Multiple   Spaces  ");
        assertEquals("Multiple Spaces", result);
    }

    @Test
    void testReplaceManySpaceToOneSpaceWithSingleSpace() {
        String result = GenerateNameHelper.replaceManySpaceToOneSpace("Single Space");
        assertEquals("Single Space", result);
    }

    @Test
    void testReplaceManySpaceToOneSpaceWithNoSpaces() {
        String result = GenerateNameHelper.replaceManySpaceToOneSpace("NoSpaces");
        assertEquals("NoSpaces", result);
    }

    @Test
    void testReplaceManySpaceToOneSpaceWithEmptyString() {
        String result = GenerateNameHelper.replaceManySpaceToOneSpace("");
        assertEquals("", result);
    }

    @Test
    void testReplaceManySpaceToOneSpaceWithOnlySpaces() {
        String result = GenerateNameHelper.replaceManySpaceToOneSpace("   ");
        assertEquals(" ", result);
    }

    @Test
    void testReplaceManySpaceToOneSpaceWithNull() {
        assertThrows(NullPointerException.class, () -> GenerateNameHelper.replaceManySpaceToOneSpace(null));
    }

    @Test
    void testReplaceSpaceToEmpty() {
        String result = GenerateNameHelper.replaceSpaceToEmpty("  Remove   All   Spaces  ");
        assertEquals("RemoveAllSpaces", result);
    }

    @Test
    void testReplaceSpaceToEmptyWithSingleSpace() {
        String result = GenerateNameHelper.replaceSpaceToEmpty("Single Space");
        assertEquals("SingleSpace", result);
    }

    @Test
    void testReplaceSpaceToEmptyWithNoSpaces() {
        String result = GenerateNameHelper.replaceSpaceToEmpty("NoSpaces");
        assertEquals("NoSpaces", result);
    }

    @Test
    void testReplaceSpaceToEmptyWithEmptyString() {
        String result = GenerateNameHelper.replaceSpaceToEmpty("");
        assertEquals("", result);
    }

    @Test
    void testReplaceSpaceToEmptyWithOnlySpaces() {
        String result = GenerateNameHelper.replaceSpaceToEmpty("   ");
        assertEquals("", result);
    }

    @Test
    void testReplaceSpaceToEmptyWithNull() {
        assertThrows(NullPointerException.class, () -> GenerateNameHelper.replaceSpaceToEmpty(null));
    }

    @Test
    void testGenerateNameWithDifferentLengths() {
        assertEquals(5, GenerateNameHelper.generateName(5).length());
        assertEquals(15, GenerateNameHelper.generateName(15).length());
        assertEquals(1, GenerateNameHelper.generateName(1).length());
    }

    @Test
    void testGenerateNameCharacters() {
        String name = GenerateNameHelper.generateName(100);
        assertTrue(name.matches("[A-Za-z0-9]+"));
    }

    @Test
    void testGenerateCodeFromNameWithAccentedCharacters() {
        String result = GenerateNameHelper.generateCodeFromName("Hà Nội");
        assertEquals("HA_NOI", result);
    }

    @Test
    void testGenerateCodeFromNameWithMixedCase() {
        String result = GenerateNameHelper.generateCodeFromName("MiXeD cAsE");
        assertEquals("MIXED_CASE", result);
    }

    @Test
    void testGenerateCodeFromNameWithSpecialVietnameseCharacters() {
        String result = GenerateNameHelper.generateCodeFromName("Ăn Ẩm Ứng");
        assertEquals("AN_AM_UNG", result);
    }
}