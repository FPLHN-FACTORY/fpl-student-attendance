package udpm.hn.studentattendance.helpers;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import udpm.hn.studentattendance.core.authentication.oauth2.AuthUser;
import udpm.hn.studentattendance.entities.ImportLog;
import udpm.hn.studentattendance.entities.ImportLogDetail;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.ImportLogType;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogDetailRepository;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExcelHelperTest {

    @Mock
    private EXImportLogRepository importLogRepository;

    @Mock
    private EXImportLogDetailRepository importLogDetailRepository;

    @Mock
    private SessionHelper sessionHelper;

    @InjectMocks
    private ExcelHelper excelHelper;

    @BeforeEach
    void setUp() {
        lenient().when(sessionHelper.getUserId()).thenReturn("1");
        lenient().when(sessionHelper.getFacilityId()).thenReturn("1");
    }

    @Test
    void testExcelHelperExists() {
        assertNotNull(ExcelHelper.class);
    }

    @Test
    void testHasExcelFormatWithXlsx() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes());
        assertTrue(ExcelHelper.hasExcelFormat(file));
    }

    @Test
    void testHasExcelFormatWithXls() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xls",
                "application/vnd.ms-excel",
                "test content".getBytes());
        assertTrue(ExcelHelper.hasExcelFormat(file));
    }

    @Test
    void testHasExcelFormatWithNonExcelFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt",
                "text/plain",
                "test content".getBytes());
        assertFalse(ExcelHelper.hasExcelFormat(file));
    }

    @Test
    void testHasExcelFormatWithNull() {
        assertFalse(ExcelHelper.hasExcelFormat(null));
    }

    @Test
    void testHasExcelFormatWithEmptyContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx",
                "",
                "test content".getBytes());
        assertFalse(ExcelHelper.hasExcelFormat(file));
    }

    @Test
    void testHasExcelFormatWithFilenameExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx",
                "application/octet-stream",
                "test content".getBytes());
        assertTrue(ExcelHelper.hasExcelFormat(file));
    }

    @Test
    void testReadFileWithValidExcel() throws IOException {
        // Create a test Excel file
        byte[] excelContent = createTestExcelFile();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelContent);

        List<Map<String, String>> result = ExcelHelper.readFile(file);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size()); // 2 data rows

        // Check first row
        Map<String, String> firstRow = result.get(0);
        assertEquals("1", firstRow.get("_LINE"));
        assertEquals("John", firstRow.get("name"));
        assertEquals("25", firstRow.get("age"));
    }

    @Test
    void testReadFileWithEmptySheet() throws IOException {
        // Create Excel file with empty sheet
        byte[] excelContent = createEmptyExcelFile();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelContent);

        assertThrows(IllegalArgumentException.class, () -> ExcelHelper.readFile(file));
    }

    @Test
    void testReadFileWithNoHeaders() throws IOException {
        // Create Excel file with no headers
        byte[] excelContent = createExcelFileWithoutHeaders();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelContent);

        assertThrows(IllegalArgumentException.class, () -> ExcelHelper.readFile(file));
    }

    @Test
    void testCreateExcelStream() {
        List<String> headers = Arrays.asList("Name", "Age", "City");
        List<List<Object>> data = Arrays.asList(
                Arrays.asList("John", 25, "New York"),
                Arrays.asList("Jane", 30, "Los Angeles"));

        byte[] result = ExcelHelper.createExcelStream("TestSheet", headers, data);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testCreateExcelStreamWithEmptyData() {
        List<String> headers = Arrays.asList("Name", "Age");
        List<List<Object>> data = new ArrayList<>();

        byte[] result = ExcelHelper.createExcelStream("TestSheet", headers, data);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testCreateExcelStreamWithNullValues() {
        List<String> headers = Arrays.asList("Name", "Age", "City");
        List<List<Object>> data = Arrays.asList(
                Arrays.asList("John", 25, null),
                Arrays.asList(null, 30, "Los Angeles"));

        byte[] result = ExcelHelper.createExcelStream("TestSheet", headers, data);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testSaveLogSuccess() {
        EXImportRequest request = new EXImportRequest();
        request.setCode("TEST_CODE");
        request.setFileName("test.xlsx");
        request.setLine(1);

        when(importLogRepository.findByIdUserAndCodeAndFileNameAndFacility_Id(any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(importLogRepository.save(any())).thenReturn(new ImportLog());
        when(importLogDetailRepository.save(any())).thenReturn(new ImportLogDetail());

        excelHelper.saveLogSuccess(ImportLogType.STUDENT, "Success message", request);

        verify(importLogRepository).findByIdUserAndCodeAndFileNameAndFacility_Id("1", "TEST_CODE", "test.xlsx", "1");
        verify(importLogRepository).save(any(ImportLog.class));
        verify(importLogDetailRepository).save(any(ImportLogDetail.class));
    }

    @Test
    void testSaveLogError() {
        EXImportRequest request = new EXImportRequest();
        request.setCode("TEST_CODE");
        request.setFileName("test.xlsx");
        request.setLine(1);

        when(importLogRepository.findByIdUserAndCodeAndFileNameAndFacility_Id(any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(importLogRepository.save(any())).thenReturn(new ImportLog());
        when(importLogDetailRepository.save(any())).thenReturn(new ImportLogDetail());

        excelHelper.saveLogError(ImportLogType.STUDENT, "Error message", request);

        verify(importLogRepository).findByIdUserAndCodeAndFileNameAndFacility_Id("1", "TEST_CODE", "test.xlsx", "1");
        verify(importLogRepository).save(any(ImportLog.class));
        verify(importLogDetailRepository).save(any(ImportLogDetail.class));
    }

    @Test
    void testSaveLogWithExistingImportLog() {
        EXImportRequest request = new EXImportRequest();
        request.setCode("TEST_CODE");
        request.setFileName("test.xlsx");
        request.setLine(1);

        ImportLog existingLog = new ImportLog();
        existingLog.setId("1");

        when(importLogRepository.findByIdUserAndCodeAndFileNameAndFacility_Id(any(), any(), any(), any()))
                .thenReturn(Optional.of(existingLog));
        when(importLogDetailRepository.save(any())).thenReturn(new ImportLogDetail());

        excelHelper.saveLogSuccess(ImportLogType.STUDENT, "Success message", request);

        verify(importLogRepository).findByIdUserAndCodeAndFileNameAndFacility_Id("1", "TEST_CODE", "test.xlsx", "1");
        verify(importLogDetailRepository).save(any(ImportLogDetail.class));
    }

    @Test
    void testSaveLogWithNullFacilityId() {
        EXImportRequest request = new EXImportRequest();
        request.setCode("TEST_CODE");
        request.setFileName("test.xlsx");
        request.setLine(1);

        when(sessionHelper.getFacilityId()).thenReturn(null);
        when(importLogRepository.findByIdUserAndCodeAndFileNameAndFacility_Id(any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(importLogRepository.save(any())).thenReturn(new ImportLog());
        when(importLogDetailRepository.save(any())).thenReturn(new ImportLogDetail());

        excelHelper.saveLogSuccess(ImportLogType.STUDENT, "Success message", request);

        verify(importLogRepository).findByIdUserAndCodeAndFileNameAndFacility_Id("1", "TEST_CODE", "test.xlsx", null);
        verify(importLogRepository).save(any(ImportLog.class));
        verify(importLogDetailRepository).save(any(ImportLogDetail.class));
    }

    @Test
    void testConstants() {
        assertEquals(EntityStatus.ACTIVE, ExcelHelper.STATUS_SUCCESS);
        assertEquals(EntityStatus.INACTIVE, ExcelHelper.STATUS_ERROR);
    }

    private byte[] createTestExcelFile() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Test");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Name");
            headerRow.createCell(1).setCellValue("Age");
            headerRow.createCell(2).setCellValue("City");

            // Create data rows
            Row dataRow1 = sheet.createRow(1);
            dataRow1.createCell(0).setCellValue("John");
            dataRow1.createCell(1).setCellValue(25);
            dataRow1.createCell(2).setCellValue("New York");

            Row dataRow2 = sheet.createRow(2);
            dataRow2.createCell(0).setCellValue("Jane");
            dataRow2.createCell(1).setCellValue(30);
            dataRow2.createCell(2).setCellValue("Los Angeles");

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createEmptyExcelFile() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.createSheet("Test");
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createExcelFileWithoutHeaders() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Test");

            // Create data row without header
            Row dataRow = sheet.createRow(0);
            dataRow.createCell(0).setCellValue("John");
            dataRow.createCell(1).setCellValue(25);

            workbook.write(out);
            return out.toByteArray();
        }
    }
}