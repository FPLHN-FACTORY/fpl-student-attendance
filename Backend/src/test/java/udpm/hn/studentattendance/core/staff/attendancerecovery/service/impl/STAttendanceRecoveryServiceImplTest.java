package udpm.hn.studentattendance.core.staff.attendancerecovery.service.impl;

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
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STAttendanceRecoveryRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STCreateOrUpdateNewEventRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STStudentAttendanceRecoveryAddRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.repository.*;
import udpm.hn.studentattendance.entities.*;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogDetailResponse;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.response.STAttendanceRecoveryResponse;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogResponse;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class STAttendanceRecoveryServiceImplTest {

    @Mock
    private STAttendanceRecoveryRepository attendanceRecoveryRepository;

    @Mock
    private STAttendanceRecoverySemesterRepository semesterRepository;

    @Mock
    private STAttendanceRecoveryFacilityRepository facilityRepository;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private STAttendanceRecoveryStudentRepository studentRepository;

    @Mock
    private STAttendanceRecoveryStudentFactoryRepository studentFactoryRepository;

    @Mock
    private STAttendanceRecoveryPlanFactoryRepository planFactoryRepository;

    @Mock
    private STAttendanceRecoveryPlanDateRepository planDateRepository;

    @Mock
    private STAttendanceRevoveryAttendanceRepository attendanceRepository;

    @Mock
    private UserActivityLogHelper userActivityLogHelper;

    @Mock
    private STAttendanceRecoveryHistoryLogRepository historyLogRepository;

    @Mock
    private STAttendanceRecoveryHistoryLogDetailRepository historyLogDetailRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @Mock
    private RedisCacheHelper redisCacheHelper;

    @InjectMocks
    private STAttendanceRecoveryServiceImpl attendanceRecoveryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(attendanceRecoveryService, "redisTTL", 3600L);
        // Removed unnecessary stubbing for redisCacheHelper.getOrSet
    }

    @Test
    @DisplayName("getCachedAttendanceRecoveryList should return data from cache when available")
    void testGetCachedAttendanceRecoveryList_CacheHit() {
        // Arrange
        STAttendanceRecoveryRequest request = new STAttendanceRecoveryRequest();
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_ATTENDANCE_RECOVERY + "list_" +
                facilityId + "_" + request.toString();

        PageableObject<?> cachedData = new PageableObject<>();
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(cachedData);

        // Act
        PageableObject<?> result = attendanceRecoveryService.getCachedAttendanceRecoveryList(request);

        // Assert
        assertNotNull(result);
        assertSame(cachedData, result);
        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        verifyNoInteractions(attendanceRecoveryRepository);
    }

    @Test
    @DisplayName("getCachedAttendanceRecoveryList should fetch from repository when not in cache")
    void testGetCachedAttendanceRecoveryList_CacheMiss() {
        // Arrange
        STAttendanceRecoveryRequest request = new STAttendanceRecoveryRequest();
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_ATTENDANCE_RECOVERY + "list_" +
                facilityId + "_" + request.toString();

        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });

        Page<STAttendanceRecoveryResponse> page = new PageImpl<>(new ArrayList<>());
        when(attendanceRecoveryRepository.getListAttendanceRecovery(any(), eq(facilityId), any(Pageable.class)))
                .thenReturn(page);

        // Act
        PageableObject<?> result = attendanceRecoveryService.getCachedAttendanceRecoveryList(request);

        // Assert
        assertNotNull(result);
        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        verify(attendanceRecoveryRepository).getListAttendanceRecovery(any(), eq(facilityId), any(Pageable.class));
    }

    @Test
    @DisplayName("getListAttendanceRecovery should return cached data")
    void testGetListAttendanceRecovery() {
        // Arrange
        STAttendanceRecoveryRequest request = new STAttendanceRecoveryRequest();
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        // Mock the repository to return data
        Page<STAttendanceRecoveryResponse> page = new PageImpl<>(new ArrayList<>());
        when(attendanceRecoveryRepository.getListAttendanceRecovery(any(), eq(facilityId), any(Pageable.class)))
                .thenReturn(page);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.getListAttendanceRecovery(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy danh sách sự kiện thành công", apiResponse.getMessage());
    }

    @Test
    @DisplayName("deleteAttendanceRecovery should delete event and attendances")
    void testDeleteAttendanceRecovery_Success() {
        // Arrange
        String eventId = "event-1";
        AttendanceRecovery event = mock(AttendanceRecovery.class);
        when(event.getId()).thenReturn(eventId);
        when(event.getName()).thenReturn("Event Name");
        when(attendanceRecoveryRepository.findById(eventId)).thenReturn(Optional.of(event));

        List<Attendance> attendanceList = Arrays.asList(mock(Attendance.class), mock(Attendance.class));
        when(attendanceRepository.findAllByAttendanceRecoveryId(eventId)).thenReturn(attendanceList);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.deleteAttendanceRecovery(eventId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Xóa sự kiện khôi phục điểm danh sinh viên thành công", apiResponse.getMessage());

        verify(attendanceRepository).deleteAll(attendanceList);
        verify(attendanceRecoveryRepository).deleteById(eventId);
        verify(userActivityLogHelper).saveLog(contains("vừa xóa sự kiện khôi phục điểm danh"));
    }

    @Test
    @DisplayName("getCachedSemesters should return data from cache when available")
    void testGetCachedSemesters_CacheHit() {
        // Arrange
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_ATTENDANCE_RECOVERY + "semesters";
        List<Semester> cachedSemesters = Arrays.asList(mock(Semester.class), mock(Semester.class));

        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(cachedSemesters);

        // Act
        List<Semester> result = attendanceRecoveryService.getCachedSemesters();

        // Assert
        assertNotNull(result);
        assertSame(cachedSemesters, result);
        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        verifyNoInteractions(semesterRepository);
    }

    @Test
    @DisplayName("getAllSemester should return semester list")
    void testGetAllSemester() {
        // Arrange
        List<Semester> semesters = Arrays.asList(mock(Semester.class), mock(Semester.class));

        // Mock the method to return cached semesters
        when(semesterRepository.findAll()).thenReturn(semesters);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.getAllSemester();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy tất cả học kỳ thành công", apiResponse.getMessage());
    }

    @Test
    @DisplayName("createNewEventAttendanceRecovery should create new event")
    void testCreateNewEventAttendanceRecovery() {
        // Arrange
        String facilityId = "facility-1";
        STCreateOrUpdateNewEventRequest request = new STCreateOrUpdateNewEventRequest();
        request.setName("New Event");
        request.setDescription("Description");
        request.setDay(System.currentTimeMillis());

        Facility facility = mock(Facility.class);
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));

        AttendanceRecovery savedEvent = new AttendanceRecovery();
        savedEvent.setId("saved-id");
        savedEvent.setName(request.getName());
        when(attendanceRecoveryRepository.save(any(AttendanceRecovery.class))).thenReturn(savedEvent);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.createNewEventAttendanceRecovery(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());

        verify(attendanceRecoveryRepository).save(any(AttendanceRecovery.class));
    }

    @Test
    @DisplayName("getCachedAttendanceRecoveryDetail should return event if found")
    void testGetCachedAttendanceRecoveryDetail_CacheHit() {
        // Arrange
        String eventId = "event-1";
        AttendanceRecovery event = mock(AttendanceRecovery.class);
        when(attendanceRecoveryRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Act
        AttendanceRecovery result = attendanceRecoveryService.getCachedAttendanceRecoveryDetail(eventId);

        // Assert
        assertNotNull(result);
        assertSame(event, result);
        verify(attendanceRecoveryRepository).findById(eventId);
    }

    @Test
    @DisplayName("getDetailEventAttendanceRecovery should return event details")
    void testGetDetailEventAttendanceRecovery() {
        // Arrange
        String eventId = "event-1";
        AttendanceRecovery event = mock(AttendanceRecovery.class);

        when(attendanceRecoveryRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.getDetailEventAttendanceRecovery(eventId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy chi tiết sự kiện khôi phục điểm danh thành công", apiResponse.getMessage());
        assertEquals(event, apiResponse.getData());
    }

    @Test
    @DisplayName("updateEventAttendanceRecovery should update event")
    void testUpdateEventAttendanceRecovery() {
        // Arrange
        String eventId = "event-1";
        STCreateOrUpdateNewEventRequest request = new STCreateOrUpdateNewEventRequest();
        request.setName("Updated Event");
        request.setDescription("Updated Description");
        request.setDay(System.currentTimeMillis());

        AttendanceRecovery existingEvent = new AttendanceRecovery();
        existingEvent.setId(eventId);
        existingEvent.setName("Old Event");
        when(attendanceRecoveryRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(attendanceRecoveryRepository.save(any(AttendanceRecovery.class))).thenReturn(existingEvent);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.updateEventAttendanceRecovery(request, eventId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(attendanceRecoveryRepository).save(any(AttendanceRecovery.class));
    }

    @Test
    @DisplayName("importAttendanceRecoveryStudent should process student attendance recovery successfully")
    void testImportAttendanceRecoveryStudent_Success() {
        // Arrange
        STStudentAttendanceRecoveryAddRequest request = new STStudentAttendanceRecoveryAddRequest();
        request.setStudentCode("ST001");
        request.setDay(System.currentTimeMillis());
        request.setAttendanceRecoveryId("recovery-1");

        UserStudent student = mock(UserStudent.class);
        when(student.getId()).thenReturn("student-1");
        when(student.getCode()).thenReturn("ST001");
        when(student.getName()).thenReturn("Student Name");
        when(studentRepository.getStudentByCode("ST001", EntityStatus.ACTIVE)).thenReturn(student);

        UserStudentFactory studentFactory = mock(UserStudentFactory.class);
        Factory factory = mock(Factory.class);
        when(factory.getId()).thenReturn("factory-1");
        when(studentFactory.getFactory()).thenReturn(factory);
        when(studentFactoryRepository.getUserStudentFactoryByUserId("student-1", EntityStatus.ACTIVE,
                EntityStatus.ACTIVE))
                .thenReturn(studentFactory);

        PlanFactory planFactory = mock(PlanFactory.class);
        when(planFactory.getId()).thenReturn("plan-factory-1");
        when(planFactoryRepository.getPlanFactoryByFactoryId("factory-1", EntityStatus.ACTIVE, EntityStatus.ACTIVE))
                .thenReturn(planFactory);

        PlanDate planDate = mock(PlanDate.class);
        when(planDate.getId()).thenReturn("plan-date-1");
        when(planDate.getEndDate()).thenReturn(request.getDay());
        List<PlanDate> planDates = Collections.singletonList(planDate);
        when(planDateRepository.getAllPlanDateByPlanFactoryId("plan-factory-1", EntityStatus.ACTIVE,
                EntityStatus.ACTIVE))
                .thenReturn(planDates);

        AttendanceRecovery attendanceRecovery = mock(AttendanceRecovery.class);
        when(attendanceRecoveryRepository.findById("recovery-1")).thenReturn(Optional.of(attendanceRecovery));

        when(attendanceRepository.findByUserStudentIdAndPlanDateId("student-1", "plan-date-1")).thenReturn(null);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.importAttendanceRecoveryStudent(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertTrue(apiResponse.getMessage().contains("Khôi phục điểm danh thành công"));

        verify(attendanceRepository).save(any(Attendance.class));
    }
}