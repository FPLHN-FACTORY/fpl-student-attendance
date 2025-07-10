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
import udpm.hn.studentattendance.core.admin.userstaff.model.request.ADCreateUpdateStaffRequest;
import udpm.hn.studentattendance.core.admin.userstaff.model.response.ADStaffResponse;
import udpm.hn.studentattendance.core.admin.userstaff.repository.ADStaffExtendRepository;
import udpm.hn.studentattendance.core.admin.userstaff.repository.ADStaffFacilityExtendRepository;
import udpm.hn.studentattendance.core.admin.userstaff.service.ADStaffService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
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

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class EXStaffServiceImplTest {

    @Mock
    private ADStaffService staffService;

    @Mock
    private EXImportLogRepository importLogRepository;

    @Mock
    private EXImportLogDetailRepository importLogDetailRepository;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private ADStaffFacilityExtendRepository facilityRepository;

    @Mock
    private ExcelHelper excelHelper;

    @Mock
    private ADStaffExtendRepository staffExtendRepository;

    @InjectMocks
    private EXStaffServiceImpl exStaffService;

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
        ResponseEntity<?> response = exStaffService.getDataFromFile(request);

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
        ResponseEntity<?> response = exStaffService.getDataFromFile(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Lỗi khi xử lý Excel", apiResponse.getMessage());
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
                Map.of("MA_NHAN_VIEN", "NV001", "TEN_NHAN_VIEN", "Nguyen Van A", "EMAIL_FE", "nguyenvana@fe.edu.vn"));

        when(ExcelHelper.readFile(file)).thenReturn(expectedData);

        // When
        ResponseEntity<?> response = exStaffService.getDataFromFile(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Tải lên Excel thành công", apiResponse.getMessage());
        assertEquals(expectedData, apiResponse.getData());
    }

    @Test
    @DisplayName("Test importItem should return error when facility is null")
    void testImportItemWithNullFacility() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("CO_SO", null);
        item.put("MA_NHAN_VIEN", "NV001");
        item.put("TEN_NHAN_VIEN", "Nguyen Van A");
        item.put("EMAIL_FE", "nguyenvana@fe.edu.vn");
        item.put("EMAIL_FPT", "nguyenvana@fpt.edu.vn");
        request.setData(data);
        request.setItem(item);

        // When
        ResponseEntity<?> response = exStaffService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Thông tin cơ sở không hợp lệ.", apiResponse.getMessage());

        verify(excelHelper).saveLogError(ImportLogType.STAFF, "Thông tin cơ sở không hợp lệ.", request);
    }

    @Test
    @DisplayName("Test importItem should return error when facility is empty")
    void testImportItemWithEmptyFacility() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("CO_SO", "");
        item.put("MA_NHAN_VIEN", "NV001");
        item.put("TEN_NHAN_VIEN", "Nguyen Van A");
        item.put("EMAIL_FE", "nguyenvana@fe.edu.vn");
        item.put("EMAIL_FPT", "nguyenvana@fpt.edu.vn");
        request.setData(data);
        request.setItem(item);

        // When
        ResponseEntity<?> response = exStaffService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Thông tin cơ sở không hợp lệ.", apiResponse.getMessage());

        verify(excelHelper).saveLogError(ImportLogType.STAFF, "Thông tin cơ sở không hợp lệ.", request);
    }

    @Test
    @DisplayName("Test importItem should return error when facility format is invalid")
    void testImportItemWithInvalidFacilityFormat() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("CO_SO", "Invalid Format");
        item.put("MA_NHAN_VIEN", "NV001");
        item.put("TEN_NHAN_VIEN", "Nguyen Van A");
        item.put("EMAIL_FE", "nguyenvana@fe.edu.vn");
        item.put("EMAIL_FPT", "nguyenvana@fpt.edu.vn");
        request.setData(data);
        request.setItem(item);

        // When
        ResponseEntity<?> response = exStaffService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Định dạng cơ sở không hợp lệ. Vui lòng chọn giá trị từ menu sổ xuống.", apiResponse.getMessage());

        verify(excelHelper).saveLogError(ImportLogType.STAFF,
                "Định dạng cơ sở không hợp lệ. Vui lòng chọn giá trị từ menu sổ xuống. => Invalid Format", request);
    }

    @Test
    @DisplayName("Test importItem should return error when staff code is null")
    void testImportItemWithNullStaffCode() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("CO_SO", "FPT University (FU)");
        item.put("MA_NHAN_VIEN", null);
        item.put("TEN_NHAN_VIEN", "Nguyen Van A");
        item.put("EMAIL_FE", "nguyenvana@fe.edu.vn");
        item.put("EMAIL_FPT", "nguyenvana@fpt.edu.vn");
        request.setData(data);
        request.setItem(item);

        // When
        ResponseEntity<?> response = exStaffService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không được để trống mã nhân viên", apiResponse.getMessage());

        verify(excelHelper).saveLogError(ImportLogType.STAFF, "Không được để trống mã nhân viên", request);
    }

    @Test
    @DisplayName("Test importItem should return error when staff code is empty")
    void testImportItemWithEmptyStaffCode() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("CO_SO", "FPT University (FU)");
        item.put("MA_NHAN_VIEN", "");
        item.put("TEN_NHAN_VIEN", "Nguyen Van A");
        item.put("EMAIL_FE", "nguyenvana@fe.edu.vn");
        item.put("EMAIL_FPT", "nguyenvana@fpt.edu.vn");
        request.setData(data);
        request.setItem(item);

        // When
        ResponseEntity<?> response = exStaffService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không được để trống mã nhân viên", apiResponse.getMessage());

        verify(excelHelper).saveLogError(ImportLogType.STAFF, "Không được để trống mã nhân viên", request);
    }

    @Test
    @DisplayName("Test importItem should return error when staff name is null")
    void testImportItemWithNullStaffName() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("CO_SO", "FPT University (FU)");
        item.put("MA_NHAN_VIEN", "NV001");
        item.put("TEN_NHAN_VIEN", null);
        item.put("EMAIL_FE", "nguyenvana@fe.edu.vn");
        item.put("EMAIL_FPT", "nguyenvana@fpt.edu.vn");
        request.setData(data);
        request.setItem(item);

        // When
        ResponseEntity<?> response = exStaffService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không được để trống tên nhân viên", apiResponse.getMessage());

        verify(excelHelper).saveLogError(ImportLogType.STAFF, "Không được để trống tên nhân viên", request);
    }

    @Test
    @DisplayName("Test importItem should return error when staff name is empty")
    void testImportItemWithEmptyStaffName() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("CO_SO", "FPT University (FU)");
        item.put("MA_NHAN_VIEN", "NV001");
        item.put("TEN_NHAN_VIEN", "");
        item.put("EMAIL_FE", "nguyenvana@fe.edu.vn");
        item.put("EMAIL_FPT", "nguyenvana@fpt.edu.vn");
        request.setData(data);
        request.setItem(item);

        // When
        ResponseEntity<?> response = exStaffService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không được để trống tên nhân viên", apiResponse.getMessage());

        verify(excelHelper).saveLogError(ImportLogType.STAFF, "Không được để trống tên nhân viên", request);
    }

    @Test
    @DisplayName("Test importItem should return error when email FE is null")
    void testImportItemWithNullEmailFe() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("CO_SO", "FPT University (FU)");
        item.put("MA_NHAN_VIEN", "NV001");
        item.put("TEN_NHAN_VIEN", "Nguyen Van A");
        item.put("EMAIL_FE", null);
        item.put("EMAIL_FPT", "nguyenvana@fpt.edu.vn");
        request.setData(data);
        request.setItem(item);

        // When
        ResponseEntity<?> response = exStaffService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không được để trống email fe của nhân viên", apiResponse.getMessage());

        verify(excelHelper).saveLogError(ImportLogType.STAFF, "Không được để trống email fe của nhân viên", request);
    }

    @Test
    @DisplayName("Test importItem should return error when email FE is empty")
    void testImportItemWithEmptyEmailFe() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("CO_SO", "FPT University (FU)");
        item.put("MA_NHAN_VIEN", "NV001");
        item.put("TEN_NHAN_VIEN", "Nguyen Van A");
        item.put("EMAIL_FE", "");
        item.put("EMAIL_FPT", "nguyenvana@fpt.edu.vn");
        request.setData(data);
        request.setItem(item);

        // When
        ResponseEntity<?> response = exStaffService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không được để trống email fe của nhân viên", apiResponse.getMessage());

        verify(excelHelper).saveLogError(ImportLogType.STAFF, "Không được để trống email fe của nhân viên", request);
    }

    @Test
    @DisplayName("Test importItem should return error when email FPT is null")
    void testImportItemWithNullEmailFpt() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("CO_SO", "FPT University (FU)");
        item.put("MA_NHAN_VIEN", "NV001");
        item.put("TEN_NHAN_VIEN", "Nguyen Van A");
        item.put("EMAIL_FE", "nguyenvana@fe.edu.vn");
        item.put("EMAIL_FPT", null);
        request.setData(data);
        request.setItem(item);

        // When
        ResponseEntity<?> response = exStaffService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không được để trống email fpt của nhân viên", apiResponse.getMessage());

        verify(excelHelper).saveLogError(ImportLogType.STAFF, "Không được để trống email fpt của nhân viên", request);
    }

    @Test
    @DisplayName("Test importItem should return error when email FPT is empty")
    void testImportItemWithEmptyEmailFpt() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("CO_SO", "FPT University (FU)");
        item.put("MA_NHAN_VIEN", "NV001");
        item.put("TEN_NHAN_VIEN", "Nguyen Van A");
        item.put("EMAIL_FE", "nguyenvana@fe.edu.vn");
        item.put("EMAIL_FPT", "");
        request.setData(data);
        request.setItem(item);

        // When
        ResponseEntity<?> response = exStaffService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không được để trống email fpt của nhân viên", apiResponse.getMessage());

        verify(excelHelper).saveLogError(ImportLogType.STAFF, "Không được để trống email fpt của nhân viên", request);
    }

    @Test
    @DisplayName("Test importItem should save success log when staff creation succeeds")
    void testImportItemWithSuccessfulCreation() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("CO_SO", "FPT University (FU)");
        item.put("MA_NHAN_VIEN", "NV001");
        item.put("TEN_NHAN_VIEN", "Nguyen Van A");
        item.put("EMAIL_FE", "nguyenvana@fe.edu.vn");
        item.put("EMAIL_FPT", "nguyenvana@fpt.edu.vn");
        item.put("VAI_TRO", "phụ trách xưởng");
        request.setData(data);
        request.setItem(item);

        Facility facility = mock(Facility.class);
        when(facility.getId()).thenReturn("facility-123");
        when(facilityRepository.getFacilityByCodeAndStatus("FU", EntityStatus.ACTIVE)).thenReturn(facility);

        ApiResponse successResponse = ApiResponse.success("Thêm nhân viên mới thành công");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(successResponse, HttpStatus.OK);

        when(staffService.createStaff(any(ADCreateUpdateStaffRequest.class)))
                .thenReturn((ResponseEntity) serviceResponse);

        // When
        ResponseEntity<?> response = exStaffService.importItem(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(excelHelper).saveLogSuccess(ImportLogType.STAFF, "Thêm nhân viên mới thành công", request);
    }

    @Test
    @DisplayName("Test importItem should save error log when staff creation fails")
    void testImportItemWithFailedCreation() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("CO_SO", "FPT University (FU)");
        item.put("MA_NHAN_VIEN", "NV001");
        item.put("TEN_NHAN_VIEN", "Nguyen Van A");
        item.put("EMAIL_FE", "nguyenvana@fe.edu.vn");
        item.put("EMAIL_FPT", "nguyenvana@fpt.edu.vn");
        request.setData(data);
        request.setItem(item);

        Facility facility = mock(Facility.class);
        when(facility.getId()).thenReturn("facility-123");
        when(facilityRepository.getFacilityByCodeAndStatus("FU", EntityStatus.ACTIVE)).thenReturn(facility);

        ApiResponse errorResponse = ApiResponse.error("Mã nhân viên đã tồn tại");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

        when(staffService.createStaff(any(ADCreateUpdateStaffRequest.class)))
                .thenReturn((ResponseEntity) serviceResponse);

        // When
        ResponseEntity<?> response = exStaffService.importItem(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(excelHelper).saveLogError(ImportLogType.STAFF, "Mã nhân viên đã tồn tại", request);
    }

    @Test
    @DisplayName("Test importItem should handle multiple roles correctly")
    void testImportItemWithMultipleRoles() {
        // Given
        EXImportRequest request = new EXImportRequest();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> item = new HashMap<>();
        item.put("CO_SO", "FPT University (FU)");
        item.put("MA_NHAN_VIEN", "NV001");
        item.put("TEN_NHAN_VIEN", "Nguyen Van A");
        item.put("EMAIL_FE", "nguyenvana@fe.edu.vn");
        item.put("EMAIL_FPT", "nguyenvana@fpt.edu.vn");
        item.put("VAI_TRO", "phụ trách xưởng, giảng viên");
        request.setData(data);
        request.setItem(item);

        Facility facility = mock(Facility.class);
        when(facility.getId()).thenReturn("facility-123");
        when(facilityRepository.getFacilityByCodeAndStatus("FU", EntityStatus.ACTIVE)).thenReturn(facility);

        ApiResponse successResponse = ApiResponse.success("Thêm nhân viên mới thành công");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(successResponse, HttpStatus.OK);

        when(staffService.createStaff(any(ADCreateUpdateStaffRequest.class)))
                .thenReturn((ResponseEntity) serviceResponse);

        // When
        ResponseEntity<?> response = exStaffService.importItem(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffService)
                .createStaff(argThat(req -> req.getRoleCodes().contains("0") && req.getRoleCodes().contains("1")));
    }

    @Test
    @DisplayName("Test exportData should return null when IOException occurs")
    void testExportDataWithIOException() {
        // Given
        EXDataRequest request = new EXDataRequest();
        ADStaffResponse staff = mock(ADStaffResponse.class);
        when(staff.getStaffCode()).thenReturn("NV001");
        when(staff.getStaffName()).thenReturn("Nguyen Van A");
        when(staff.getStaffEmailFe()).thenReturn("nguyenvana@fe.edu.vn");
        when(staff.getStaffEmailFpt()).thenReturn("nguyenvana@fpt.edu.vn");
        when(staff.getFacilityName()).thenReturn("FPT University");
        when(staff.getRoleCode()).thenReturn("0,1");

        List<ADStaffResponse> staffList = Arrays.asList(staff);

        when(staffExtendRepository.exportAllStaff()).thenReturn(staffList);

        // When
        ResponseEntity<?> response = exStaffService.exportData(request);

        // Then
        assertNull(response);
    }

    @Test
    @DisplayName("Test downloadTemplate should return error when template creation fails")
    void testDownloadTemplateWithCreationFailure() throws Exception {
        // Given
        EXDataRequest request = new EXDataRequest();

        when(ExcelHelper.createExcelStream(anyString(), anyList(), anyList()))
                .thenThrow(new Exception("Template creation failed"));

        // When
        ResponseEntity<?> response = exStaffService.downloadTemplate(request);

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

        when(ExcelHelper.createExcelStream(anyString(), anyList(), anyList())).thenReturn(null);

        // When
        ResponseEntity<?> response = exStaffService.downloadTemplate(request);

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

        when(importLogRepository.getListHistory(any(Pageable.class), eq(ImportLogType.STAFF.ordinal()),
                eq("user-123"), eq("facility-123")))
                .thenReturn(page);

        // When
        ResponseEntity<?> response = exStaffService.historyLog(request);

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
        ResponseEntity<?> response = exStaffService.historyLogDetail(request, logId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());
        assertEquals(details, apiResponse.getData());
    }
}