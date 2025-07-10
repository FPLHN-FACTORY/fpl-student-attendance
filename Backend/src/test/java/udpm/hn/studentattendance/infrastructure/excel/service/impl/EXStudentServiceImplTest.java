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
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.staff.student.model.request.USStudentCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.student.model.response.USStudentResponse;
import udpm.hn.studentattendance.core.staff.student.repository.USStudentExtendRepository;
import udpm.hn.studentattendance.core.staff.student.service.STStudentService;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.ImportLogType;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogDetailResponse;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogResponse;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogDetailRepository;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogRepository;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EXStudentServiceImplTest {

    @Mock
    private STStudentService service;

    @Mock
    private EXImportLogRepository importLogRepository;

    @Mock
    private EXImportLogDetailRepository importLogDetailRepository;

    @Mock
    private ExcelHelper excelHelper;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private USStudentExtendRepository studentExtendRepository;

    @InjectMocks
    private EXStudentServiceImpl exStudentService;

    @BeforeEach
    void setUp() {
        lenient().when(sessionHelper.getUserId()).thenReturn("user-123");
        lenient().when(sessionHelper.getFacilityId()).thenReturn("facility-123");
    }

    @Test
    @DisplayName("Test getDataFromFile should return error when file is empty")
    void testGetDataFromFileWithEmptyFile() {
        // Given
        EXUploadRequest request = new EXUploadRequest();
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);
        request.setFile(emptyFile);

        // When
        ResponseEntity<?> response = exStudentService.getDataFromFile(request);

        // Then
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Vui lòng tải lên file Excel", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test getDataFromFile should return error when IOException occurs")
    void testGetDataFromFileWithIOException() throws IOException {
        // Given
        EXUploadRequest request = new EXUploadRequest();
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "test data".getBytes());
        request.setFile(file);

        when(ExcelHelper.readFile(file)).thenThrow(new IOException("File read error"));

        // When
        ResponseEntity<?> response = exStudentService.getDataFromFile(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Lỗi khi xử lý file Excel", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test getDataFromFile should return success when file is valid")
    void testGetDataFromFileWithValidFile() throws IOException {
        // Given
        EXUploadRequest request = new EXUploadRequest();
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "test data".getBytes());
        request.setFile(file);

        List<Map<String, String>> expectedData = Arrays.asList(
                Map.of("MA_SINH_VIEN", "ST001", "TEN_SINH_VIEN", "Nguyen Van A", "EMAIL", "nguyenvana@fpt.edu.vn"));

        when(ExcelHelper.readFile(file)).thenReturn(expectedData);

        // When
        ResponseEntity<?> response = exStudentService.getDataFromFile(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Tải lên Excel thành công", apiResponse.getMessage());
        assertEquals(expectedData, apiResponse.getData());
    }

    @Test
    @DisplayName("Test importItem should return error when student code is null")
    void testImportItemWithNullStudentCode() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("MA_SINH_VIEN", null);
        item.put("TEN_SINH_VIEN", "Nguyen Van A");
        item.put("EMAIL", "nguyenvana@fpt.edu.vn");
        request.setData(data);
        request.setItem(item);

        // When
        ResponseEntity<?> response = exStudentService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Mã sinh viên không được để trống.", apiResponse.getMessage());

        verify(excelHelper).saveLogError(ImportLogType.STUDENT, "Mã sinh viên không được để trống.", request);
    }

    @Test
    @DisplayName("Test importItem should return error when student code is empty")
    void testImportItemWithEmptyStudentCode() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("MA_SINH_VIEN", "");
        item.put("TEN_SINH_VIEN", "Nguyen Van A");
        item.put("EMAIL", "nguyenvana@fpt.edu.vn");
        request.setData(data);
        request.setItem(item);

        // When
        ResponseEntity<?> response = exStudentService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Mã sinh viên không được để trống.", apiResponse.getMessage());

        verify(excelHelper).saveLogError(ImportLogType.STUDENT, "Mã sinh viên không được để trống.", request);
    }

    @Test
    @DisplayName("Test importItem should return error when student code is blank")
    void testImportItemWithBlankStudentCode() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("MA_SINH_VIEN", "   ");
        item.put("TEN_SINH_VIEN", "Nguyen Van A");
        item.put("EMAIL", "nguyenvana@fpt.edu.vn");
        request.setData(data);
        request.setItem(item);

        // When
        ResponseEntity<?> response = exStudentService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Mã sinh viên không được để trống.", apiResponse.getMessage());

        verify(excelHelper).saveLogError(ImportLogType.STUDENT, "Mã sinh viên không được để trống.", request);
    }

    @Test
    @DisplayName("Test importItem should return error when student name is null")
    void testImportItemWithNullStudentName() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("MA_SINH_VIEN", "ST001");
        item.put("TEN_SINH_VIEN", null);
        item.put("EMAIL", "nguyenvana@fpt.edu.vn");
        request.setData(data);
        request.setItem(item);

        // When
        ResponseEntity<?> response = exStudentService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Tên sinh viên không được để trống.", apiResponse.getMessage());

        verify(excelHelper).saveLogError(ImportLogType.STUDENT, "Tên sinh viên không được để trống.", request);
    }

    @Test
    @DisplayName("Test importItem should return error when student name is empty")
    void testImportItemWithEmptyStudentName() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("MA_SINH_VIEN", "ST001");
        item.put("TEN_SINH_VIEN", "");
        item.put("EMAIL", "nguyenvana@fpt.edu.vn");
        request.setData(data);
        request.setItem(item);

        // When
        ResponseEntity<?> response = exStudentService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Tên sinh viên không được để trống.", apiResponse.getMessage());

        verify(excelHelper).saveLogError(ImportLogType.STUDENT, "Tên sinh viên không được để trống.", request);
    }

    @Test
    @DisplayName("Test importItem should return error when email is null")
    void testImportItemWithNullEmail() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("MA_SINH_VIEN", "ST001");
        item.put("TEN_SINH_VIEN", "Nguyen Van A");
        item.put("EMAIL", null);
        request.setData(data);
        request.setItem(item);

        // When
        ResponseEntity<?> response = exStudentService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Email sinh viên không được để trống.", apiResponse.getMessage());

        verify(excelHelper).saveLogError(ImportLogType.STUDENT, "Email sinh viên không được để trống.", request);
    }

    @Test
    @DisplayName("Test importItem should return error when email is empty")
    void testImportItemWithEmptyEmail() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("MA_SINH_VIEN", "ST001");
        item.put("TEN_SINH_VIEN", "Nguyen Van A");
        item.put("EMAIL", "");
        request.setData(data);
        request.setItem(item);

        // When
        ResponseEntity<?> response = exStudentService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Email sinh viên không được để trống.", apiResponse.getMessage());

        verify(excelHelper).saveLogError(ImportLogType.STUDENT, "Email sinh viên không được để trống.", request);
    }

    @Test
    @DisplayName("Test importItem should save success log when student creation succeeds")
    void testImportItemWithSuccessfulCreation() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("MA_SINH_VIEN", "ST001");
        item.put("TEN_SINH_VIEN", "Nguyen Van A");
        item.put("EMAIL", "nguyenvana@fpt.edu.vn");
        request.setData(data);
        request.setItem(item);

        ApiResponse successResponse = ApiResponse.success("Thêm sinh viên mới thành công");

        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(successResponse, HttpStatus.OK);

        when(service.createStudent(any(USStudentCreateUpdateRequest.class)))
                .thenReturn((ResponseEntity) serviceResponse);

        // When
        ResponseEntity<?> response = exStudentService.importItem(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(excelHelper).saveLogSuccess(ImportLogType.STUDENT, "Thêm sinh viên mới thành công", request);
    }

    @Test
    @DisplayName("Test importItem should save error log when student creation fails")
    void testImportItemWithFailedCreation() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("MA_SINH_VIEN", "ST001");
        item.put("TEN_SINH_VIEN", "Nguyen Van A");
        item.put("EMAIL", "nguyenvana@fpt.edu.vn");
        request.setData(data);
        request.setItem(item);

        ApiResponse errorResponse = ApiResponse.error("Mã sinh viên đã tồn tại");

        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

        when(service.createStudent(any(USStudentCreateUpdateRequest.class)))
                .thenReturn((ResponseEntity) serviceResponse);

        // When
        ResponseEntity<?> response = exStudentService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(excelHelper).saveLogError(ImportLogType.STUDENT, "Mã sinh viên đã tồn tại", request);
    }

    @Test
    @DisplayName("Test exportData should return null when IOException occurs")
    void testExportDataWithIOException() {
        // Given
        EXDataRequest request = new EXDataRequest();
        USStudentResponse student = mock(USStudentResponse.class);
        when(student.getStudentCode()).thenReturn("ST001");
        when(student.getStudentName()).thenReturn("Nguyen Van A");
        when(student.getStudentEmail()).thenReturn("nguyenvana@fpt.edu.vn");

        List<USStudentResponse> students = Arrays.asList(student);

        when(studentExtendRepository.exportAllStudent("facility-123")).thenReturn(students);
        when(ExcelHelper.createExcelStream("Student Data", any(), any()))
                .thenThrow(new RuntimeException("IO Error"));

        // When
        ResponseEntity<?> response = exStudentService.exportData(request);

        // Then
        assertNull(response);
    }

    @Test
    @DisplayName("Test downloadTemplate should return error when template creation fails")
    void testDownloadTemplateWithCreationFailure() throws Exception {
        // Given
        EXDataRequest request = new EXDataRequest();

        when(ExcelHelper.createExcelStream("Student Template", any(), any()))
                .thenThrow(new Exception("Template creation failed"));

        // When
        ResponseEntity<?> response = exStudentService.downloadTemplate(request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không thể tạo file mẫu", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test downloadTemplate should return error when template data is null")
    void testDownloadTemplateWithNullData() throws Exception {
        // Given
        EXDataRequest request = new EXDataRequest();

        when(ExcelHelper.createExcelStream("Student Template", any(), any())).thenReturn(null);

        // When
        ResponseEntity<?> response = exStudentService.downloadTemplate(request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không thể tạo file mẫu", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test historyLog should return paginated log data")
    void testHistoryLog() {
        // Given
        EXDataRequest request = new EXDataRequest();
        ExImportLogResponse log1 = mock(ExImportLogResponse.class);
        ExImportLogResponse log2 = mock(ExImportLogResponse.class);
        List<ExImportLogResponse> logs = Arrays.asList(log1, log2);
        Page<ExImportLogResponse> page = new PageImpl<>(logs);

        when(importLogRepository.getListHistory(any(Pageable.class), eq(ImportLogType.STUDENT.ordinal()),
                eq("user-123"), eq("facility-123")))
                .thenReturn(page);

        // When
        ResponseEntity<?> response = exStudentService.historyLog(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test historyLogDetail should return log detail data")
    void testHistoryLogDetail() {
        // Given
        EXDataRequest request = new EXDataRequest();
        String logId = "log-123";
        ExImportLogDetailResponse detail1 = mock(ExImportLogDetailResponse.class);
        ExImportLogDetailResponse detail2 = mock(ExImportLogDetailResponse.class);
        List<ExImportLogDetailResponse> details = Arrays.asList(detail1, detail2);

        when(importLogDetailRepository.getAllList(logId, "user-123", "facility-123")).thenReturn(details);

        // When
        ResponseEntity<?> response = exStudentService.historyLogDetail(request, logId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());
        assertEquals(details, apiResponse.getData());
    }

}