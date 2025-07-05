package udpm.hn.studentattendance.core.teacher.teachingschedule.service.impl;

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
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.request.TCTSPlanDateUpdateRequest;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.request.TCTeachingScheduleRequest;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.response.TCTSDetailPlanDateResponse;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.response.TCTeachingScheduleResponse;
import udpm.hn.studentattendance.core.teacher.teachingschedule.repository.TCTSFactoryExtendRepository;
import udpm.hn.studentattendance.core.teacher.teachingschedule.repository.TCTSProjectExtendRepository;
import udpm.hn.studentattendance.core.teacher.teachingschedule.repository.TCTSSubjectExtendRepository;
import udpm.hn.studentattendance.core.teacher.teachingschedule.repository.TCTeachingScheduleExtendRepository;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.PlanFactory;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.entities.Subject;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TCTeachingScheduleServiceImplTest {

    @Mock
    private TCTeachingScheduleExtendRepository teachingScheduleRepository;

    @Mock
    private TCTSProjectExtendRepository projectRepository;

    @Mock
    private TCTSSubjectExtendRepository subjectRepository;

    @Mock
    private TCTSFactoryExtendRepository factoryRepository;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private RedisService redisService;

    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @InjectMocks
    private TCTeachingScheduleServiceImpl teachingScheduleService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(teachingScheduleService, "MAX_LATE_ARRIVAL", 15);
        ReflectionTestUtils.setField(teachingScheduleService, "redisTTL", 3600L);
    }

    @Test
    @DisplayName("getCachedTeachingSchedule should return data from cache when available")
    void testGetCachedTeachingSchedule_CacheHit() {
        // Arrange
        String userId = "teacher-1";
        TCTeachingScheduleRequest request = new TCTeachingScheduleRequest();
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "list_" + userId + "_"
                + request.toString();

        PageableObject<?> cachedData = new PageableObject<>();
        when(redisService.get(cacheKey)).thenReturn(cachedData);
        when(redisService.getObject(cacheKey, PageableObject.class)).thenReturn(cachedData);
        when(sessionHelper.getUserId()).thenReturn(userId);

        // Act
        PageableObject<?> result = teachingScheduleService.getCachedTeachingSchedule(request);

        // Assert
        assertNotNull(result);
        assertSame(cachedData, result);
        verify(redisService).get(cacheKey);
        verify(redisService).getObject(cacheKey, PageableObject.class);
        verifyNoInteractions(teachingScheduleRepository);
    }

    @Test
    @DisplayName("getCachedTeachingSchedule should fetch and cache data when not in cache")
    void testGetCachedTeachingSchedule_CacheMiss() {
        // Arrange
        String userId = "teacher-1";
        TCTeachingScheduleRequest request = new TCTeachingScheduleRequest();
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "list_" + userId + "_"
                + request.toString();

        when(redisService.get(cacheKey)).thenReturn(null);
        when(sessionHelper.getUserId()).thenReturn(userId);

        Page<TCTeachingScheduleResponse> page = new PageImpl<>(new ArrayList<>());
        when(teachingScheduleRepository.getAllTeachingScheduleByStaff(eq(userId), any(Pageable.class), eq(request)))
                .thenReturn(page);

        // Act
        PageableObject<?> result = teachingScheduleService.getCachedTeachingSchedule(request);

        // Assert
        assertNotNull(result);
        verify(redisService).get(cacheKey);
        verify(teachingScheduleRepository).getAllTeachingScheduleByStaff(eq(userId), any(Pageable.class), eq(request));
        verify(redisService).set(eq(cacheKey), any(PageableObject.class), eq(3600L));
    }

    @Test
    @DisplayName("getAllTeachingScheduleByStaff should use cached data")
    void testGetAllTeachingScheduleByStaff() {
        // Arrange
        String userId = "teacher-1";
        TCTeachingScheduleRequest request = new TCTeachingScheduleRequest();
        when(sessionHelper.getUserId()).thenReturn(userId);

        PageableObject<?> mockedResult = mock(PageableObject.class);
        doReturn(mockedResult).when(teachingScheduleService).getCachedTeachingSchedule(request);
        when(sessionHelper.getUserName()).thenReturn("Teacher Name");

        // Act
        ResponseEntity<?> response = teachingScheduleService.getAllTeachingScheduleByStaff(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertTrue(apiResponse.getMessage().contains("Lấy tất cả lịch dạy của"));
        assertEquals(mockedResult, apiResponse.getData());

        verify(teachingScheduleService).getCachedTeachingSchedule(request);
        verifyNoMoreInteractions(teachingScheduleRepository);
    }

    @Test
    @DisplayName("getCachedFactories should return data from cache when available")
    void testGetCachedFactories_CacheHit() {
        // Arrange
        String userId = "teacher-1";
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "factories_" + userId;

        List<Factory> cachedFactories = Arrays.asList(mock(Factory.class), mock(Factory.class));
        when(redisService.get(cacheKey)).thenReturn(cachedFactories);
        when(redisService.getObject(cacheKey, List.class)).thenReturn(cachedFactories);
        when(sessionHelper.getUserId()).thenReturn(userId);

        // Act
        List<Factory> result = teachingScheduleService.getCachedFactories();

        // Assert
        assertNotNull(result);
        assertSame(cachedFactories, result);
        verify(redisService).get(cacheKey);
        verify(redisService).getObject(cacheKey, List.class);
        verifyNoInteractions(factoryRepository);
    }

    @Test
    @DisplayName("getAllFactoryByStaff should use cached data")
    void testGetAllFactoryByStaff() {
        // Arrange
        String userId = "teacher-1";
        when(sessionHelper.getUserId()).thenReturn(userId);
        when(sessionHelper.getUserName()).thenReturn("Teacher Name");

        List<Factory> mockFactories = Arrays.asList(
                mock(Factory.class),
                mock(Factory.class));
        doReturn(mockFactories).when(teachingScheduleService).getCachedFactories();

        // Act
        ResponseEntity<?> response = teachingScheduleService.getAllFactoryByStaff();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getMessage().contains("Lấy tất cả nhóm xửng của"));
        assertEquals(mockFactories, apiResponse.getData());

        verify(teachingScheduleService).getCachedFactories();
        verifyNoInteractions(factoryRepository);
    }

    @Test
    @DisplayName("getCachedProjects should return data from cache when available")
    void testGetCachedProjects_CacheHit() {
        // Arrange
        String userId = "teacher-1";
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "projects_" + userId;

        List<Project> cachedProjects = Arrays.asList(mock(Project.class), mock(Project.class));
        when(redisService.get(cacheKey)).thenReturn(cachedProjects);
        when(redisService.getObject(cacheKey, List.class)).thenReturn(cachedProjects);
        when(sessionHelper.getUserId()).thenReturn(userId);

        // Act
        List<Project> result = teachingScheduleService.getCachedProjects();

        // Assert
        assertNotNull(result);
        assertSame(cachedProjects, result);
        verify(redisService).get(cacheKey);
        verify(redisService).getObject(cacheKey, List.class);
        verifyNoInteractions(projectRepository);
    }

    @Test
    @DisplayName("getAllProjectByStaff should use cached data")
    void testGetAllProjectByStaff() {
        // Arrange
        String userId = "teacher-1";
        when(sessionHelper.getUserId()).thenReturn(userId);
        when(sessionHelper.getUserName()).thenReturn("Teacher Name");

        List<Project> mockProjects = Arrays.asList(
                mock(Project.class),
                mock(Project.class));
        doReturn(mockProjects).when(teachingScheduleService).getCachedProjects();

        // Act
        ResponseEntity<?> response = teachingScheduleService.getAllProjectByStaff();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getMessage().contains("Lấy tất cả dự án đang dạy của"));
        assertEquals(mockProjects, apiResponse.getData());

        verify(teachingScheduleService).getCachedProjects();
        verifyNoInteractions(projectRepository);
    }

    @Test
    @DisplayName("getCachedSubjects should return data from cache when available")
    void testGetCachedSubjects_CacheHit() {
        // Arrange
        String userId = "teacher-1";
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "subjects_" + userId;

        List<Subject> cachedSubjects = Arrays.asList(mock(Subject.class), mock(Subject.class));
        when(redisService.get(cacheKey)).thenReturn(cachedSubjects);
        when(redisService.getObject(cacheKey, List.class)).thenReturn(cachedSubjects);
        when(sessionHelper.getUserId()).thenReturn(userId);

        // Act
        List<Subject> result = teachingScheduleService.getCachedSubjects();

        // Assert
        assertNotNull(result);
        assertSame(cachedSubjects, result);
        verify(redisService).get(cacheKey);
        verify(redisService).getObject(cacheKey, List.class);
        verifyNoInteractions(subjectRepository);
    }

    @Test
    @DisplayName("getAllSubjectByStaff should use cached data")
    void testGetAllSubjectByStaff() {
        // Arrange
        String userId = "teacher-1";
        when(sessionHelper.getUserId()).thenReturn(userId);
        when(sessionHelper.getUserName()).thenReturn("Teacher Name");

        List<Subject> mockSubjects = Arrays.asList(
                mock(Subject.class),
                mock(Subject.class));
        doReturn(mockSubjects).when(teachingScheduleService).getCachedSubjects();

        // Act
        ResponseEntity<?> response = teachingScheduleService.getAllSubjectByStaff();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getMessage().contains("Lấy tất cả môn học của"));
        assertEquals(mockSubjects, apiResponse.getData());

        verify(teachingScheduleService).getCachedSubjects();
        verifyNoInteractions(subjectRepository);
    }

    @Test
    @DisplayName("invalidateTeachingScheduleCache should delete specific user cache")
    void testInvalidateTeachingScheduleCache() {
        // Arrange
        String userId = "teacher-1";
        String cachePattern = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "list_" + userId + "_*";

        // Use reflection to access private method
        ReflectionTestUtils.invokeMethod(teachingScheduleService, "invalidateTeachingScheduleCaches");

        // Then
        verify(redisService).deletePattern(contains(cachePattern));
    }

    @Test
    @DisplayName("invalidateAllTeachingScheduleCaches should delete all teaching schedule caches")
    void testInvalidateAllTeachingScheduleCaches() {
        // Arrange
        String cachePattern = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "*";

        // Use reflection to access private method
        ReflectionTestUtils.invokeMethod(teachingScheduleService, "invalidateTeachingScheduleCaches");

        // Then
        verify(redisService).deletePattern(contains(cachePattern));
    }

    @Test
    @DisplayName("updatePlanDate should invalidate caches after successful update")
    void testUpdatePlanDate_WithCacheInvalidation() {
        // Arrange
        String planDateId = "plan-date-1";
        String userId = "teacher-1";

        when(sessionHelper.getUserId()).thenReturn(userId);

        TCTSPlanDateUpdateRequest request = new TCTSPlanDateUpdateRequest();
        request.setIdPlanDate(planDateId);
        request.setDescription("Updated description");
        request.setLateArrival(10);
        request.setLink("https://example.com/meet");
        request.setRoom("Room 101");

        PlanDate planDate = mock(PlanDate.class);
        PlanFactory planFactory = mock(PlanFactory.class);
        Factory factory = mock(Factory.class);
        UserStaff teacher = mock(UserStaff.class);

        when(planDate.getPlanFactory()).thenReturn(planFactory);
        when(planFactory.getFactory()).thenReturn(factory);
        when(factory.getUserStaff()).thenReturn(teacher);
        when(teacher.getId()).thenReturn(userId);

        when(teachingScheduleRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
        when(teachingScheduleRepository.isOutOfTime(planDateId)).thenReturn(false);
        when(teachingScheduleRepository.save(planDate)).thenReturn(planDate);

        // Act
        ResponseEntity<?> response = teachingScheduleService.updatePlanDate(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify cache invalidation
        verify(redisService).delete(RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "plan_date_" + planDateId);
    }

    @Test
    @DisplayName("getCachedPlanDateDetail should return data from cache when available")
    void testGetCachedPlanDateDetail_CacheHit() {
        // Arrange
        String planDateId = "plan-date-1";
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "plan_date_" + planDateId;

        TCTSDetailPlanDateResponse cachedDetail = mock(TCTSDetailPlanDateResponse.class);
        when(redisService.get(cacheKey)).thenReturn(cachedDetail);
        when(redisService.getObject(cacheKey, TCTSDetailPlanDateResponse.class)).thenReturn(cachedDetail);

        // Act
        TCTSDetailPlanDateResponse result = teachingScheduleService.getCachedPlanDateDetail(planDateId);

        // Assert
        assertNotNull(result);
        assertSame(cachedDetail, result);
        verify(redisService).get(cacheKey);
        verify(redisService).getObject(cacheKey, TCTSDetailPlanDateResponse.class);
        verifyNoInteractions(teachingScheduleRepository);
    }

    @Test
    @DisplayName("getDetailPlanDate should use cached data")
    void testGetDetailPlanDate_Success() {
        // Arrange
        String planDateId = "plan-date-1";
        TCTSDetailPlanDateResponse mockResponse = mock(TCTSDetailPlanDateResponse.class);
        doReturn(mockResponse).when(teachingScheduleService).getCachedPlanDateDetail(planDateId);

        // Act
        ResponseEntity<?> response = teachingScheduleService.getDetailPlanDate(planDateId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy chi tiết kế hoạch thành công", apiResponse.getMessage());
        assertEquals(mockResponse, apiResponse.getData());

        verify(teachingScheduleService).getCachedPlanDateDetail(planDateId);
        verifyNoInteractions(teachingScheduleRepository);
    }

    @Test
    @DisplayName("changeTypePlanDate should invalidate caches after successful change")
    void testChangeTypePlanDate_WithCacheInvalidation() {
        // Arrange
        String planDateId = "plan-date-1";
        String room = "Room 101";
        String userId = "teacher-1";

        when(sessionHelper.getUserId()).thenReturn(userId);

        PlanDate planDate = mock(PlanDate.class);
        PlanFactory planFactory = mock(PlanFactory.class);
        Factory factory = mock(Factory.class);
        UserStaff teacher = mock(UserStaff.class);

        when(planDate.getPlanFactory()).thenReturn(planFactory);
        when(planFactory.getFactory()).thenReturn(factory);
        when(factory.getUserStaff()).thenReturn(teacher);
        when(teacher.getId()).thenReturn(userId);

        when(planDate.getType()).thenReturn(ShiftType.OFFLINE); // Initially OFFLINE
        when(teachingScheduleRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
        when(teachingScheduleRepository.isOutOfTime(planDateId)).thenReturn(false);
        when(teachingScheduleRepository.save(planDate)).thenReturn(planDate);

        // Act
        ResponseEntity<?> response = teachingScheduleService.changeTypePlanDate(planDateId, room);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify cache invalidation
        verify(redisService).delete(RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "plan_date_" + planDateId);
    }
}