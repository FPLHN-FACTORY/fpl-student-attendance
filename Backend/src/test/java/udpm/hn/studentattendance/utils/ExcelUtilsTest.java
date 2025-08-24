package udpm.hn.studentattendance.utils;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class ExcelUtilsTest {
    @Test
    void testCreateTemplate() {
        Workbook wb = new XSSFWorkbook();
        List<String> headers = List.of("A", "B");
        List<List<Object>> data = List.of(List.of(1, "x"), List.of(2, "y"));
        Sheet sheet = ExcelUtils.createTemplate(wb, "TestSheet", headers, data);
        assertEquals("TestSheet", sheet.getSheetName());
        assertEquals("A", sheet.getRow(0).getCell(0).getStringCellValue());
        assertEquals(1, (int) sheet.getRow(1).getCell(0).getNumericCellValue());
        assertEquals("x", sheet.getRow(1).getCell(1).getStringCellValue());
    }

    @Test
    void testCreateHiddenSheet() {
        Workbook wb = new XSSFWorkbook();
        Sheet hidden = ExcelUtils.createHiddenSheet(wb, "Hidden");
        assertEquals("Hidden", hidden.getSheetName());
        assertTrue(wb.isSheetHidden(wb.getSheetIndex(hidden)));
    }

    @Test
    void testHexToColor() {
        var color = ExcelUtils.hexToColor("#FF0000");
        assertNotNull(color);
        byte[] rgb = color.getRGB();
        assertEquals((byte) 255, rgb[0]);
        assertEquals((byte) 0, rgb[1]);
        assertEquals((byte) 0, rgb[2]);
    }

    @Test
    void testHexToColor_invalid() {
        assertNull(ExcelUtils.hexToColor(null));
        assertNull(ExcelUtils.hexToColor(""));
    }
}
