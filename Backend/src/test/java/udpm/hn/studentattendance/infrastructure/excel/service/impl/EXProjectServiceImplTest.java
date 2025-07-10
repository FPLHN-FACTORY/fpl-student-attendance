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
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectCreateOrUpdateRequest;
import udpm.hn.studentattendance.core.staff.project.model.response.USProjectResponse;
import udpm.hn.studentattendance.core.staff.project.repository.STLevelProjectExtendRepository;
import udpm.hn.studentattendance.core.staff.project.repository.STProjectExtendRepository;
import udpm.hn.studentattendance.core.staff.project.repository.STProjectSemesterExtendRepository;
import udpm.hn.studentattendance.core.staff.project.repository.STProjectSubjectFacilityExtendRepository;
import udpm.hn.studentattendance.core.staff.project.service.STProjectManagementService;
import udpm.hn.studentattendance.entities.LevelProject;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.entities.SubjectFacility;
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

import java.io.ByteArrayOutputStream;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EXProjectServiceImplTest {

    @Mock
    private EXImportLogRepository importLogRepository;
    @Mock
    private EXImportLogDetailRepository importLogDetailRepository;
    @Mock
    private ExcelHelper excelHelper;
    @Mock
    private SessionHelper sessionHelper;
    @Mock
    private STProjectManagementService service;
    @Mock
    private STProjectSemesterExtendRepository semesterExtendRepository;
    @Mock
    private STProjectSubjectFacilityExtendRepository subjectFacilityExtendRepository;
    @Mock
    private STLevelProjectExtendRepository levelProjectExtendRepository;
    @Mock
    private STProjectExtendRepository projectExtendRepository;

    @InjectMocks
    private EXProjectServiceImpl exProjectService;

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
        ResponseEntity<?> response = exProjectService.getDataFromFile(request);
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Vui lòng tải lên file Excel", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test getDataFromFile should return error when IOException occurs")
    void testGetDataFromFileWithIOException() throws IOException {
        EXUploadRequest request = new EXUploadRequest();
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "test data".getBytes());
        request.setFile(file);
        when(ExcelHelper.readFile(file)).thenThrow(new IOException("File read error"));
        ResponseEntity<?> response = exProjectService.getDataFromFile(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Lỗi khi xử lý Excel", apiResponse.getMessage());
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
        List<Map<String, String>> expectedData = List.of(Map.of("TEN_DU_AN", "DA001"));
        when(ExcelHelper.readFile(file)).thenReturn(expectedData);
        ResponseEntity<?> response = exProjectService.getDataFromFile(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Tải lên Excel thành công", apiResponse.getMessage());
        assertEquals(expectedData, apiResponse.getData());
    }

    @Test
    @DisplayName("Test importItem should return error when project name is null")
    void testImportItemWithNullProjectName() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("TEN_DU_AN", null);
        item.put("CAP_DU_AN", "Level 1 - LV1");
        item.put("HOC_KY", "HK1");
        item.put("MON_HOC", "Subject 1 - SUB1");
        request.setItem(item);
        ResponseEntity<?> response = exProjectService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không được để trống tên dự án", apiResponse.getMessage());
        verify(excelHelper).saveLogError(ImportLogType.PROJECT, "Không được để trống tên dự án", request);
    }

    @Test
    @DisplayName("Test importItem should return error when level is null")
    void testImportItemWithNullLevel() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("TEN_DU_AN", "DA001");
        item.put("CAP_DU_AN", null);
        item.put("HOC_KY", "HK1");
        item.put("MON_HOC", "Subject 1 - SUB1");
        request.setItem(item);
        ResponseEntity<?> response = exProjectService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không được để trống cấp dự án", apiResponse.getMessage());
        verify(excelHelper).saveLogError(ImportLogType.PROJECT, "Không được để trống cấp dự án", request);
    }

    @Test
    @DisplayName("Test importItem should return error when semester is null")
    void testImportItemWithNullSemester() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("TEN_DU_AN", "DA001");
        item.put("CAP_DU_AN", "Level 1 - LV1");
        item.put("HOC_KY", null);
        item.put("MON_HOC", "Subject 1 - SUB1");
        request.setItem(item);
        ResponseEntity<?> response = exProjectService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không được để trống học kỳ", apiResponse.getMessage());
        verify(excelHelper).saveLogError(ImportLogType.PROJECT, "Không được để trống học kỳ", request);
    }

    @Test
    @DisplayName("Test importItem should return error when subject is null")
    void testImportItemWithNullSubject() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("TEN_DU_AN", "DA001");
        item.put("CAP_DU_AN", "Level 1 - LV1");
        item.put("HOC_KY", "HK1");
        item.put("MON_HOC", null);
        request.setItem(item);
        ResponseEntity<?> response = exProjectService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không được để trống môn học", apiResponse.getMessage());
        verify(excelHelper).saveLogError(ImportLogType.PROJECT, "Không được để trống môn học", request);
    }

    @Test
    @DisplayName("Test importItem should return error when level format is invalid")
    void testImportItemWithInvalidLevelFormat() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("TEN_DU_AN", "DA001");
        item.put("CAP_DU_AN", "Level 1");
        item.put("HOC_KY", "HK1");
        item.put("MON_HOC", "Subject 1 - SUB1");
        request.setItem(item);
        ResponseEntity<?> response = exProjectService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Định dạng cấp dự án không hợp lệ. Vui lòng chọn từ dropdown.", apiResponse.getMessage());
        verify(excelHelper).saveLogError(ImportLogType.PROJECT,
                "Định dạng cấp dự án không hợp lệ. Vui lòng chọn từ dropdown.", request);
    }

    @Test
    @DisplayName("Test importItem should return error when subject format is invalid")
    void testImportItemWithInvalidSubjectFormat() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("TEN_DU_AN", "DA001");
        item.put("CAP_DU_AN", "Level 1 - LV1");
        item.put("HOC_KY", "HK1");
        item.put("MON_HOC", "Subject 1");
        request.setItem(item);
        ResponseEntity<?> response = exProjectService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Định dạng môn học không hợp lệ. Vui lòng chọn từ dropdown.", apiResponse.getMessage());
        verify(excelHelper).saveLogError(ImportLogType.PROJECT,
                "Định dạng môn học không hợp lệ. Vui lòng chọn từ dropdown.", request);
    }

    @Test
    @DisplayName("Test importItem should return error when semester not found")
    void testImportItemWithSemesterNotFound() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("TEN_DU_AN", "DA001");
        item.put("CAP_DU_AN", "Level 1 - LV1");
        item.put("HOC_KY", "HK1");
        item.put("MON_HOC", "Subject 1 - SUB1");
        request.setItem(item);
        when(semesterExtendRepository.getSemesterByCode("HK1")).thenReturn(null);
        ResponseEntity<?> response = exProjectService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Học kỳ 'HK1' không tồn tại.", apiResponse.getMessage());
        verify(excelHelper).saveLogError(ImportLogType.PROJECT, "Học kỳ 'HK1' không tồn tại.", request);
    }

    @Test
    @DisplayName("Test importItem should return error when subject not found")
    void testImportItemWithSubjectNotFound() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("TEN_DU_AN", "DA001");
        item.put("CAP_DU_AN", "Level 1 - LV1");
        item.put("HOC_KY", "HK1");
        item.put("MON_HOC", "Subject 1 - SUB1");
        request.setItem(item);
        Semester semester = mock(Semester.class);
        when(semesterExtendRepository.getSemesterByCode("HK1")).thenReturn(semester);
        when(subjectFacilityExtendRepository.getSubjectFacilityByName("facility-123", "SUB1")).thenReturn(null);
        ResponseEntity<?> response = exProjectService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Môn học với ID 'SUB1' không tồn tại.", apiResponse.getMessage());
        verify(excelHelper).saveLogError(ImportLogType.PROJECT, "Môn học với ID 'SUB1' không tồn tại.", request);
    }

    @Test
    @DisplayName("Test importItem should return error when level not found")
    void testImportItemWithLevelNotFound() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("TEN_DU_AN", "DA001");
        item.put("CAP_DU_AN", "Level 1 - LV1");
        item.put("HOC_KY", "HK1");
        item.put("MON_HOC", "Subject 1 - SUB1");
        request.setItem(item);
        Semester semester = mock(Semester.class);
        when(semesterExtendRepository.getSemesterByCode("HK1")).thenReturn(semester);
        SubjectFacility subject = mock(SubjectFacility.class);
        when(subjectFacilityExtendRepository.getSubjectFacilityByName("facility-123", "SUB1")).thenReturn(subject);
        when(levelProjectExtendRepository.getAllLevelProjectByCode(EntityStatus.ACTIVE, "LV1")).thenReturn(null);
        ResponseEntity<?> response = exProjectService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Cấp dự án 'LV1' không tồn tại.", apiResponse.getMessage());
        verify(excelHelper).saveLogError(ImportLogType.PROJECT, "Cấp dự án 'LV1' không tồn tại.", request);
    }

    @Test
    @DisplayName("Test importItem should save success log when project creation succeeds")
    void testImportItemWithSuccessfulCreation() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("TEN_DU_AN", "DA001");
        item.put("CAP_DU_AN", "Level 1 - LV1");
        item.put("HOC_KY", "HK1");
        item.put("MON_HOC", "Subject 1 - SUB1");
        request.setItem(item);
        Semester semester = mock(Semester.class);
        when(semester.getId()).thenReturn("semester-1");
        when(semesterExtendRepository.getSemesterByCode("HK1")).thenReturn(semester);
        SubjectFacility subject = mock(SubjectFacility.class);
        when(subjectFacilityExtendRepository.getSubjectFacilityByName("facility-123", "SUB1")).thenReturn(subject);
        LevelProject level = mock(LevelProject.class);
        when(level.getId()).thenReturn("level-1");
        when(levelProjectExtendRepository.getAllLevelProjectByCode(EntityStatus.ACTIVE, "LV1")).thenReturn(level);
        ApiResponse successResponse = ApiResponse.success("Thêm dự án mới thành công");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(successResponse, HttpStatus.OK);
        when(service.createProject(any(USProjectCreateOrUpdateRequest.class)))
                .thenReturn((ResponseEntity) serviceResponse);
        ResponseEntity<?> response = exProjectService.importItem(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(excelHelper).saveLogSuccess(ImportLogType.PROJECT, "Thêm dự án mới thành công", request);
    }

    @Test
    @DisplayName("Test importItem should save error log when project creation fails")
    void testImportItemWithFailedCreation() {
        EXImportRequest request = new EXImportRequest();
        Map<String, String> item = new HashMap<>();
        item.put("TEN_DU_AN", "DA001");
        item.put("CAP_DU_AN", "Level 1 - LV1");
        item.put("HOC_KY", "HK1");
        item.put("MON_HOC", "Subject 1 - SUB1");
        request.setItem(item);
        Semester semester = mock(Semester.class);
        when(semester.getId()).thenReturn("semester-1");
        when(semesterExtendRepository.getSemesterByCode("HK1")).thenReturn(semester);
        SubjectFacility subject = mock(SubjectFacility.class);
        when(subjectFacilityExtendRepository.getSubjectFacilityByName("facility-123", "SUB1")).thenReturn(subject);
        LevelProject level = mock(LevelProject.class);
        when(level.getId()).thenReturn("level-1");
        when(levelProjectExtendRepository.getAllLevelProjectByCode(EntityStatus.ACTIVE, "LV1")).thenReturn(level);
        ApiResponse errorResponse = ApiResponse.error("Tên dự án đã tồn tại");
        ResponseEntity<ApiResponse> serviceResponse = new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        when(service.createProject(any(USProjectCreateOrUpdateRequest.class)))
                .thenReturn((ResponseEntity) serviceResponse);
        ResponseEntity<?> response = exProjectService.importItem(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(excelHelper).saveLogError(ImportLogType.PROJECT, "Tên dự án đã tồn tại", request);
    }

    // Export, downloadTemplate, historyLog, historyLogDetail tests có thể bổ sung
    // tương tự như các service khác
}