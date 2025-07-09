package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import org.junit.jupiter.api.BeforeEach;
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
import udpm.hn.studentattendance.core.staff.studentfactory.model.request.USStudentFactoryAddRequest;
import udpm.hn.studentattendance.core.staff.factory.model.response.USPlanDateStudentFactoryResponse;
import udpm.hn.studentattendance.core.staff.factory.repository.factory.USFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.studentfactory.service.USStudentFactoryService;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;
import udpm.hn.studentattendance.infrastructure.constants.ImportLogType;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
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
class EXStudentFactoryServiceImplTest {
    @Mock
    private USStudentFactoryService service;
    @Mock
    private EXImportLogRepository importLogRepository;
    @Mock
    private EXImportLogDetailRepository importLogDetailRepository;
    @Mock
    private ExcelHelper excelHelper;
    @Mock
    private SessionHelper sessionHelper;
    @Mock
    private USFactoryExtendRepository factoryExtendRepository;
    @InjectMocks
    private EXStudentFactoryServiceImpl exStudentFactoryService;

    private EXUploadRequest uploadRequest;
    private EXImportRequest importRequest;
    private EXDataRequest dataRequest;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        uploadRequest = new EXUploadRequest();
        importRequest = new EXImportRequest();
        dataRequest = new EXDataRequest();
        mockFile = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "test content".getBytes());
    }

    // ========== getDataFromFile Tests ==========
    @Test
    void getDataFromFile_EmptyFile_ReturnsBadGateway() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);
        uploadRequest.setFile(emptyFile);
        ResponseEntity<?> response = exStudentFactoryService.getDataFromFile(uploadRequest);
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Vui lòng tải lên file Excel"));
    }

    @Test
    void getDataFromFile_IOException_ReturnsError() throws IOException {
        uploadRequest.setFile(mockFile);
        when(ExcelHelper.readFile(any())).thenThrow(new IOException("IO error"));
        ResponseEntity<?> response = exStudentFactoryService.getDataFromFile(uploadRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Lỗi khi xử lý file Excel"));
    }

    @Test
    void getDataFromFile_ValidFile_ReturnsSuccess() throws IOException {
        uploadRequest.setFile(mockFile);
        List<Map<String, String>> mockData = Arrays.asList(
                Map.of("MA_SINH_VIEN", "SV001"),
                Map.of("MA_SINH_VIEN", "SV002"));
        when(ExcelHelper.readFile(any())).thenReturn(mockData);
        ResponseEntity<?> response = exStudentFactoryService.getDataFromFile(uploadRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Tải lên file Excel thành công"));
    }

    // ========== importItem Tests ==========
    @Test
    void importItem_MissingStudentCode_ReturnsValidationError() {
        Map<String, Object> data = Map.of("idFactory", "FACT001");
        Map<String, String> item = new HashMap<>();
        importRequest.setData(data);
        importRequest.setItem(item);
        ResponseEntity<?> response = exStudentFactoryService.importItem(importRequest);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(excelHelper).saveLogError(eq(ImportLogType.STUDENT_FACTORY),
                contains("Mã sinh viên không được để trống"), eq(importRequest));
    }

    @Test
    void importItem_ValidData_ServiceSuccess_ReturnsSuccess() {
        Map<String, Object> data = Map.of("idFactory", "FACT001");
        Map<String, String> item = Map.of("MA_SINH_VIEN", "SV001");
        importRequest.setData(data);
        importRequest.setItem(item);
        ApiResponse apiResponse = ApiResponse.success("Thêm thành công");
        ResponseEntity<ApiResponse> serviceResponse = ResponseEntity.ok(apiResponse);
        when(service.createStudent(any(USStudentFactoryAddRequest.class))).thenReturn((ResponseEntity) serviceResponse);
        ResponseEntity<?> response = exStudentFactoryService.importItem(importRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(excelHelper).saveLogSuccess(eq(ImportLogType.STUDENT_FACTORY), eq("Thêm thành công"), eq(importRequest));
    }

    @Test
    void importItem_ValidData_ServiceError_ReturnsError() {
        Map<String, Object> data = Map.of("idFactory", "FACT001");
        Map<String, String> item = Map.of("MA_SINH_VIEN", "SV001");
        importRequest.setData(data);
        importRequest.setItem(item);
        ApiResponse apiResponse = ApiResponse.error("Thêm thất bại");
        ResponseEntity<ApiResponse> serviceResponse = ResponseEntity.ok(apiResponse);
        when(service.createStudent(any(USStudentFactoryAddRequest.class))).thenReturn((ResponseEntity) serviceResponse);
        ResponseEntity<?> response = exStudentFactoryService.importItem(importRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(excelHelper).saveLogError(eq(ImportLogType.STUDENT_FACTORY), eq("Thêm thất bại"), eq(importRequest));
    }

    // ========== exportData Tests ==========
    @Test
    void exportData_FactoryNotFound_ReturnsError() {
        Map<String, Object> data = Map.of("idFactory", "FACT001");
        dataRequest.setData(data);
        when(factoryExtendRepository.findById("FACT001")).thenReturn(Optional.empty());
        ResponseEntity<?> response = exStudentFactoryService.exportData(dataRequest);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Không tìm thấy xưởng"));
    }

    @Test
    void exportData_NoPermission_ReturnsUnauthorized() {
        Map<String, Object> data = Map.of("idFactory", "FACT001");
        dataRequest.setData(data);
        Factory factory = mock(Factory.class);
        when(factoryExtendRepository.findById("FACT001")).thenReturn(Optional.of(factory));
        when(sessionHelper.getUserRole()).thenReturn(Set.of());
        when(factory.getUserStaff()).thenReturn(mock(udpm.hn.studentattendance.entities.UserStaff.class));
        when(factory.getUserStaff().getId()).thenReturn("STAFF001");
        when(sessionHelper.getUserId()).thenReturn("OTHERUSER");
        ResponseEntity<?> response = exStudentFactoryService.exportData(dataRequest);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Không có quyền truy cập"));
    }

    @Test
    void exportData_Success_ReturnsExcelFile() {
        Map<String, Object> data = Map.of("idFactory", "FACT001");
        dataRequest.setData(data);
        Factory factory = mock(Factory.class);
        when(factoryExtendRepository.findById("FACT001")).thenReturn(Optional.of(factory));
        when(factory.getId()).thenReturn("FACT001");
        when(factory.getName()).thenReturn("Xưởng A");
        when(sessionHelper.getUserRole()).thenReturn(Set.of(RoleConstant.ADMIN));
        List<USPlanDateStudentFactoryResponse> lstData = new ArrayList<>();
        USPlanDateStudentFactoryResponse record = mock(USPlanDateStudentFactoryResponse.class);
        when(record.getCode()).thenReturn("SV001");
        when(record.getName()).thenReturn("Nguyen Van A");
        when(record.getStatus()).thenReturn(AttendanceStatus.ABSENT.ordinal());
        when(record.getStartDate()).thenReturn(System.currentTimeMillis() - 1000);
        lstData.add(record);
        when(factoryExtendRepository.getAllPlanDateAttendanceByIdFactory("FACT001")).thenReturn(lstData);
        ResponseEntity<?> response = exStudentFactoryService.exportData(dataRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof byte[]);
    }

    @Test
    void exportData_IOException_ReturnsError() {
        Map<String, Object> data = Map.of("idFactory", "FACT001");
        dataRequest.setData(data);
        Factory factory = mock(Factory.class);
        when(factoryExtendRepository.findById("FACT001")).thenReturn(Optional.of(factory));
        when(factory.getId()).thenReturn("FACT001");
        when(factory.getName()).thenReturn("Xưởng A");
        when(sessionHelper.getUserRole()).thenReturn(Set.of(RoleConstant.ADMIN));
        when(factoryExtendRepository.getAllPlanDateAttendanceByIdFactory("FACT001"))
                .thenThrow(new RuntimeException("IO error"));
        ResponseEntity<?> response = exStudentFactoryService.exportData(dataRequest);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Lỗi khi xuất Excel"));
    }

    // ========== downloadTemplate Tests ==========
    @Test
    void downloadTemplate_Success_ReturnsTemplateFile() {
        ResponseEntity<?> response = exStudentFactoryService.downloadTemplate(dataRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof byte[]);
    }

    // ========== historyLog Tests ==========
    @Test
    void historyLog_Success_ReturnsLogList() {
        when(sessionHelper.getUserId()).thenReturn("USER001");
        when(sessionHelper.getFacilityId()).thenReturn("FACILITY001");
        List<ExImportLogResponse> mockLogs = List.of(mock(ExImportLogResponse.class));
        Page<ExImportLogResponse> mockPage = new PageImpl<>(mockLogs);
        when(importLogRepository.getListHistory(any(Pageable.class), eq(ImportLogType.STUDENT_FACTORY.ordinal()),
                eq("USER001"), eq("FACILITY001"))).thenReturn(mockPage);
        ResponseEntity<?> response = exStudentFactoryService.historyLog(dataRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Lấy danh sách dữ liệu thành công"));
    }

    // ========== historyLogDetail Tests ==========
    @Test
    void historyLogDetail_Success_ReturnsLogDetails() {
        when(sessionHelper.getUserId()).thenReturn("USER001");
        when(sessionHelper.getFacilityId()).thenReturn("FACILITY001");
        List<ExImportLogDetailResponse> mockDetails = List.of(mock(ExImportLogDetailResponse.class));
        when(importLogDetailRepository.getAllList("LOG001", "USER001", "FACILITY001")).thenReturn(mockDetails);
        ResponseEntity<?> response = exStudentFactoryService.historyLogDetail(dataRequest, "LOG001");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Lấy danh sách dữ liệu thành công"));
    }
}