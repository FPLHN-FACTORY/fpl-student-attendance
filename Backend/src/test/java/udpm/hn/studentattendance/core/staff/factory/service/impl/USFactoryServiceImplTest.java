package udpm.hn.studentattendance.core.staff.factory.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.core.notification.model.request.NotificationAddRequest;
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.model.response.USDetailFactoryResponse;
import udpm.hn.studentattendance.core.staff.factory.model.response.USFactoryResponse;
import udpm.hn.studentattendance.core.staff.factory.model.response.USProjectFactoryResponse;
import udpm.hn.studentattendance.core.staff.factory.repository.factory.*;
import udpm.hn.studentattendance.entities.*;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class USFactoryServiceImplTest {

        @Mock
        private USFactoryExtendRepository factoryRepository;

        @Mock
        private USProjectFactoryExtendRepository projectFactoryExtendRepository;

        @Mock
        private USStaffFactoryExtendRepository staffFactoryExtendRepository;

        @Mock
        private USSubjectFacilityFactoryExtendRepository subjectFacilityFactoryExtendRepository;

        @Mock
        private USFactoryPlanExtendRepository factoryPlanExtendRepository;

        @Mock
        private USFactorySemesterExtendRepository semesterRepository;

        @Mock
        private NotificationService notificationService;

        @Mock
        private CommonUserStudentRepository commonUserStudentRepository;

        @Mock
        private SessionHelper sessionHelper;

        @Mock
        private USFactoryProjectPlanExtendRepository projectPlanExtendRepository;

        @Mock
        private UserActivityLogHelper userActivityLogHelper;

        @Mock
        private RedisService redisService;

        @Mock
        private RedisInvalidationHelper redisInvalidationHelper;

        @Mock
        private RedisCacheHelper redisCacheHelper;

        @InjectMocks
        private USFactoryServiceImpl factoryService;

        @BeforeEach
        void setUp() {
                // Set Redis TTL value
                ReflectionTestUtils.setField(factoryService, "redisTTL", 3600L);
                // Removed unnecessary stubbing for redisCacheHelper.getOrSet
        }

        @Test
        @DisplayName("getAllFactory should return paginated factory list")
        void testGetAllFactory() {
                // Arrange
                String facilityId = "facility-1";
                USFactoryRequest request = new USFactoryRequest();

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);

                Page<USFactoryResponse> page = new PageImpl<>(new ArrayList<>());
                when(factoryRepository.getAllFactory(any(Pageable.class), eq(facilityId), eq(request)))
                                .thenReturn(page);

                // Mock Redis cache miss
                when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong()))
                                .thenAnswer(invocation -> {
                                        java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                                        return supplier.get();
                                });

                // Act
                ResponseEntity<?> response = factoryService.getAllFactory(request);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
                assertEquals("Hiển thị tất cả nhóm xưởng thành công", apiResponse.getMessage());

                verify(factoryRepository).getAllFactory(any(Pageable.class), eq(facilityId), eq(request));
                verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        }

        @Test
        @DisplayName("getAllProject should return project list by facility")
        void testGetAllProject() {
                // Arrange
                String facilityId = "facility-1";
                when(sessionHelper.getFacilityId()).thenReturn(facilityId);

                List<USProjectFactoryResponse> mockProjects = Arrays.asList(
                                mock(USProjectFactoryResponse.class),
                                mock(USProjectFactoryResponse.class));

                when(projectFactoryExtendRepository.getAllProject(facilityId)).thenReturn(mockProjects);

                // Mock Redis cache miss
                when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong()))
                                .thenAnswer(invocation -> {
                                        java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                                        return supplier.get();
                                });

                // Act
                ResponseEntity<?> response = factoryService.getAllProject();

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
                assertEquals("Lấy tất cả dự án theo cơ sở thành công", apiResponse.getMessage());
                assertEquals(mockProjects, apiResponse.getData());

                verify(projectFactoryExtendRepository).getAllProject(facilityId);
                verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        }

        @Test
        @DisplayName("getAllSubjectFacility should return subject facilities by facility")
        void testGetAllSubjectFacility() {
                // Arrange
                String facilityId = "facility-1";
                when(sessionHelper.getFacilityId()).thenReturn(facilityId);

                List<SubjectFacility> mockSubjectFacilities = Arrays.asList(
                                mock(SubjectFacility.class),
                                mock(SubjectFacility.class));

                when(subjectFacilityFactoryExtendRepository.getAllSubjectFacility(
                                EntityStatus.ACTIVE,
                                EntityStatus.ACTIVE,
                                facilityId))
                                .thenReturn(mockSubjectFacilities);

                // Mock Redis cache miss
                when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong()))
                                .thenAnswer(invocation -> {
                                        java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                                        return supplier.get();
                                });

                // Act
                ResponseEntity<?> response = factoryService.getAllSubjectFacility();

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
                assertEquals("Lấy tất cả bộ môn cơ sở thành công", apiResponse.getMessage());
                assertEquals(mockSubjectFacilities, apiResponse.getData());

                verify(subjectFacilityFactoryExtendRepository).getAllSubjectFacility(
                                EntityStatus.ACTIVE,
                                EntityStatus.ACTIVE,
                                facilityId);
                verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        }

        @Test
        @DisplayName("getAllStaff should return staff list by facility")
        void testGetAllStaff() {
                // Arrange
                String facilityId = "facility-1";
                when(sessionHelper.getFacilityId()).thenReturn(facilityId);

                List<UserStaff> mockStaffs = Arrays.asList(
                                mock(UserStaff.class),
                                mock(UserStaff.class));

                when(staffFactoryExtendRepository.getListUserStaff(
                                EntityStatus.ACTIVE,
                                EntityStatus.ACTIVE,
                                facilityId,
                                RoleConstant.TEACHER))
                                .thenReturn(mockStaffs);

                // Mock Redis cache miss
                when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong()))
                                .thenAnswer(invocation -> {
                                        java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                                        return supplier.get();
                                });

                // Act
                ResponseEntity<?> response = factoryService.getAllStaff();

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
                assertEquals("Lấy tất cả giảng viên theo cơ sở thành công", apiResponse.getMessage());
                assertEquals(mockStaffs, apiResponse.getData());

                verify(staffFactoryExtendRepository).getListUserStaff(
                                EntityStatus.ACTIVE,
                                EntityStatus.ACTIVE,
                                facilityId,
                                RoleConstant.TEACHER);
                verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        }

        @Test
        @DisplayName("getDetailFactory should return factory details when factory exists")
        void testGetDetailFactory_Success() {
                // Arrange
                String factoryId = "factory-1";
                USDetailFactoryResponse mockResponse = mock(USDetailFactoryResponse.class);

                when(factoryRepository.getFactoryById(factoryId)).thenReturn(Optional.of(mockResponse));

                // Act
                ResponseEntity<?> response = factoryService.getDetailFactory(factoryId);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
                assertEquals("Xem chi tiết nhóm xưởng thành công", apiResponse.getMessage());

                verify(factoryRepository).getFactoryById(factoryId);
        }

        @Test
        @DisplayName("getDetailFactory should return error when factory does not exist")
        void testGetDetailFactory_NotFound() {
                // Arrange
                String factoryId = "non-existent-factory";

                when(factoryRepository.getFactoryById(factoryId)).thenReturn(Optional.empty());

                // Mock Redis cache miss
                when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(null);

                // Act
                ResponseEntity<?> response = factoryService.getDetailFactory(factoryId);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Nhóm xưởng không tồn tại", apiResponse.getMessage());

                verify(factoryRepository).getFactoryById(factoryId);
                verify(redisCacheHelper, never()).getOrSet(anyString(), any(), any(), anyLong());
        }

        @Test
        @DisplayName("createFactory should create new factory successfully")
        void testCreateFactory_Success() {
                // Arrange
                String facilityId = "facility-1";
                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(sessionHelper.getUserCode()).thenReturn("USER123");
                when(sessionHelper.getUserName()).thenReturn("Test User");

                USFactoryCreateUpdateRequest request = new USFactoryCreateUpdateRequest();
                request.setFactoryName("Test Factory");
                request.setFactoryDescription("Test Description");
                request.setIdUserStaff("staff-1");
                request.setIdProject("project-1");

                UserStaff mockStaff = mock(UserStaff.class);
                when(mockStaff.getId()).thenReturn("staff-1");

                Project mockProject = mock(Project.class);
                when(mockProject.getId()).thenReturn("project-1");
                when(mockProject.getName()).thenReturn("Test Project");

                when(staffFactoryExtendRepository.findById("staff-1")).thenReturn(Optional.of(mockStaff));
                when(projectFactoryExtendRepository.findById("project-1")).thenReturn(Optional.of(mockProject));
                when(factoryRepository.isExistNameAndProject(anyString(), anyString(), isNull()))
                                .thenReturn(false);

                Factory savedFactory = new Factory();
                savedFactory.setId("new-factory-id");
                savedFactory.setName(request.getFactoryName());
                when(factoryRepository.save(any(Factory.class))).thenReturn(savedFactory);

                // Mock notification service
                doAnswer(invocation -> {
                        NotificationAddRequest notificationRequest = invocation.getArgument(0);
                        return null; // Return value doesn't matter for void methods
                }).when(notificationService).add(any(NotificationAddRequest.class));

                doNothing().when(userActivityLogHelper).saveLog(anyString());
                doNothing().when(redisInvalidationHelper).invalidateAllCaches();

                // Act
                ResponseEntity<?> response = factoryService.createFactory(request);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
                assertEquals("Thêm nhóm xưởng mới thành công", apiResponse.getMessage());

                verify(factoryRepository).save(any(Factory.class));
                verify(notificationService).add(any(NotificationAddRequest.class));
                verify(userActivityLogHelper).saveLog(contains("vừa thêm 1 nhóm xưởng mới"));
                verify(redisInvalidationHelper).invalidateAllCaches();
        }

        @Test
        @DisplayName("createFactory should return error when factory already exists")
        void testCreateFactory_AlreadyExists() {
                // Arrange
                USFactoryCreateUpdateRequest request = new USFactoryCreateUpdateRequest();
                request.setFactoryName("Existing Factory");
                request.setIdUserStaff("staff-1");
                request.setIdProject("project-1");

                UserStaff mockStaff = mock(UserStaff.class);
                Project mockProject = mock(Project.class);

                when(staffFactoryExtendRepository.findById("staff-1")).thenReturn(Optional.of(mockStaff));
                when(projectFactoryExtendRepository.findById("project-1")).thenReturn(Optional.of(mockProject));
                when(factoryRepository.isExistNameAndProject(request.getFactoryName(), request.getIdProject(), null))
                                .thenReturn(true);

                // Act
                ResponseEntity<?> response = factoryService.createFactory(request);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Nhóm xưởng đã tồn tại trong dự án này", apiResponse.getMessage());

                verify(factoryRepository, never()).save(any(Factory.class));
                verify(redisInvalidationHelper, never()).invalidateAllCaches();
        }

        @Test
        @DisplayName("createFactory should return error when staff not found")
        void testCreateFactory_StaffNotFound() {
                // Arrange
                USFactoryCreateUpdateRequest request = new USFactoryCreateUpdateRequest();
                request.setFactoryName("Test Factory");
                request.setIdUserStaff("staff-404");
                request.setIdProject("project-1");

                when(staffFactoryExtendRepository.findById("staff-404")).thenReturn(Optional.empty());

                // Act
                ResponseEntity<?> response = factoryService.createFactory(request);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Giảng viên không tồn tại", apiResponse.getMessage());

                verify(factoryRepository, never()).save(any(Factory.class));
                verify(redisInvalidationHelper, never()).invalidateAllCaches();
        }

        @Test
        @DisplayName("createFactory should return error when project not found")
        void testCreateFactory_ProjectNotFound() {
                // Arrange
                USFactoryCreateUpdateRequest request = new USFactoryCreateUpdateRequest();
                request.setFactoryName("Test Factory");
                request.setIdUserStaff("staff-1");
                request.setIdProject("project-404");

                UserStaff mockStaff = mock(UserStaff.class);
                when(staffFactoryExtendRepository.findById("staff-1")).thenReturn(Optional.of(mockStaff));
                when(projectFactoryExtendRepository.findById("project-404")).thenReturn(Optional.empty());

                // Act
                ResponseEntity<?> response = factoryService.createFactory(request);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
                assertEquals("Dự án không tồn tại", apiResponse.getMessage());

                verify(factoryRepository, never()).save(any(Factory.class));
                verify(redisInvalidationHelper, never()).invalidateAllCaches();
        }

        @Test
        @DisplayName("updateFactory should update factory successfully")
        void testUpdateFactory_Success() {
                // Arrange
                USFactoryCreateUpdateRequest request = new USFactoryCreateUpdateRequest();
                request.setId("factory-1");
                request.setFactoryName("Updated Factory");
                request.setFactoryDescription("Updated Description");
                request.setIdUserStaff("staff-1");
                request.setIdProject("project-1");

                Project oldProject = mock(Project.class);
                when(oldProject.getId()).thenReturn("old-project-1");

                Factory existingFactory = mock(Factory.class);
                when(existingFactory.getId()).thenReturn("factory-1");
                when(existingFactory.getName()).thenReturn("Old Factory Name");
                when(existingFactory.getProject()).thenReturn(oldProject);
                when(factoryRepository.findById("factory-1")).thenReturn(Optional.of(existingFactory));

                UserStaff mockStaff = mock(UserStaff.class);
                when(mockStaff.getId()).thenReturn("staff-1");
                when(staffFactoryExtendRepository.findById("staff-1")).thenReturn(Optional.of(mockStaff));

                Project mockProject = mock(Project.class);
                when(mockProject.getId()).thenReturn("project-1");
                when(projectFactoryExtendRepository.findById("project-1")).thenReturn(Optional.of(mockProject));

                when(factoryRepository.isExistNameAndProject(request.getFactoryName(), request.getIdProject(),
                                "factory-1"))
                                .thenReturn(false);

                Factory updatedFactory = new Factory();
                updatedFactory.setId("factory-1");
                updatedFactory.setName(request.getFactoryName());
                when(factoryRepository.save(any(Factory.class))).thenReturn(updatedFactory);

                doNothing().when(userActivityLogHelper).saveLog(anyString());
                doNothing().when(redisInvalidationHelper).invalidateAllCaches();

                // Act
                ResponseEntity<?> response = factoryService.updateFactory(request);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
                assertEquals("Cập nhật nhóm xưởng thành công", apiResponse.getMessage());

                verify(factoryRepository).save(any(Factory.class));
                verify(userActivityLogHelper).saveLog(contains("vừa cập nhật nhóm xưởng"));
                verify(redisInvalidationHelper).invalidateAllCaches();
        }

        @Test
        @DisplayName("changeStatus should change factory status successfully")
        void testChangeStatus_Success() {
                // Arrange
                String factoryId = "factory-1";
                Project project = mock(Project.class);
                when(project.getName()).thenReturn("Test Project");

                Factory factory = mock(Factory.class);
                when(factory.getId()).thenReturn(factoryId);
                when(factory.getName()).thenReturn("Test Factory");
                when(factory.getStatus()).thenReturn(EntityStatus.ACTIVE);
                when(factory.getProject()).thenReturn(project);
                when(factoryRepository.findById(factoryId)).thenReturn(Optional.of(factory));

                Factory savedFactory = mock(Factory.class);
                when(savedFactory.getId()).thenReturn(factoryId);
                when(savedFactory.getName()).thenReturn("Test Factory");
                when(savedFactory.getStatus()).thenReturn(EntityStatus.INACTIVE);
                when(savedFactory.getProject()).thenReturn(project);
                when(factoryRepository.save(any(Factory.class))).thenReturn(savedFactory);

                doNothing().when(userActivityLogHelper).saveLog(anyString());
                doNothing().when(redisInvalidationHelper).invalidateAllCaches();

                // Act
                ResponseEntity<?> response = factoryService.changeStatus(factoryId);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
                assertEquals("Đổi trạng thái nhóm xưởng thành công", apiResponse.getMessage());

                verify(factoryRepository).save(any(Factory.class));
                verify(userActivityLogHelper).saveLog(contains("vừa thay đổi trạng thái nhóm xưởng"));
                verify(redisInvalidationHelper).invalidateAllCaches();
        }
}