package udpm.hn.studentattendance.helpers;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExcelHelperTest {
    @Test
    void testCreateExcelStreamAndReadFile() throws Exception {
        // Tạo dữ liệu mẫu
        List<String> headers = Arrays.asList("Họ tên", "Tuổi");
        List<List<Object>> data = Arrays.asList(
                Arrays.asList("Nguyễn Văn A", 20),
                Arrays.asList("Trần Thị B", 22));
        // Tạo file excel từ dữ liệu
        byte[] excelBytes = ExcelHelper.createExcelStream("Sheet1", headers, data);
        assertNotNull(excelBytes);
        // Đọc lại file excel vừa tạo
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);
        List<Map<String, String>> rows = ExcelHelper.readFile(multipartFile);
        assertEquals(2, rows.size());
        assertEquals("Nguyễn Văn A", rows.get(0).get("HO_TEN"));
        assertEquals("20", rows.get(0).get("TUOI"));
        assertEquals("Trần Thị B", rows.get(1).get("HO_TEN"));
        assertEquals("22", rows.get(1).get("TUOI"));
    }

    @Test
    void testReadFile_invalidFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);
        assertThrows(Exception.class, () -> ExcelHelper.readFile(emptyFile));
    }

    @Test
    void testCreateExcelStream_emptyData() {
        List<String> headers = Arrays.asList("A", "B");
        List<List<Object>> data = Arrays.asList();
        byte[] excelBytes = ExcelHelper.createExcelStream("Sheet1", headers, data);
        assertNotNull(excelBytes);
        // Đọc lại file excel để kiểm tra header
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = wb.getSheetAt(0);
            assertEquals("Sheet1", sheet.getSheetName());
            assertEquals("A", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("B", sheet.getRow(0).getCell(1).getStringCellValue());
            assertNull(sheet.getRow(1));
        } catch (Exception e) {
            fail("Should read excel without error");
        }
    }
}
