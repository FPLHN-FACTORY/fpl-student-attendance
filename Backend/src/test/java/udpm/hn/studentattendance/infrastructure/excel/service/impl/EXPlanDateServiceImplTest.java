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
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddOrUpdatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDFacilityShiftRepository;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanDateService;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCAttendanceRepository;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCFactoryExtendRepository;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCPlanDateRepository;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCPlanFactoryRepository;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCStudentFactoryExtendRepository;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.ImportLogType;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogDetailResponse;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogResponse;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogDetailRepository;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogRepository;

import java.io.ByteArrayOutputStream;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EXPlanDateServiceImplTest {
    @Mock
    private SPDPlanDateService spdPlanDateService;
    @Mock
    private SPDFacilityShiftRepository spdFacilityShiftRepository;
    @Mock
    private EXImportLogRepository importLogRepository;
    @Mock
    private EXImportLogDetailRepository importLogDetailRepository;
    @Mock
    private TCAttendanceRepository tcAttendanceRepository;
    @Mock
    private TCPlanDateRepository tcPlanDateRepository;
    @Mock
    private TCFactoryExtendRepository tcFactoryExtendRepository;
    @Mock
    private TCPlanFactoryRepository tcPlanFactoryRepository;
    @Mock
    private TCStudentFactoryExtendRepository tcStudentFactoryExtendRepository;
    @Mock
    private ExcelHelper excelHelper;
    @Mock
    private SessionHelper sessionHelper;

    @InjectMocks
    private EXPlanDateServiceImpl exPlanDateService;

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
        ResponseEntity<ApiResponse> response = exPlanDateService.getDataFromFile(request);
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Vui lòng tải lên file Excel", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test getDataFromFile should return error when IOException occurs")
    void testGetDataFromFileWithIOException() throws IOException {
        EXUploadRequest request = new EXUploadRequest();
        // Tạo file Excel hợp lệ
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet("Sheet1");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        byte[] excelBytes = bos.toByteArray();
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);
        request.setFile(file);
        when(ExcelHelper.readFile(file)).thenThrow(new IOException("File read error"));
        ResponseEntity<ApiResponse> response = exPlanDateService.getDataFromFile(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Lỗi khi xử lý excel", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test getDataFromFile should return success when file is valid")
    void testGetDataFromFileWithValidFile() throws IOException {
        EXUploadRequest request = new EXUploadRequest();
        // Tạo file Excel hợp lệ
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet("Sheet1");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        byte[] excelBytes = bos.toByteArray();
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);
        request.setFile(file);
        List<Map<String, String>> expectedData = List.of(Map.of("NGAY_DIEN_RA", "01/01/2024"));
        when(ExcelHelper.readFile(file)).thenReturn(expectedData);
        ResponseEntity<ApiResponse> response = exPlanDateService.getDataFromFile(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Tải lên excel thành công", apiResponse.getMessage());
        assertEquals(expectedData, apiResponse.getData());
    }

    @Test
    @DisplayName("Test importItem should return error when plan factory not found")
    void testImportItemWithPlanFactoryNotFound() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("idPlanFactory", "pf-1");
        request.setItem(item);
        request.setData(data);
        ApiResponse errorResponse = ApiResponse.error("Không tìm thấy nhóm xưởng");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        when(spdPlanDateService.getDetail("pf-1")).thenReturn((ResponseEntity) serviceResponse);
        ResponseEntity<ApiResponse> response = exPlanDateService.importItem(request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không tìm thấy nhóm xưởng", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test importItem should return error when date format is invalid")
    void testImportItemWithInvalidDateFormat() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("NGAY_DIEN_RA", "2024-01-01");
        item.put("CA_BAT_DAU", "1");
        item.put("CA_KET_THUC", "2");
        item.put("HINH_THUC_HOC", "ONLINE");
        item.put("DIEM_DANH_MUON_TOI_DA", "15");
        item.put("CHECK_IP", "Có");
        item.put("CHECK_DIA_DIEM", "Có");
        item.put("YEU_CAU_CHECKIN", "Có");
        item.put("YEU_CAU_CHECKOUT", "Có");
        Map<String, Object> data = new HashMap<>();
        data.put("idPlanFactory", "pf-1");
        request.setItem(item);
        request.setData(data);
        ApiResponse successResponse = ApiResponse.success("Success");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(successResponse, HttpStatus.OK);
        when(spdPlanDateService.getDetail("pf-1")).thenReturn((ResponseEntity) serviceResponse);
        ResponseEntity<ApiResponse> response = exPlanDateService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Ngày diễn ra không hợp lệ (dd/MM/yyyy)", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test importItem should return error when shift type is invalid")
    void testImportItemWithInvalidShiftType() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("NGAY_DIEN_RA", "01/01/2024");
        item.put("CA_BAT_DAU", "1");
        item.put("CA_KET_THUC", "2");
        item.put("HINH_THUC_HOC", "INVALID");
        item.put("DIEM_DANH_MUON_TOI_DA", "15");
        item.put("CHECK_IP", "Có");
        item.put("CHECK_DIA_DIEM", "Có");
        item.put("YEU_CAU_CHECKIN", "Có");
        item.put("YEU_CAU_CHECKOUT", "Có");
        Map<String, Object> data = new HashMap<>();
        data.put("idPlanFactory", "pf-1");
        request.setItem(item);
        request.setData(data);
        ApiResponse successResponse = ApiResponse.success("Success");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(successResponse, HttpStatus.OK);
        when(spdPlanDateService.getDetail("pf-1")).thenReturn((ResponseEntity) serviceResponse);
        ResponseEntity<ApiResponse> response = exPlanDateService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Hình thức học không hợp lệ (ONLINE/OFFLINE)", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test importItem should return error when shift format is invalid")
    void testImportItemWithInvalidShiftFormat() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("NGAY_DIEN_RA", "01/01/2024");
        item.put("CA_BAT_DAU", "invalid");
        item.put("CA_KET_THUC", "2");
        item.put("HINH_THUC_HOC", "ONLINE");
        item.put("DIEM_DANH_MUON_TOI_DA", "15");
        item.put("CHECK_IP", "Có");
        item.put("CHECK_DIA_DIEM", "Có");
        item.put("YEU_CAU_CHECKIN", "Có");
        item.put("YEU_CAU_CHECKOUT", "Có");
        Map<String, Object> data = new HashMap<>();
        data.put("idPlanFactory", "pf-1");
        request.setItem(item);
        request.setData(data);
        ApiResponse successResponse = ApiResponse.success("Success");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(successResponse, HttpStatus.OK);
        when(spdPlanDateService.getDetail("pf-1")).thenReturn((ResponseEntity) serviceResponse);
        ResponseEntity<ApiResponse> response = exPlanDateService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Ca học không hợp lệ (1, 2, 3, ...)", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test importItem should return error when end shift is less than start shift")
    void testImportItemWithInvalidShiftRange() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("NGAY_DIEN_RA", "01/01/2024");
        item.put("CA_BAT_DAU", "3");
        item.put("CA_KET_THUC", "1");
        item.put("HINH_THUC_HOC", "ONLINE");
        item.put("DIEM_DANH_MUON_TOI_DA", "15");
        item.put("CHECK_IP", "Có");
        item.put("CHECK_DIA_DIEM", "Có");
        item.put("YEU_CAU_CHECKIN", "Có");
        item.put("YEU_CAU_CHECKOUT", "Có");
        Map<String, Object> data = new HashMap<>();
        data.put("idPlanFactory", "pf-1");
        request.setItem(item);
        request.setData(data);
        ApiResponse successResponse = ApiResponse.success("Success");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(successResponse, HttpStatus.OK);
        when(spdPlanDateService.getDetail("pf-1")).thenReturn((ResponseEntity) serviceResponse);
        ResponseEntity<ApiResponse> response = exPlanDateService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Ca kết thúc phải lớn hơn ca bắt đầu", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test importItem should return error when late arrival format is invalid")
    void testImportItemWithInvalidLateArrivalFormat() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("NGAY_DIEN_RA", "01/01/2024");
        item.put("CA_BAT_DAU", "1");
        item.put("CA_KET_THUC", "2");
        item.put("HINH_THUC_HOC", "ONLINE");
        item.put("DIEM_DANH_MUON_TOI_DA", "invalid");
        item.put("CHECK_IP", "Có");
        item.put("CHECK_DIA_DIEM", "Có");
        item.put("YEU_CAU_CHECKIN", "Có");
        item.put("YEU_CAU_CHECKOUT", "Có");
        Map<String, Object> data = new HashMap<>();
        data.put("idPlanFactory", "pf-1");
        request.setItem(item);
        request.setData(data);
        ApiResponse successResponse = ApiResponse.success("Success");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(successResponse, HttpStatus.OK);
        when(spdPlanDateService.getDetail("pf-1")).thenReturn((ResponseEntity) serviceResponse);
        ResponseEntity<ApiResponse> response = exPlanDateService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Thời gian điểm danh muộn tối đa không hợp lệ (0, 15, ...)", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test importItem should return error when check IP format is invalid")
    void testImportItemWithInvalidCheckIpFormat() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("NGAY_DIEN_RA", "01/01/2024");
        item.put("CA_BAT_DAU", "1");
        item.put("CA_KET_THUC", "2");
        item.put("HINH_THUC_HOC", "ONLINE");
        item.put("DIEM_DANH_MUON_TOI_DA", "15");
        item.put("CHECK_IP", "Invalid");
        item.put("CHECK_DIA_DIEM", "Có");
        item.put("YEU_CAU_CHECKIN", "Có");
        item.put("YEU_CAU_CHECKOUT", "Có");
        Map<String, Object> data = new HashMap<>();
        data.put("idPlanFactory", "pf-1");
        request.setItem(item);
        request.setData(data);
        ApiResponse successResponse = ApiResponse.success("Success");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(successResponse, HttpStatus.OK);
        when(spdPlanDateService.getDetail("pf-1")).thenReturn((ResponseEntity) serviceResponse);
        ResponseEntity<ApiResponse> response = exPlanDateService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Check IP không hợp lệ (Có / Không)", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test importItem should return error when check location format is invalid")
    void testImportItemWithInvalidCheckLocationFormat() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("NGAY_DIEN_RA", "01/01/2024");
        item.put("CA_BAT_DAU", "1");
        item.put("CA_KET_THUC", "2");
        item.put("HINH_THUC_HOC", "ONLINE");
        item.put("DIEM_DANH_MUON_TOI_DA", "15");
        item.put("CHECK_IP", "Có");
        item.put("CHECK_DIA_DIEM", "Invalid");
        item.put("YEU_CAU_CHECKIN", "Có");
        item.put("YEU_CAU_CHECKOUT", "Có");
        Map<String, Object> data = new HashMap<>();
        data.put("idPlanFactory", "pf-1");
        request.setItem(item);
        request.setData(data);
        ApiResponse successResponse = ApiResponse.success("Success");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(successResponse, HttpStatus.OK);
        when(spdPlanDateService.getDetail("pf-1")).thenReturn((ResponseEntity) serviceResponse);
        ResponseEntity<ApiResponse> response = exPlanDateService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Check địa điểm không hợp lệ (Có / Không)", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test importItem should return error when checkin requirement format is invalid")
    void testImportItemWithInvalidCheckinRequirementFormat() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("NGAY_DIEN_RA", "01/01/2024");
        item.put("CA_BAT_DAU", "1");
        item.put("CA_KET_THUC", "2");
        item.put("HINH_THUC_HOC", "ONLINE");
        item.put("DIEM_DANH_MUON_TOI_DA", "15");
        item.put("CHECK_IP", "Có");
        item.put("CHECK_DIA_DIEM", "Có");
        item.put("YEU_CAU_CHECKIN", "Invalid");
        item.put("YEU_CAU_CHECKOUT", "Có");
        Map<String, Object> data = new HashMap<>();
        data.put("idPlanFactory", "pf-1");
        request.setItem(item);
        request.setData(data);
        ApiResponse successResponse = ApiResponse.success("Success");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(successResponse, HttpStatus.OK);
        when(spdPlanDateService.getDetail("pf-1")).thenReturn((ResponseEntity) serviceResponse);
        ResponseEntity<ApiResponse> response = exPlanDateService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Yêu cầu checkin không hợp lệ (Có / Không)", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test importItem should return error when checkout requirement format is invalid")
    void testImportItemWithInvalidCheckoutRequirementFormat() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("NGAY_DIEN_RA", "01/01/2024");
        item.put("CA_BAT_DAU", "1");
        item.put("CA_KET_THUC", "2");
        item.put("HINH_THUC_HOC", "ONLINE");
        item.put("DIEM_DANH_MUON_TOI_DA", "15");
        item.put("CHECK_IP", "Có");
        item.put("CHECK_DIA_DIEM", "Có");
        item.put("YEU_CAU_CHECKIN", "Có");
        item.put("YEU_CAU_CHECKOUT", "Invalid");
        Map<String, Object> data = new HashMap<>();
        data.put("idPlanFactory", "pf-1");
        request.setItem(item);
        request.setData(data);
        ApiResponse successResponse = ApiResponse.success("Success");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(successResponse, HttpStatus.OK);
        when(spdPlanDateService.getDetail("pf-1")).thenReturn((ResponseEntity) serviceResponse);
        ResponseEntity<ApiResponse> response = exPlanDateService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Yêu cầu checkout không hợp lệ (Có / Không)", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test importItem should save success log when plan date creation succeeds")
    void testImportItemWithSuccessfulCreation() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("NGAY_DIEN_RA", "01/01/2024");
        item.put("CA_BAT_DAU", "1");
        item.put("CA_KET_THUC", "2");
        item.put("HINH_THUC_HOC", "ONLINE");
        item.put("DIEM_DANH_MUON_TOI_DA", "15");
        item.put("CHECK_IP", "Có");
        item.put("CHECK_DIA_DIEM", "Có");
        item.put("YEU_CAU_CHECKIN", "Có");
        item.put("YEU_CAU_CHECKOUT", "Có");
        Map<String, Object> data = new HashMap<>();
        data.put("idPlanFactory", "pf-1");
        request.setItem(item);
        request.setData(data);
        ApiResponse successResponse = ApiResponse.success("Thêm lịch học thành công");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(successResponse, HttpStatus.OK);
        when(spdPlanDateService.getDetail("pf-1")).thenReturn((ResponseEntity) serviceResponse);
        ResponseEntity<ApiResponse> response = exPlanDateService.importItem(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Test importItem should save error log when plan date creation fails")
    void testImportItemWithFailedCreation() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("NGAY_DIEN_RA", "01/01/2024");
        item.put("CA_BAT_DAU", "1");
        item.put("CA_KET_THUC", "2");
        item.put("HINH_THUC_HOC", "ONLINE");
        item.put("DIEM_DANH_MUON_TOI_DA", "15");
        item.put("CHECK_IP", "Có");
        item.put("CHECK_DIA_DIEM", "Có");
        item.put("YEU_CAU_CHECKIN", "Có");
        item.put("YEU_CAU_CHECKOUT", "Có");
        Map<String, Object> data = new HashMap<>();
        data.put("idPlanFactory", "pf-1");
        request.setItem(item);
        request.setData(data);
        ApiResponse successResponse = ApiResponse.success("Success");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(successResponse, HttpStatus.OK);
        when(spdPlanDateService.getDetail("pf-1")).thenReturn((ResponseEntity) serviceResponse);
        ApiResponse errorResponse = ApiResponse.error("Thêm lịch học thất bại");
        ResponseEntity<ApiResponse> errorServiceResponse = new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        // when(spdPlanDateService.addOrUpdatePlanDate(any(SPDAddOrUpdatePlanDateRequest.class))).thenReturn(errorServiceResponse);
        ResponseEntity<ApiResponse> response = exPlanDateService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // Export, downloadTemplate, historyLog, historyLogDetail tests có thể bổ sung
    // tương tự như các service khác
}