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
import udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.response.USFactoryResponse;
import udpm.hn.studentattendance.core.staff.factory.repository.factory.USFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.repository.factory.USProjectFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.repository.factory.USStaffFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.service.USFactoryService;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
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
import udpm.hn.studentattendance.core.staff.factory.model.response.USProjectFactoryResponse;

@ExtendWith(MockitoExtension.class)
class EXFactoryServiceImplTest {

        @Mock
        private USFactoryService factoryService;
        @Mock
        private USProjectFactoryExtendRepository projectFactoryExtendRepository;
        @Mock
        private USStaffFactoryExtendRepository staffFactoryExtendRepository;
        @Mock
        private EXImportLogRepository importLogRepository;
        @Mock
        private EXImportLogDetailRepository importLogDetailRepository;
        @Mock
        private SessionHelper sessionHelper;
        @Mock
        private ExcelHelper excelHelper;
        @Mock
        private USFactoryExtendRepository factoryExtendRepository;

        @InjectMocks
        private EXFactoryServiceImpl exFactoryService;

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
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "test content".getBytes());
        }

        // ========== getDataFromFile Tests ==========

        @Test
        void getDataFromFile_EmptyFile_ReturnsBadGateway() {
                // Arrange
                MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);
                uploadRequest.setFile(emptyFile);

                // Act
                ResponseEntity<?> response = exFactoryService.getDataFromFile(uploadRequest);

                // Assert
                assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
                assertTrue(response.getBody().toString().contains("Vui lòng tải lên file excel"));
        }

        @Test
        void getDataFromFile_IOException_ReturnsError() throws IOException {
                // Arrange
                MockMultipartFile invalidFile = new MockMultipartFile("file", "invalid.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "invalid content".getBytes());
                uploadRequest.setFile(invalidFile);

                // Act
                ResponseEntity<?> response = exFactoryService.getDataFromFile(uploadRequest);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertTrue(response.getBody().toString().contains("Lỗi khi xử lý file excel"));
        }

        @Test
        void getDataFromFile_ValidFileWithFactoryData_ReturnsSuccess() throws IOException {
                // Arrange
                uploadRequest.setFile(mockFile);
                List<Map<String, String>> mockData = Arrays.asList(
                                Map.of("TEN_NHOM_XUONG", "Factory 1", "MO_TA", "Description 1"),
                                Map.of("TEN_NHOM_XUONG", "Factory 2", "MO_TA", "Description 2"),
                                Map.of("OTHER_FIELD", "Value", "MO_TA", "Description 3") // Should be filtered out
                );
                when(ExcelHelper.readFile(any())).thenReturn(mockData);

                // Act
                ResponseEntity<?> response = exFactoryService.getDataFromFile(uploadRequest);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertTrue(response.getBody().toString().contains("Tải lên file excel thành công"));
        }

        @Test
        void getDataFromFile_ValidFileNoFactoryData_ReturnsEmptyList() throws IOException {
                // Arrange
                uploadRequest.setFile(mockFile);
                List<Map<String, String>> mockData = Arrays.asList(
                                Map.of("OTHER_FIELD", "Value 1"),
                                Map.of("ANOTHER_FIELD", "Value 2"));
                when(ExcelHelper.readFile(any())).thenReturn(mockData);

                // Act
                ResponseEntity<?> response = exFactoryService.getDataFromFile(uploadRequest);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertTrue(response.getBody().toString().contains("Tải lên file excel thành công"));
        }

        // ========== importItem Tests ==========

        @Test
        void importItem_MissingFactoryName_ReturnsValidationError() {
                // Arrange
                Map<String, String> item = Map.of("MO_TA", "Description", "GIANG_VIEN", "Teacher (T001)", "DAYLADUAN",
                                "PROJ001");
                importRequest.setItem(item);

                // Act
                ResponseEntity<?> response = exFactoryService.importItem(importRequest);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                verify(excelHelper).saveLogError(eq(ImportLogType.FACTORY),
                                contains("Tên nhóm xưởng không được để trống"),
                                eq(importRequest));
        }

        @Test
        void importItem_EmptyFactoryName_ReturnsValidationError() {
                // Arrange
                Map<String, String> item = Map.of("TEN_NHOM_XUONG", "   ", "MO_TA", "Description", "GIANG_VIEN",
                                "Teacher (T001)", "DAYLADUAN", "PROJ001");
                importRequest.setItem(item);

                // Act
                ResponseEntity<?> response = exFactoryService.importItem(importRequest);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                verify(excelHelper).saveLogError(eq(ImportLogType.FACTORY),
                                contains("Tên nhóm xưởng không được để trống"),
                                eq(importRequest));
        }

        @Test
        void importItem_MissingLecturer_ReturnsValidationError() {
                // Arrange
                Map<String, String> item = Map.of("TEN_NHOM_XUONG", "Factory Name", "MO_TA", "Description", "DAYLADUAN",
                                "PROJ001");
                importRequest.setItem(item);

                // Act
                ResponseEntity<?> response = exFactoryService.importItem(importRequest);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                verify(excelHelper).saveLogError(eq(ImportLogType.FACTORY),
                                contains("Thông tin giảng viên không được để trống"), eq(importRequest));
        }

        @Test
        void importItem_EmptyLecturer_ReturnsValidationError() {
                // Arrange
                Map<String, String> item = Map.of("TEN_NHOM_XUONG", "Factory Name", "MO_TA", "Description",
                                "GIANG_VIEN", "   ",
                                "DAYLADUAN", "PROJ001");
                importRequest.setItem(item);

                // Act
                ResponseEntity<?> response = exFactoryService.importItem(importRequest);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                verify(excelHelper).saveLogError(eq(ImportLogType.FACTORY),
                                contains("Thông tin giảng viên không được để trống"), eq(importRequest));
        }

        @Test
        void importItem_InvalidLecturerFormat_ReturnsValidationError() {
                // Arrange
                Map<String, String> item = Map.of("TEN_NHOM_XUONG", "Factory Name", "MO_TA", "Description",
                                "GIANG_VIEN",
                                "Invalid Format", "DAYLADUAN", "PROJ001");
                importRequest.setItem(item);

                // Act
                ResponseEntity<?> response = exFactoryService.importItem(importRequest);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                verify(excelHelper).saveLogError(eq(ImportLogType.FACTORY),
                                contains("Định dạng giảng viên không hợp lệ"),
                                eq(importRequest));
        }

        @Test
        void importItem_LecturerNotFound_ReturnsValidationError() {
                // Arrange
                Map<String, String> item = Map.of("TEN_NHOM_XUONG", "Factory Name", "MO_TA", "Description",
                                "GIANG_VIEN",
                                "Teacher (T001)", "DAYLADUAN", "PROJ001");
                importRequest.setItem(item);
                when(staffFactoryExtendRepository.findUserStaffByCode("T001")).thenReturn(Optional.empty());

                // Act
                ResponseEntity<?> response = exFactoryService.importItem(importRequest);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                verify(excelHelper).saveLogError(eq(ImportLogType.FACTORY), contains("Giảng viên không tồn tại"),
                                eq(importRequest));
        }

        @Test
        void importItem_MissingProjectId_ReturnsValidationError() {
                // Arrange
                Map<String, String> item = Map.of("TEN_NHOM_XUONG", "Factory Name", "MO_TA", "Description",
                                "GIANG_VIEN",
                                "Teacher (T001)");
                importRequest.setItem(item);

                // Act
                ResponseEntity<?> response = exFactoryService.importItem(importRequest);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                verify(excelHelper).saveLogError(eq(ImportLogType.FACTORY),
                                contains("Thông tin dự án không được để trống"),
                                eq(importRequest));
        }

        @Test
        void importItem_EmptyProjectId_ReturnsValidationError() {
                // Arrange
                Map<String, String> item = Map.of("TEN_NHOM_XUONG", "Factory Name", "MO_TA", "Description",
                                "GIANG_VIEN",
                                "Teacher (T001)", "DAYLADUAN", "   ");
                importRequest.setItem(item);

                // Act
                ResponseEntity<?> response = exFactoryService.importItem(importRequest);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                verify(excelHelper).saveLogError(eq(ImportLogType.FACTORY),
                                contains("Thông tin dự án không được để trống"),
                                eq(importRequest));
        }

        @Test
        void importItem_ValidDataServiceSuccess_ReturnsSuccess() {
                // Arrange
                Map<String, String> item = Map.of("TEN_NHOM_XUONG", "Factory Name", "MO_TA", "Description",
                                "GIANG_VIEN",
                                "Teacher (T001)", "DAYLADUAN", "PROJ001");
                importRequest.setItem(item);

                UserStaff mockUserStaff = new UserStaff();
                mockUserStaff.setId("USER001");
                when(staffFactoryExtendRepository.findUserStaffByCode("T001")).thenReturn(Optional.of(mockUserStaff));

                ApiResponse successResponse = ApiResponse.success("Factory created successfully");
                ResponseEntity<Object> serviceResponse = ResponseEntity.ok(successResponse);
                when(factoryService.createFactory(any(USFactoryCreateUpdateRequest.class)))
                                .thenReturn((ResponseEntity) serviceResponse);

                // Act
                ResponseEntity<?> response = exFactoryService.importItem(importRequest);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                verify(excelHelper).saveLogSuccess(eq(ImportLogType.FACTORY), eq("Factory created successfully"),
                                eq(importRequest));
        }

        @Test
        void importItem_ValidDataServiceFailure_ReturnsError() {
                // Arrange
                Map<String, String> item = Map.of("TEN_NHOM_XUONG", "Factory Name", "MO_TA", "Description",
                                "GIANG_VIEN",
                                "Teacher (T001)", "DAYLADUAN", "PROJ001");
                importRequest.setItem(item);

                UserStaff mockUserStaff = new UserStaff();
                mockUserStaff.setId("USER001");
                when(staffFactoryExtendRepository.findUserStaffByCode("T001")).thenReturn(Optional.of(mockUserStaff));

                ApiResponse errorResponse = ApiResponse.error("Factory creation failed");
                ResponseEntity<Object> serviceResponse = ResponseEntity.ok(errorResponse);
                when(factoryService.createFactory(any(USFactoryCreateUpdateRequest.class)))
                                .thenReturn((ResponseEntity) serviceResponse);

                // Act
                ResponseEntity<?> response = exFactoryService.importItem(importRequest);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                verify(excelHelper).saveLogError(eq(ImportLogType.FACTORY), eq("Factory creation failed"),
                                eq(importRequest));
        }

        // ========== exportData Tests ==========

        @Test
        void exportData_Success_ReturnsExcelFile() {
                // Arrange
                when(sessionHelper.getFacilityId()).thenReturn("FACILITY001");
                USFactoryResponse factory1 = mock(USFactoryResponse.class);
                when(factory1.getName()).thenReturn("Factory 1");
                when(factory1.getProjectName()).thenReturn("Project 1");
                when(factory1.getSubjectCode()).thenReturn("SUB001");
                when(factory1.getStaffName()).thenReturn("Staff 1");
                when(factory1.getFactoryDescription()).thenReturn("Desc 1");
                USFactoryResponse factory2 = mock(USFactoryResponse.class);
                when(factory2.getName()).thenReturn("Factory 2");
                when(factory2.getProjectName()).thenReturn("Project 2");
                when(factory2.getSubjectCode()).thenReturn("SUB002");
                when(factory2.getStaffName()).thenReturn("Staff 2");
                when(factory2.getFactoryDescription()).thenReturn("Desc 2");
                List<USFactoryResponse> mockFactories = Arrays.asList(factory1, factory2);
                when(factoryExtendRepository.exportAllFactory("FACILITY001")).thenReturn(mockFactories);

                // Act
                ResponseEntity<?> response = exFactoryService.exportData(dataRequest);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertTrue(response.getBody() instanceof byte[]);
        }

        @Test
        void exportData_IOException_ReturnsNull() {
                // Arrange
                when(sessionHelper.getFacilityId()).thenReturn("FACILITY001");
                when(factoryExtendRepository.exportAllFactory("FACILITY001"))
                                .thenThrow(new RuntimeException("Database error"));

                // Act
                ResponseEntity<?> response = exFactoryService.exportData(dataRequest);

                // Assert
                assertNull(response);
        }

        // ========== downloadTemplate Tests ==========

        @Test
        void downloadTemplate_Success_ReturnsTemplateFile() {
                // Arrange
                when(sessionHelper.getFacilityId()).thenReturn("FACILITY001");
                udpm.hn.studentattendance.core.staff.factory.model.response.USProjectFactoryResponse proj1 = mock(
                                udpm.hn.studentattendance.core.staff.factory.model.response.USProjectFactoryResponse.class);
                when(proj1.getId()).thenReturn("PROJ001");
                when(proj1.getProjectName()).thenReturn("Project 1");
                udpm.hn.studentattendance.core.staff.factory.model.response.USProjectFactoryResponse proj2 = mock(
                                udpm.hn.studentattendance.core.staff.factory.model.response.USProjectFactoryResponse.class);
                when(proj2.getId()).thenReturn("PROJ002");
                when(proj2.getProjectName()).thenReturn("Project 2");
                when(projectFactoryExtendRepository.getAllProject("FACILITY001"))
                                .thenReturn(Arrays.asList(proj1, proj2));
                when(staffFactoryExtendRepository.getListUserStaff(
                                eq(EntityStatus.ACTIVE), eq(EntityStatus.ACTIVE), eq("FACILITY001"),
                                eq(RoleConstant.TEACHER)))
                                .thenReturn(Arrays.asList(
                                                createMockUserStaff("Staff 1", "S001"),
                                                createMockUserStaff("Staff 2", "S002")));

                // Act
                ResponseEntity<?> response = exFactoryService.downloadTemplate(dataRequest);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertTrue(response.getBody() instanceof byte[]);
        }

        @Test
        void downloadTemplate_IOException_ReturnsError() {
                // Arrange
                when(sessionHelper.getFacilityId()).thenReturn("FACILITY001");
                udpm.hn.studentattendance.core.staff.factory.model.response.USProjectFactoryResponse proj1 = mock(
                                udpm.hn.studentattendance.core.staff.factory.model.response.USProjectFactoryResponse.class);
                when(proj1.getId()).thenReturn("PROJ001");
                when(proj1.getProjectName()).thenReturn("Project 1");
                when(projectFactoryExtendRepository.getAllProject("FACILITY001")).thenReturn(Arrays.asList(proj1));
                when(staffFactoryExtendRepository.getListUserStaff(
                                eq(EntityStatus.ACTIVE), eq(EntityStatus.ACTIVE), eq("FACILITY001"),
                                eq(RoleConstant.TEACHER)))
                                .thenReturn(Arrays.asList(
                                                createMockUserStaff("Staff 1", "S001")));

                // Act
                ResponseEntity<?> response = exFactoryService.downloadTemplate(dataRequest);

                // Assert
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                assertTrue(response.getBody().toString().contains("Không thể tạo file mẫu"));
        }

        // ========== historyLog Tests ==========

        @Test
        void historyLog_Success_ReturnsLogList() {
                // Arrange
                when(sessionHelper.getUserId()).thenReturn("USER001");
                when(sessionHelper.getFacilityId()).thenReturn("FACILITY001");

                List<ExImportLogResponse> mockLogs = Arrays.asList(
                                createMockImportLog("LOG001", "Success"),
                                createMockImportLog("LOG002", "Error"));
                Page<ExImportLogResponse> mockPage = new PageImpl<>(mockLogs);
                when(importLogRepository.getListHistory(any(Pageable.class), eq(ImportLogType.FACTORY.ordinal()),
                                eq("USER001"),
                                eq("FACILITY001")))
                                .thenReturn(mockPage);

                // Act
                ResponseEntity<?> response = exFactoryService.historyLog(dataRequest);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertTrue(response.getBody().toString().contains("Lấy danh sách dữ liệu thành công"));
        }

        // ========== historyLogDetail Tests ==========

        @Test
        void historyLogDetail_Success_ReturnsLogDetails() {
                // Arrange
                String logId = "LOG001";
                when(sessionHelper.getUserId()).thenReturn("USER001");
                when(sessionHelper.getFacilityId()).thenReturn("FACILITY001");

                List<ExImportLogDetailResponse> mockDetails = Arrays.asList(
                                createMockImportLogDetail("DETAIL001", "Success"),
                                createMockImportLogDetail("DETAIL002", "Error"));
                when(importLogDetailRepository.getAllList(logId, "USER001", "FACILITY001")).thenReturn(mockDetails);

                // Act
                ResponseEntity<?> response = exFactoryService.historyLogDetail(dataRequest, logId);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertTrue(response.getBody().toString().contains("Lấy danh sách dữ liệu thành công"));
        }

        // ========== createExcelStream Tests ==========

        @Test
        void createExcelStream_ValidData_ReturnsByteArray() throws IOException {
                // Arrange
                List<String> headers = Arrays.asList("Header1", "Header2");
                List<Map<String, String>> data = Arrays.asList(
                                Map.of("Header1", "Value1", "Header2", "Value2"));
                List<String> projectNames = Arrays.asList("Project1", "Project2");
                List<String> projectIds = Arrays.asList("ID1", "ID2");
                List<String> staffList = Arrays.asList("Staff1 (S001)", "Staff2 (S002)");

                // Act
                byte[] result = EXFactoryServiceImpl.createExcelStream("TestSheet", headers, data, projectNames,
                                projectIds,
                                staffList);

                // Assert
                assertNotNull(result);
                assertTrue(result.length > 0);
        }

        @Test
        void createExcelStream_EmptyData_ReturnsValidFile() throws IOException {
                // Arrange
                List<String> headers = Arrays.asList("Header1", "Header2");
                List<Map<String, String>> data = new ArrayList<>();
                List<String> projectNames = new ArrayList<>();
                List<String> projectIds = new ArrayList<>();
                List<String> staffList = new ArrayList<>();

                // Act
                byte[] result = EXFactoryServiceImpl.createExcelStream("TestSheet", headers, data, projectNames,
                                projectIds,
                                staffList);

                // Assert
                assertNotNull(result);
                assertTrue(result.length > 0);
        }

        // ========== Helper Methods ==========

        private UserStaff createMockUserStaff(String name, String code) {
                UserStaff userStaff = new UserStaff();
                userStaff.setName(name);
                userStaff.setCode(code);
                return userStaff;
        }

        private ExImportLogResponse createMockImportLog(String id, String status) {
                ExImportLogResponse log = mock(ExImportLogResponse.class);
                when(log.getId()).thenReturn(id);
                // ExImportLogResponse does not have getStatus(), so skip it
                return log;
        }

        private ExImportLogDetailResponse createMockImportLogDetail(String id, String status) {
                ExImportLogDetailResponse detail = mock(ExImportLogDetailResponse.class);
                when(detail.getLine()).thenReturn(id);
                // getStatus returns Integer, so parse status as int if possible
                Integer statusInt = null;
                try {
                        statusInt = Integer.parseInt(status);
                } catch (Exception e) {
                        statusInt = 0;
                }
                when(detail.getStatus()).thenReturn(statusInt);
                return detail;
        }
}