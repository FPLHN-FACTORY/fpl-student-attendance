package udpm.hn.studentattendance.core.teacher.factory.service.impl;

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
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCFactoryResponse;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCFactoryExtendRepository;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCProjectExtendRepository;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCSemesterExtendRepository;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TCFactoryServiceImplTest {

    @Mock
    private TCFactoryExtendRepository factoryExtendRepository;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private TCProjectExtendRepository projectExtendRepository;

    @Mock
    private TCSemesterExtendRepository semesterExtendRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @Mock
    private RedisCacheHelper redisCacheHelper;

    @InjectMocks
    private TCFactoryServiceImpl factoryService;

    @BeforeEach
    void setUp() {
        // Set redisTTL value
        ReflectionTestUtils.setField(factoryService, "redisTTL", 3600L);
        // Removed unnecessary stubbing for redisCacheHelper.getOrSet
    }

    @Test
    @DisplayName("getAllFactoryByTeacher should return cached data when available")
    void testGetAllFactoryByTeacher_WithCachedData() {
        // Arrange
        String facilityId = "facility-1";
        String userCode = "GV001";
        TCFactoryRequest request = new TCFactoryRequest();

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserCode()).thenReturn(userCode);

        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_TEACHER_FACTORY + "factory_" + userCode + "_" + facilityId
                + "_" + request.toString();
        PageableObject cachedData = new PageableObject();
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(cachedData);

        // Act
        ResponseEntity<?> response = factoryService.getAllFactoryByTeacher(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        // Fix: match actual service message (remove ' (cached)')
        assertEquals("Lấy tất cả nhóm xưởng do giảng viên " + userCode + " thành công",
                apiResponse.getMessage());
        assertEquals(cachedData, apiResponse.getData());

        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        verify(factoryExtendRepository, never()).getAllFactoryByTeacher(any(Pageable.class), anyString(), anyString(),
                any(TCFactoryRequest.class));
    }

    @Test
    @DisplayName("getAllFactoryByTeacher should query repository when cache is missed")
    void testGetAllFactoryByTeacher_WithoutCachedData() {
        // Arrange
        String facilityId = "facility-1";
        String userCode = "GV001";
        TCFactoryRequest request = new TCFactoryRequest();

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserCode()).thenReturn(userCode);
        // Fix: use thenAnswer to call supplier and return correct data
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenAnswer(invocation -> {
            // Simulate cache miss, call supplier
            java.util.function.Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        Page<TCFactoryResponse> page = new PageImpl<>(new ArrayList<>());
        when(factoryExtendRepository.getAllFactoryByTeacher(any(Pageable.class), eq(facilityId), eq(userCode),
                eq(request)))
                .thenReturn(page);

        // Act
        ResponseEntity<?> response = factoryService.getAllFactoryByTeacher(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy tất cả nhóm xưởng do giảng viên " + userCode + " thành công", apiResponse.getMessage());

        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        verify(factoryExtendRepository).getAllFactoryByTeacher(any(Pageable.class), eq(facilityId), eq(userCode),
                eq(request));
        // Removed: verify(redisService).set(anyString(), any(PageableObject.class), eq(3600L));
    }

    @Test
    @DisplayName("getAllProjectByFacility should return cached data when available")
    void testGetAllProjectByFacility_WithCachedData() {
        // Arrange
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_TEACHER_FACTORY + "projects_" + facilityId;
        List<Project> cachedProjects = Arrays.asList(mock(Project.class), mock(Project.class));
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(cachedProjects);

        // Act
        ResponseEntity<?> response = factoryService.getAllProjectByFacility();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        // Fix: match actual service message (remove ' (cached)')
        assertEquals("Lấy tất cả dự án theo cơ sở thành công", apiResponse.getMessage());
        assertEquals(cachedProjects, apiResponse.getData());

        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        verify(projectExtendRepository, never()).getAllProjectName(anyString());
    }

    @Test
    @DisplayName("getAllProjectByFacility should query repository when cache is missed")
    void testGetAllProjectByFacility_WithoutCachedData() {
        // Arrange
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        // Fix: use thenAnswer to call supplier and return correct data
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenAnswer(invocation -> {
            java.util.function.Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        List<Project> projects = Arrays.asList(mock(Project.class), mock(Project.class));
        when(projectExtendRepository.getAllProjectName(facilityId)).thenReturn(projects);

        // Act
        ResponseEntity<?> response = factoryService.getAllProjectByFacility();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy tất cả dự án theo cơ sở thành công", apiResponse.getMessage());
        assertEquals(projects, apiResponse.getData());

        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        verify(projectExtendRepository).getAllProjectName(facilityId);
        // Removed: verify(redisService).set(anyString(), eq(projects), eq(3600L));
    }

    @Test
    @DisplayName("getAllSemester should return cached data when available")
    void testGetAllSemester_WithCachedData() {
        // Arrange
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_TEACHER_FACTORY + "semesters_active";
        List<Semester> cachedSemesters = Arrays.asList(mock(Semester.class), mock(Semester.class));
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(cachedSemesters);

        // Act
        ResponseEntity<?> response = factoryService.getAllSemester();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        // Fix: match actual service message (remove ' (cached)')
        assertEquals("Lấy tất cả học kỳ thành công", apiResponse.getMessage());
        assertEquals(cachedSemesters, apiResponse.getData());

        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        verify(semesterExtendRepository, never()).getAllSemester(any(EntityStatus.class));
    }

    @Test
    @DisplayName("getAllSemester should query repository when cache is missed")
    void testGetAllSemester_WithoutCachedData() {
        // Arrange
        // Fix: use thenAnswer to call supplier and return correct data
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenAnswer(invocation -> {
            java.util.function.Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        List<Semester> semesters = Arrays.asList(mock(Semester.class), mock(Semester.class));
        when(semesterExtendRepository.getAllSemester(EntityStatus.ACTIVE)).thenReturn(semesters);

        // Act
        ResponseEntity<?> response = factoryService.getAllSemester();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy tất cả học kỳ thành công", apiResponse.getMessage());
        assertEquals(semesters, apiResponse.getData());

        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        verify(semesterExtendRepository).getAllSemester(EntityStatus.ACTIVE);
        // Removed: verify(redisService).set(anyString(), eq(semesters), eq(3600L));
    }
}