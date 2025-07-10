package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STStudentAttendanceRecoveryAddRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.repository.STAttendanceRecoveryRepository;
import udpm.hn.studentattendance.core.staff.attendancerecovery.service.STAttendanceRecoveryService;
import udpm.hn.studentattendance.entities.AttendanceRecovery;
import udpm.hn.studentattendance.entities.ImportLog;
import udpm.hn.studentattendance.entities.ImportLogDetail;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.ImportLogType;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogDetailResponse;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogResponse;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogDetailRepository;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogRepository;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserActivityLogRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;

@ExtendWith(MockitoExtension.class)
class EXAttendanceRecoveryServiceImplTest {
    @Mock
    private EXImportLogRepository importLogRepository;
    @Mock
    private EXImportLogDetailRepository importLogDetailRepository;
    @Mock
    private SessionHelper sessionHelper;
    @Mock
    private ExcelHelper excelHelper;
    @Mock
    private STAttendanceRecoveryService attendanceRecoveryService;
    @Mock
    private STAttendanceRecoveryRepository attendanceRecoveryRepository;
    @Mock
    private UserActivityLogHelper userActivityLogHelper;
    @Mock
    private CommonUserActivityLogRepository userActivityLogRepository;

    @InjectMocks
    private EXAttendanceRecoveryServiceImpl exAttendanceRecoveryService;

    @BeforeEach
    void setUp() {
        lenient().when(sessionHelper.getUserId()).thenReturn("user-123");
        lenient().when(sessionHelper.getFacilityId()).thenReturn("facility-123");
    }

    @Test
    @DisplayName("Test getDataFromFile should return error when file is empty")
    void testGetDataFromFileWithEmptyFile() {
        EXUploadRequest request = new EXUploadRequest();
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);
        request.setFile(emptyFile);
        ResponseEntity<?> response = exAttendanceRecoveryService.getDataFromFile(request);
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Vui lòng tải lên file excel", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test getDataFromFile should return error when IOException occurs")
    void testGetDataFromFileWithIOException() throws IOException {
        EXUploadRequest request = new EXUploadRequest();
        // Create an invalid Excel file that will cause IOException
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "invalid excel content".getBytes());
        request.setFile(file);
        
        ResponseEntity<?> response = exAttendanceRecoveryService.getDataFromFile(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Lỗi khi xử lý excel", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test getDataFromFile should return success when file is valid")
    void testGetDataFromFileWithValidFile() throws IOException {
        EXUploadRequest request = new EXUploadRequest();
        // Create a valid Excel file with headers
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Ngày điểm danh");
        headerRow.createCell(1).setCellValue("Mã sinh viên");
        
        // Create data row
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("01/01/2024");
        dataRow.createCell(1).setCellValue("SV001");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        byte[] excelBytes = bos.toByteArray();
        
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);
        request.setFile(file);
        
        ResponseEntity<?> response = exAttendanceRecoveryService.getDataFromFile(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Tải lên excel thành công", apiResponse.getMessage());
        
        // Verify that the data contains the expected content
        List<Map<String, String>> data = (List<Map<String, String>>) apiResponse.getData();
        assertNotNull(data);
        assertFalse(data.isEmpty());
        
        // Check that the data contains the expected structure
        Map<String, String> firstRow = data.get(0);
        assertNotNull(firstRow);
        assertTrue(firstRow.containsKey("_LINE"));
        assertTrue(firstRow.containsKey("MA_SINH_VIEN"));
        assertEquals("SV001", firstRow.get("MA_SINH_VIEN"));
    }

    @Test
    @DisplayName("Test importItem should return error when day is null or empty")
    void testImportItemWithNullOrEmptyDay() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("NGAY_DIEM_DANH", "");
        item.put("MA_SINH_VIEN", "SV001");
        Map<String, Object> data = new HashMap<>();
        data.put("attendanceRecoveryId", "ar-1");
        request.setItem(item);
        request.setData(data);
        ImportLog importLog = mock(ImportLog.class);
        when(importLogRepository.save(any(ImportLog.class))).thenReturn(importLog);
        when(attendanceRecoveryRepository.findById("ar-1")).thenReturn(Optional.empty());
        ResponseEntity<?> response = exAttendanceRecoveryService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Thông tin về ngày điểm danh không được để trống.", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test importItem should return error when day format is invalid")
    void testImportItemWithInvalidDayFormat() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("NGAY_DIEM_DANH", "2024-01-01");
        item.put("MA_SINH_VIEN", "SV001");
        Map<String, Object> data = new HashMap<>();
        data.put("attendanceRecoveryId", "ar-1");
        request.setItem(item);
        request.setData(data);
        ImportLog importLog = mock(ImportLog.class);
        when(importLogRepository.save(any(ImportLog.class))).thenReturn(importLog);
        when(attendanceRecoveryRepository.findById("ar-1")).thenReturn(Optional.empty());
        ResponseEntity<?> response = exAttendanceRecoveryService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals(
                "Định dạng ngày điểm danh không hợp lệ. Vui lòng sử dụng format: dd/MM/yyyy hoặc dd/MM/yyyy HH:mm:ss",
                apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test importItem should return error when day is in the future")
    void testImportItemWithFutureDay() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        String futureDay = future.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        item.put("NGAY_DIEM_DANH", futureDay);
        item.put("MA_SINH_VIEN", "SV001");
        Map<String, Object> data = new HashMap<>();
        data.put("attendanceRecoveryId", "ar-1");
        request.setItem(item);
        request.setData(data);
        ImportLog importLog = mock(ImportLog.class);
        when(importLogRepository.save(any(ImportLog.class))).thenReturn(importLog);
        when(attendanceRecoveryRepository.findById("ar-1")).thenReturn(Optional.empty());
        ResponseEntity<?> response = exAttendanceRecoveryService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals(
                "Không thể khôi phục điểm danh cho ngày trong tương lai. Ngày điểm danh phải là ngày hiện tại hoặc trong quá khứ.",
                apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test importItem should return error when student code is null or empty")
    void testImportItemWithNullOrEmptyStudentCode() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("NGAY_DIEM_DANH", "01/01/2024");
        item.put("MA_SINH_VIEN", "");
        Map<String, Object> data = new HashMap<>();
        data.put("attendanceRecoveryId", "ar-1");
        request.setItem(item);
        request.setData(data);
        ImportLog importLog = mock(ImportLog.class);
        when(importLogRepository.save(any(ImportLog.class))).thenReturn(importLog);
        when(attendanceRecoveryRepository.findById("ar-1")).thenReturn(Optional.empty());
        ResponseEntity<?> response = exAttendanceRecoveryService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Thông tin về mã sinh viên không được để trống.", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test importItem should save success log when attendance recovery succeeds")
    void testImportItemWithSuccessfulRecovery() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("NGAY_DIEM_DANH", "01/01/2024");
        item.put("MA_SINH_VIEN", "SV001");
        Map<String, Object> data = new HashMap<>();
        data.put("attendanceRecoveryId", "ar-1");
        request.setItem(item);
        request.setData(data);
        ImportLog importLog = mock(ImportLog.class);
        when(importLogRepository.save(any(ImportLog.class))).thenReturn(importLog);
        when(attendanceRecoveryRepository.findById("ar-1")).thenReturn(Optional.empty());
        ApiResponse successResponse = ApiResponse.success("Khôi phục điểm danh thành công");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(successResponse, HttpStatus.OK);
        when(attendanceRecoveryService
                .importAttendanceRecoveryStudent(any(STStudentAttendanceRecoveryAddRequest.class)))
                .thenReturn((ResponseEntity) serviceResponse);
        when(attendanceRecoveryService.getAllImportStudentSuccess(any(), any(), any(), anyInt())).thenReturn(1);
        ResponseEntity<?> response = exAttendanceRecoveryService.importItem(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Test importItem should save error log when attendance recovery fails")
    void testImportItemWithFailedRecovery() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("NGAY_DIEM_DANH", "01/01/2024");
        item.put("MA_SINH_VIEN", "SV001");
        Map<String, Object> data = new HashMap<>();
        data.put("attendanceRecoveryId", "ar-1");
        request.setItem(item);
        request.setData(data);
        ImportLog importLog = mock(ImportLog.class);
        when(importLogRepository.save(any(ImportLog.class))).thenReturn(importLog);
        when(attendanceRecoveryRepository.findById("ar-1")).thenReturn(Optional.empty());
        ApiResponse errorResponse = ApiResponse.error("Khôi phục điểm danh thất bại");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        when(attendanceRecoveryService
                .importAttendanceRecoveryStudent(any(STStudentAttendanceRecoveryAddRequest.class)))
                .thenReturn((ResponseEntity) serviceResponse);
        when(attendanceRecoveryService.getAllImportStudentSuccess(any(), any(), any(), anyInt())).thenReturn(0);
        ResponseEntity<?> response = exAttendanceRecoveryService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // Export, downloadTemplate, historyLog, historyLogDetail tests có thể bổ sung
    // tương tự như các service khác
}