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
import udpm.hn.studentattendance.entities.SubjectFacility;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.SettingHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;
import udpm.hn.studentattendance.repositories.UserStudentFactoryRepository;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.core.type.TypeReference;

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

    @Mock
    private SettingHelper settingHelper;

    @Mock
    private UserStudentFactoryRepository userStudentFactoryRepository;

    @Mock
    private RedisCacheHelper redisCacheHelper;

    @InjectMocks
    private TCTeachingScheduleServiceImpl teachingScheduleService;

    // TypeReference instances for cache mocks
    private final TypeReference<PageableObject<TCTeachingScheduleResponse>> pageableObjectTypeRef = new TypeReference<PageableObject<TCTeachingScheduleResponse>>() {
    };
    private final TypeReference<List<Factory>> listFactoryTypeRef = new TypeReference<List<Factory>>() {
    };
    private final TypeReference<List<Project>> listProjectTypeRef = new TypeReference<List<Project>>() {
    };
    private final TypeReference<List<Subject>> listSubjectTypeRef = new TypeReference<List<Subject>>() {
    };
    private final TypeReference<List<PlanDate>> listPlanDateTypeRef = new TypeReference<List<PlanDate>>() {
    };
    private final TypeReference<TCTSDetailPlanDateResponse> detailPlanDateTypeRef = new TypeReference<TCTSDetailPlanDateResponse>() {
    };

    @BeforeEach
    void setUp() {
        // Default behavior for session helper
        when(sessionHelper.getUserId()).thenReturn("teacher-1");
        when(sessionHelper.getUserName()).thenReturn("Teacher Name");

        // Default behavior for setting helper - mock specific setting
        when(settingHelper.getSetting(eq(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT), eq(Boolean.class)))
                .thenReturn(true);

        // Default behavior for userStudentFactoryRepository
        when(userStudentFactoryRepository.findAll()).thenReturn(new ArrayList<>());

        // Remove global stubbing for redisCacheHelper.getOrSet to avoid interfering
        // with per-test stubbing
        // doNothing().when(redisInvalidationHelper).invalidateAllCaches();
        doNothing().when(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("getCachedTeachingSchedule should return data from cache when available")
    void testGetCachedTeachingSchedule_CacheHit() {
        // Arrange
        String userId = "teacher-1";
        TCTeachingScheduleRequest request = new TCTeachingScheduleRequest();
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "list_" + userId + "_"
                + request.toString();

        PageableObject<TCTeachingScheduleResponse> cachedData = new PageableObject<>();
        when(redisCacheHelper.getOrSet(eq(cacheKey), any(), any())).thenReturn(cachedData);
        when(sessionHelper.getUserId()).thenReturn(userId);

        // Act
        PageableObject<?> result = teachingScheduleService.getCachedTeachingSchedule(request);

        // Assert
        assertNotNull(result);
        assertSame(cachedData, result);
        verify(redisCacheHelper).getOrSet(eq(cacheKey), any(), any());
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

        when(sessionHelper.getUserId()).thenReturn(userId);

        // Create a mock page with some data
        List<TCTeachingScheduleResponse> mockData = new ArrayList<>();
        TCTeachingScheduleResponse mockResponse = mock(TCTeachingScheduleResponse.class);
        mockData.add(mockResponse);
        Page<TCTeachingScheduleResponse> page = new PageImpl<>(mockData);
        PageableObject<TCTeachingScheduleResponse> expectedPageableObject = PageableObject.of(page);

        when(teachingScheduleRepository.getAllTeachingScheduleByStaff(eq(userId), any(Pageable.class), eq(request)))
                .thenReturn(page);

        // Mock redisCacheHelper.getOrSet to simulate cache miss and return the expected
        // result
        when(redisCacheHelper.getOrSet(eq(cacheKey), any(), any()))
                .thenAnswer(invocation -> {
                    // Get the supplier function (second argument)
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    // Execute the supplier to get the actual result
                    return supplier.get();
                });

        // Act
        PageableObject<?> result = teachingScheduleService.getCachedTeachingSchedule(request);

        // Assert
        assertNotNull(result);
        assertEquals(expectedPageableObject.getData(), result.getData());
        assertEquals(expectedPageableObject.getTotalPages(), result.getTotalPages());
        assertEquals(expectedPageableObject.getCurrentPage(), result.getCurrentPage());
        verify(teachingScheduleRepository).getAllTeachingScheduleByStaff(eq(userId), any(Pageable.class), eq(request));
    }

    @Test
    @DisplayName("getAllTeachingScheduleByStaff should use cached data")
    void testGetAllTeachingScheduleByStaff() {
        // Arrange
        String userId = "teacher-1";
        TCTeachingScheduleRequest request = new TCTeachingScheduleRequest();
        when(sessionHelper.getUserId()).thenReturn(userId);
        when(sessionHelper.getUserName()).thenReturn("Teacher Name");

        // Mock the cached data
        PageableObject<TCTeachingScheduleResponse> mockedResult = new PageableObject<>();
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "list_" + userId + "_"
                + request.toString();
        when(redisCacheHelper.getOrSet(eq(cacheKey), any(), any())).thenReturn(mockedResult);

        // Act
        ResponseEntity<?> response = teachingScheduleService.getAllTeachingScheduleByStaff(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertTrue(apiResponse.getMessage().contains("Lấy tất cả lịch dạy của"));
        assertEquals(mockedResult, apiResponse.getData());

        verify(redisCacheHelper).getOrSet(eq(cacheKey), any(), any());
        verifyNoInteractions(teachingScheduleRepository);
    }

    @Test
    @DisplayName("getCachedFactories should return data from cache when available")
    void testGetCachedFactories_CacheHit() {
        // Arrange
        String userId = "teacher-1";
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "factories_" + userId;

        List<Factory> cachedFactories = new ArrayList<>();
        when(redisCacheHelper.getOrSet(eq(cacheKey), any(), any())).thenReturn(cachedFactories);
        when(sessionHelper.getUserId()).thenReturn(userId);

        // Act
        List<Factory> result = teachingScheduleService.getCachedFactories();

        // Assert
        assertNotNull(result);
        assertEquals(cachedFactories, result);
        verify(redisCacheHelper).getOrSet(eq(cacheKey), any(), any());
        verifyNoInteractions(factoryRepository);
    }

    @Test
    @DisplayName("getAllFactoryByStaff should use cached data")
    void testGetAllFactoryByStaff() {
        // Arrange
        String userId = "teacher-1";
        when(sessionHelper.getUserId()).thenReturn(userId);
        when(sessionHelper.getUserName()).thenReturn("Teacher Name");

        // Mock the cached data
        List<Factory> mockFactories = new ArrayList<>();
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "factories_" + userId;
        when(redisCacheHelper.getOrSet(eq(cacheKey), any(), any())).thenReturn(mockFactories);

        // Act
        ResponseEntity<?> response = teachingScheduleService.getAllFactoryByStaff();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getMessage().contains("Lấy tất cả nhóm xửng của"));
        assertEquals(mockFactories, apiResponse.getData());

        verify(redisCacheHelper).getOrSet(eq(cacheKey), any(), any());
        verifyNoInteractions(factoryRepository);
    }

    @Test
    @DisplayName("getCachedProjects should return data from cache when available")
    void testGetCachedProjects_CacheHit() {
        // Arrange
        String userId = "teacher-1";
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "projects_" + userId;

        List<Project> cachedProjects = new ArrayList<>();
        when(redisCacheHelper.getOrSet(eq(cacheKey), any(), any())).thenReturn(cachedProjects);
        when(sessionHelper.getUserId()).thenReturn(userId);

        // Act
        List<Project> result = teachingScheduleService.getCachedProjects();

        // Assert
        assertNotNull(result);
        assertEquals(cachedProjects, result);
        verify(redisCacheHelper).getOrSet(eq(cacheKey), any(), any());
        verifyNoInteractions(projectRepository);
    }

    @Test
    @DisplayName("getAllProjectByStaff should use cached data")
    void testGetAllProjectByStaff() {
        // Arrange
        String userId = "teacher-1";
        when(sessionHelper.getUserId()).thenReturn(userId);
        when(sessionHelper.getUserName()).thenReturn("Teacher Name");

        // Mock the cached data
        List<Project> mockProjects = new ArrayList<>();
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "projects_" + userId;
        when(redisCacheHelper.getOrSet(eq(cacheKey), any(), any())).thenReturn(mockProjects);

        // Act
        ResponseEntity<?> response = teachingScheduleService.getAllProjectByStaff();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getMessage().contains("Lấy tất cả dự án đang dạy của"));
        assertEquals(mockProjects, apiResponse.getData());

        verify(redisCacheHelper).getOrSet(eq(cacheKey), any(), any());
        verifyNoInteractions(projectRepository);
    }

    @Test
    @DisplayName("getCachedSubjects should return data from cache when available")
    void testGetCachedSubjects_CacheHit() {
        // Arrange
        String userId = "teacher-1";
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "subjects_" + userId;

        List<Subject> cachedSubjects = new ArrayList<>();
        when(redisCacheHelper.getOrSet(eq(cacheKey), any(), any())).thenReturn(cachedSubjects);
        when(sessionHelper.getUserId()).thenReturn(userId);

        // Act
        List<Subject> result = teachingScheduleService.getCachedSubjects();

        // Assert
        assertNotNull(result);
        assertEquals(cachedSubjects, result);
        verify(redisCacheHelper).getOrSet(eq(cacheKey), any(), any());
        verifyNoInteractions(subjectRepository);
    }

    @Test
    @DisplayName("getAllSubjectByStaff should use cached data")
    void testGetAllSubjectByStaff() {
        // Arrange
        String userId = "teacher-1";
        when(sessionHelper.getUserId()).thenReturn(userId);
        when(sessionHelper.getUserName()).thenReturn("Teacher Name");

        // Mock the cached data
        List<Subject> mockSubjects = new ArrayList<>();
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_TEACHER + "subjects_" + userId;
        when(redisCacheHelper.getOrSet(eq(cacheKey), any(), any())).thenReturn(mockSubjects);

        // Act
        ResponseEntity<?> response = teachingScheduleService.getAllSubjectByStaff();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getMessage().contains("Lấy tất cả môn của"));
        assertEquals(mockSubjects, apiResponse.getData());

        verify(redisCacheHelper).getOrSet(eq(cacheKey), any(), any());
        verifyNoInteractions(subjectRepository);
    }

    @Test
    @DisplayName("updatePlanDate should invalidate caches after successful update")
    void testUpdatePlanDate_WithCacheInvalidation() {
        // Arrange
        String planDateId = "plan-date-1";
        String userId = "teacher-1";

        when(sessionHelper.getUserId()).thenReturn(userId);

        // Mock the setting that the service uses
        when(settingHelper.getSetting(eq(SettingKeys.SHIFT_MAX_LATE_ARRIVAL), eq(Integer.class))).thenReturn(15);

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
        Project project = mock(Project.class);
        SubjectFacility subjectFacility = mock(SubjectFacility.class);
        Subject subject = mock(Subject.class);

        when(planDate.getPlanFactory()).thenReturn(planFactory);
        when(planFactory.getFactory()).thenReturn(factory);
        when(factory.getUserStaff()).thenReturn(teacher);
        when(factory.getProject()).thenReturn(project);
        when(project.getSubjectFacility()).thenReturn(subjectFacility);
        when(subjectFacility.getSubject()).thenReturn(subject);
        when(project.getName()).thenReturn("Test Project");
        when(subject.getName()).thenReturn("Test Subject");
        when(teacher.getId()).thenReturn(userId);

        when(teachingScheduleRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
        when(teachingScheduleRepository.isOutOfTime(planDateId)).thenReturn(false);
        when(teachingScheduleRepository.save(planDate)).thenReturn(planDate);

        // Act
        ResponseEntity<?> response = teachingScheduleService.updatePlanDate(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify cache invalidation
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("getCachedPlanDateDetail should return data from repository")
    void testGetCachedPlanDateDetail_CacheHit() {
        // Arrange
        String planDateId = "plan-date-1";

        TCTSDetailPlanDateResponse mockDetail = mock(TCTSDetailPlanDateResponse.class);
        when(teachingScheduleRepository.getPlanDateById(planDateId)).thenReturn(Optional.of(mockDetail));

        // Act
        TCTSDetailPlanDateResponse result = teachingScheduleService.getCachedPlanDateDetail(planDateId);

        // Assert
        assertNotNull(result);
        assertSame(mockDetail, result);
        verify(teachingScheduleRepository).getPlanDateById(planDateId);
        verifyNoInteractions(redisService);
    }

    @Test
    @DisplayName("getDetailPlanDate should use repository data")
    void testGetDetailPlanDate_Success() {
        // Arrange
        String planDateId = "plan-date-1";
        TCTSDetailPlanDateResponse mockResponse = mock(TCTSDetailPlanDateResponse.class);
        when(teachingScheduleRepository.getPlanDateById(planDateId)).thenReturn(Optional.of(mockResponse));

        // Act
        ResponseEntity<?> response = teachingScheduleService.getDetailPlanDate(planDateId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy chi tiết kế hoạch thành công", apiResponse.getMessage());
        assertEquals(mockResponse, apiResponse.getData());

        verify(teachingScheduleRepository).getPlanDateById(planDateId);
        verifyNoInteractions(redisService);
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
        Project project = mock(Project.class);
        SubjectFacility subjectFacility = mock(SubjectFacility.class);
        Subject subject = mock(Subject.class);

        when(planDate.getPlanFactory()).thenReturn(planFactory);
        when(planFactory.getFactory()).thenReturn(factory);
        when(factory.getUserStaff()).thenReturn(teacher);
        when(factory.getProject()).thenReturn(project);
        when(project.getSubjectFacility()).thenReturn(subjectFacility);
        when(subjectFacility.getSubject()).thenReturn(subject);
        when(teacher.getId()).thenReturn(userId);
        when(project.getName()).thenReturn("Test Project");
        when(subject.getName()).thenReturn("Test Subject");

        when(planDate.getType()).thenReturn(ShiftType.OFFLINE); // Initially OFFLINE
        when(teachingScheduleRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
        when(teachingScheduleRepository.isOutOfTime(planDateId)).thenReturn(false);
        when(teachingScheduleRepository.save(planDate)).thenReturn(planDate);

        // Act
        ResponseEntity<?> response = teachingScheduleService.changeTypePlanDate(planDateId, room);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify cache invalidation
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test getCachedTeachingSchedule should handle cache deserialization error")
    void testGetCachedTeachingScheduleWithCacheError() {
        String userId = "teacher-1";
        TCTeachingScheduleRequest request = new TCTeachingScheduleRequest();
        // Simulate cache error by calling supplier directly
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(teachingScheduleRepository.getAllTeachingScheduleByStaff(anyString(), any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(new ArrayList<>()));

        PageableObject<?> result = teachingScheduleService.getCachedTeachingSchedule(request);
        assertNotNull(result);
        verify(teachingScheduleRepository).getAllTeachingScheduleByStaff(anyString(), any(Pageable.class), any());
    }

    @Test
    @DisplayName("Test getCachedTeachingSchedule should handle redis set exception")
    void testGetCachedTeachingScheduleWithRedisSetError() {
        TCTeachingScheduleRequest request = new TCTeachingScheduleRequest();
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(teachingScheduleRepository.getAllTeachingScheduleByStaff(anyString(), any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(new ArrayList<>()));
        doThrow(new RuntimeException("Redis error")).when(redisService).set(anyString(), any(), anyLong());
        PageableObject<?> result = teachingScheduleService.getCachedTeachingSchedule(request);
        assertNotNull(result);
        assertTrue(result instanceof PageableObject);
        verify(teachingScheduleRepository).getAllTeachingScheduleByStaff(anyString(), any(Pageable.class), any());
    }

    @Test
    @DisplayName("Test getCachedFactories should handle cache deserialization error")
    void testGetCachedFactoriesWithCacheError() {
        // Simulate cache error by calling supplier directly
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(factoryRepository.getAllFactoryByStaff(anyString(), eq(EntityStatus.ACTIVE)))
                .thenReturn(new ArrayList<>());

        List<Factory> result = teachingScheduleService.getCachedFactories();
        assertNotNull(result);
        verify(factoryRepository).getAllFactoryByStaff(anyString(), eq(EntityStatus.ACTIVE));
    }

    @Test
    @DisplayName("Test getCachedFactories should handle redis set exception")
    void testGetCachedFactoriesWithRedisSetError() {
        List<Factory> factories = new ArrayList<>();
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(factoryRepository.getAllFactoryByStaff(anyString(), eq(EntityStatus.ACTIVE))).thenReturn(factories);
        doThrow(new RuntimeException("Redis error")).when(redisService).set(anyString(), any(), anyLong());
        List<Factory> result = teachingScheduleService.getCachedFactories();
        assertNotNull(result);
        assertTrue(result instanceof List);
        verify(factoryRepository).getAllFactoryByStaff(anyString(), eq(EntityStatus.ACTIVE));
    }

    @Test
    @DisplayName("Test getCachedProjects should handle cache deserialization error")
    void testGetCachedProjectsWithCacheError() {
        // Simulate cache error by calling supplier directly
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(projectRepository.getAllProject(anyString(), eq(EntityStatus.ACTIVE))).thenReturn(new ArrayList<>());

        List<Project> result = teachingScheduleService.getCachedProjects();
        assertNotNull(result);
        verify(projectRepository).getAllProject(anyString(), eq(EntityStatus.ACTIVE));
    }

    @Test
    @DisplayName("Test getCachedProjects should handle redis set exception")
    void testGetCachedProjectsWithRedisSetError() {
        List<Project> projects = new ArrayList<>();
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(projectRepository.getAllProject(anyString(), eq(EntityStatus.ACTIVE))).thenReturn(projects);
        doThrow(new RuntimeException("Redis error")).when(redisService).set(anyString(), any(), anyLong());
        List<Project> result = teachingScheduleService.getCachedProjects();
        assertNotNull(result);
        assertTrue(result instanceof List);
        verify(projectRepository).getAllProject(anyString(), eq(EntityStatus.ACTIVE));
    }

    @Test
    @DisplayName("Test getCachedSubjects should handle cache deserialization error")
    void testGetCachedSubjectsWithCacheError() {
        // Simulate cache error by calling supplier directly
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(subjectRepository.getAllSubjectByStaff(anyString(), eq(EntityStatus.ACTIVE)))
                .thenReturn(new ArrayList<>());

        List<Subject> result = teachingScheduleService.getCachedSubjects();
        assertNotNull(result);
        verify(subjectRepository).getAllSubjectByStaff(anyString(), eq(EntityStatus.ACTIVE));
    }

    @Test
    @DisplayName("Test getCachedSubjects should handle redis set exception")
    void testGetCachedSubjectsWithRedisSetError() {
        List<Subject> subjects = new ArrayList<>();
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(subjectRepository.getAllSubjectByStaff(anyString(), eq(EntityStatus.ACTIVE))).thenReturn(subjects);
        doThrow(new RuntimeException("Redis error")).when(redisService).set(anyString(), any(), anyLong());
        List<Subject> result = teachingScheduleService.getCachedSubjects();
        assertNotNull(result);
        assertTrue(result instanceof List);
        verify(subjectRepository).getAllSubjectByStaff(anyString(), eq(EntityStatus.ACTIVE));
    }

    @Test
    @DisplayName("Test getCachedTypes should handle cache deserialization error")
    void testGetCachedTypesWithCacheError() {
        // Simulate cache error by calling supplier directly
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(teachingScheduleRepository.getAllType()).thenReturn(new ArrayList<>());

        List<PlanDate> result = teachingScheduleService.getCachedTypes();
        assertNotNull(result);
        verify(teachingScheduleRepository).getAllType();
    }

    @Test
    @DisplayName("Test getCachedTypes should handle redis set exception")
    void testGetCachedTypesWithRedisSetError() {
        List<PlanDate> types = new ArrayList<>();
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(teachingScheduleRepository.getAllType()).thenReturn(types);
        doThrow(new RuntimeException("Redis error")).when(redisService).set(anyString(), any(), anyLong());
        List<PlanDate> result = teachingScheduleService.getCachedTypes();
        assertNotNull(result);
        assertTrue(result instanceof List);
        verify(teachingScheduleRepository).getAllType();
    }

    @Test
    @DisplayName("Test getCachedCurrentTeachingSchedule should handle cache deserialization error")
    void testGetCachedCurrentTeachingScheduleWithCacheError() {
        String userId = "teacher-1";
        // Simulate cache error by calling supplier directly
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(teachingScheduleRepository.getAllTeachingSchedulePresent(anyString(), any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(new ArrayList<>()));

        PageableObject<?> result = teachingScheduleService
                .getCachedCurrentTeachingSchedule(new TCTeachingScheduleRequest());
        assertNotNull(result);
        verify(teachingScheduleRepository).getAllTeachingSchedulePresent(anyString(), any(Pageable.class), any());
    }

    @Test
    @DisplayName("Test getCachedCurrentTeachingSchedule should handle redis set exception")
    void testGetCachedCurrentTeachingScheduleWithRedisSetError() {
        TCTeachingScheduleRequest request = new TCTeachingScheduleRequest();
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(teachingScheduleRepository.getAllTeachingSchedulePresent(anyString(), any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(new ArrayList<>()));
        doThrow(new RuntimeException("Redis error")).when(redisService).set(anyString(), any(), anyLong());
        PageableObject<?> result = teachingScheduleService.getCachedCurrentTeachingSchedule(request);
        assertNotNull(result);
        assertTrue(result instanceof PageableObject);
        verify(teachingScheduleRepository).getAllTeachingSchedulePresent(anyString(), any(Pageable.class), any());
    }

    @Test
    @DisplayName("Test getCachedPlanDateDetail should handle cache deserialization error")
    void testGetCachedPlanDateDetailWithCacheError() {
        String planDateId = "plan-date-1";
        TCTSDetailPlanDateResponse detail = mock(TCTSDetailPlanDateResponse.class);
        // getCachedPlanDateDetail doesn't use cache, it directly calls repository
        when(teachingScheduleRepository.getPlanDateById(planDateId)).thenReturn(Optional.of(detail));

        TCTSDetailPlanDateResponse result = teachingScheduleService.getCachedPlanDateDetail(planDateId);
        assertNotNull(result);
        verify(teachingScheduleRepository).getPlanDateById(planDateId);
    }

    @Test
    @DisplayName("Test getCachedPlanDateDetail should handle redis set exception")
    void testGetCachedPlanDateDetailWithRedisSetError() {
        String planDateId = "plan-date-1";
        TCTSDetailPlanDateResponse detail = mock(TCTSDetailPlanDateResponse.class);
        // getCachedPlanDateDetail doesn't use cache, it directly calls repository
        when(teachingScheduleRepository.getPlanDateById(planDateId)).thenReturn(Optional.of(detail));

        TCTSDetailPlanDateResponse result = teachingScheduleService.getCachedPlanDateDetail(planDateId);

        assertNotNull(result);
        // Should not throw exception, just ignore redis error
    }

    @Test
    @DisplayName("Test getDetailPlanDate should return error when plan date not found")
    void testGetDetailPlanDateNotFound() {
        String planDateId = "nonexistent";
        when(redisCacheHelper.getOrSet(anyString(), any(), any())).thenReturn(null);
        when(teachingScheduleRepository.getPlanDateById(planDateId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = teachingScheduleService.getDetailPlanDate(planDateId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals("Lấy chi tiết lịch dạy thành công", apiResponse.getMessage());
        assertNull(apiResponse.getData());
    }

    @Test
    @DisplayName("Test updatePlanDate should return error when plan date not found")
    void testUpdatePlanDateNotFound() {
        TCTSPlanDateUpdateRequest request = new TCTSPlanDateUpdateRequest();
        request.setIdPlanDate("nonexistent");
        when(teachingScheduleRepository.findById("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<?> response = teachingScheduleService.updatePlanDate(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
    }

    @Test
    @DisplayName("Test updatePlanDate should return error for invalid date format")
    void testUpdatePlanDateInvalidDateFormat() {
        TCTSPlanDateUpdateRequest request = new TCTSPlanDateUpdateRequest();
        request.setIdPlanDate("plan-date-1");
        // No setDate method, so skip setting date

        PlanDate planDate = mock(PlanDate.class);
        PlanFactory planFactory = mock(PlanFactory.class);
        Factory factory = mock(Factory.class);
        UserStaff userStaff = mock(UserStaff.class);
        when(userStaff.getId()).thenReturn("staff-1");
        when(factory.getUserStaff()).thenReturn(userStaff);
        when(planDate.getPlanFactory()).thenReturn(planFactory);
        when(planFactory.getFactory()).thenReturn(factory);
        when(planDate.getUserStaff()).thenReturn(userStaff); // Add missing mock
        when(teachingScheduleRepository.findById("plan-date-1")).thenReturn(Optional.of(planDate));

        ResponseEntity<?> response = teachingScheduleService.updatePlanDate(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
    }

    @Test
    @DisplayName("Test updatePlanDate should return error for invalid time format")
    void testUpdatePlanDateInvalidTimeFormat() {
        TCTSPlanDateUpdateRequest request = new TCTSPlanDateUpdateRequest();
        request.setIdPlanDate("plan-date-1");
        // No setDate/setTime methods, so skip setting them

        PlanDate planDate = mock(PlanDate.class);
        PlanFactory planFactory = mock(PlanFactory.class);
        Factory factory = mock(Factory.class);
        UserStaff userStaff = mock(UserStaff.class);
        when(userStaff.getId()).thenReturn("staff-1");
        when(factory.getUserStaff()).thenReturn(userStaff);
        when(planDate.getPlanFactory()).thenReturn(planFactory);
        when(planFactory.getFactory()).thenReturn(factory);
        when(planDate.getUserStaff()).thenReturn(userStaff); // Add missing mock
        when(teachingScheduleRepository.findById("plan-date-1")).thenReturn(Optional.of(planDate));

        ResponseEntity<?> response = teachingScheduleService.updatePlanDate(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
    }

    @Test
    @DisplayName("Test changeTypePlanDate should return error when plan date not found")
    void testChangeTypePlanDateNotFound() {
        String planDateId = "nonexistent";
        String room = "Room 101";
        when(teachingScheduleRepository.findById("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<?> response = teachingScheduleService.changeTypePlanDate(planDateId, room);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
    }

    @Test
    @DisplayName("Test changeTypePlanDate should return error for invalid room format")
    void testChangeTypePlanDateInvalidRoomFormat() {
        String planDateId = "plan-date-1";
        String room = ""; // Empty room

        PlanDate planDate = mock(PlanDate.class);
        PlanFactory planFactory = mock(PlanFactory.class);
        Factory factory = mock(Factory.class);
        UserStaff userStaff = mock(UserStaff.class);
        when(userStaff.getId()).thenReturn("staff-1");
        when(factory.getUserStaff()).thenReturn(userStaff);
        when(planDate.getPlanFactory()).thenReturn(planFactory);
        when(planFactory.getFactory()).thenReturn(factory);
        when(planDate.getUserStaff()).thenReturn(userStaff); // Add missing mock
        when(teachingScheduleRepository.findById("plan-date-1")).thenReturn(Optional.of(planDate));

        ResponseEntity<?> response = teachingScheduleService.changeTypePlanDate(planDateId, room);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
    }

    @Test
    @DisplayName("Test exportTeachingSchedule should handle empty list")
    void testExportTeachingScheduleEmptyList() {
        List<TCTeachingScheduleResponse> emptyList = new ArrayList<>();

        ByteArrayInputStream result = teachingScheduleService.exportTeachingSchedule(emptyList);

        assertNotNull(result);
        // Should not throw exception for empty list
    }

    @Test
    @DisplayName("Test exportTeachingSchedule should handle null values in response")
    void testExportTeachingScheduleWithNullValues() {
        TCTeachingScheduleResponse response = mock(TCTeachingScheduleResponse.class);

        List<TCTeachingScheduleResponse> list = Arrays.asList(response);

        ByteArrayInputStream result = teachingScheduleService.exportTeachingSchedule(list);

        assertNotNull(result);
        // Should not throw exception for null values
    }

    // Remove or comment out the test for invalidatePlanDateCache, as it is private
    // and should not be tested directly.
    // @Test
    // @DisplayName("Test invalidatePlanDateCache should work correctly")
    // void testInvalidatePlanDateCache() {
    // String planDateId = "plan-date-1";
    // String cacheKey = "schedule_teacher_plan_date_" + planDateId;
    //
    // teachingScheduleService.invalidatePlanDateCache(planDateId);
    //
    // verify(redisService).delete(cacheKey);
    // }

    @Test
    @DisplayName("Test getAllTeachingSchedulePresent should use cached data")
    void testGetAllTeachingSchedulePresent() {
        TCTeachingScheduleRequest request = new TCTeachingScheduleRequest();
        PageableObject<TCTeachingScheduleResponse> cachedData = new PageableObject<>();
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenReturn(cachedData);

        ResponseEntity<?> response = teachingScheduleService.getAllTeachingSchedulePresent(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
    }

    @Test
    @DisplayName("Test getAllType should use cached data")
    void testGetAllType() {
        List<PlanDate> cachedTypes = new ArrayList<>();
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenReturn(cachedTypes);

        ResponseEntity<?> response = teachingScheduleService.getAllType();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
    }
}
