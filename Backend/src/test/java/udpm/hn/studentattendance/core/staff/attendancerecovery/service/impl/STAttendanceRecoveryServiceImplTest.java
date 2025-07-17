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
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogDetailResponse;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.response.STAttendanceRecoveryResponse;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;

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

    @Test
    @DisplayName("importAttendanceRecoveryStudent should return error when request is null")
    void testImportAttendanceRecoveryStudent_NullRequest() {
        // Act
        ResponseEntity<?> response = attendanceRecoveryService.importAttendanceRecoveryStudent(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Dữ liệu đầu vào không hợp lệ", apiResponse.getMessage());
    }

    @Test
    @DisplayName("importAttendanceRecoveryStudent should return error when student not found")
    void testImportAttendanceRecoveryStudent_StudentNotFound() {
        // Arrange
        STStudentAttendanceRecoveryAddRequest request = new STStudentAttendanceRecoveryAddRequest();
        request.setStudentCode("ST001");
        request.setDay(System.currentTimeMillis());
        request.setAttendanceRecoveryId("recovery-1");

        when(studentRepository.getStudentByCode("ST001", EntityStatus.ACTIVE)).thenReturn(null);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.importAttendanceRecoveryStudent(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertTrue(apiResponse.getMessage().contains("Không tìm thấy mã sinh viên"));
    }

    @Test
    @DisplayName("importAttendanceRecoveryStudent should return error when student factory not found")
    void testImportAttendanceRecoveryStudent_StudentFactoryNotFound() {
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

        when(studentFactoryRepository.getUserStudentFactoryByUserId("student-1", EntityStatus.ACTIVE,
                EntityStatus.ACTIVE))
                .thenReturn(null);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.importAttendanceRecoveryStudent(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertTrue(apiResponse.getMessage().contains("chưa tham gia nhóm xưởng nào"));
    }

    @Test
    @DisplayName("importAttendanceRecoveryStudent should return error when plan factory not found")
    void testImportAttendanceRecoveryStudent_PlanFactoryNotFound() {
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

        when(planFactoryRepository.getPlanFactoryByFactoryId("factory-1", EntityStatus.ACTIVE, EntityStatus.ACTIVE))
                .thenReturn(null);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.importAttendanceRecoveryStudent(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertTrue(apiResponse.getMessage().contains("chưa có kế hoạch"));
    }

    @Test
    @DisplayName("importAttendanceRecoveryStudent should return error when no valid plan dates")
    void testImportAttendanceRecoveryStudent_NoValidPlanDates() {
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

        when(planDateRepository.getAllPlanDateByPlanFactoryId("plan-factory-1", EntityStatus.ACTIVE,
                EntityStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.importAttendanceRecoveryStudent(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertTrue(apiResponse.getMessage().contains("không có ca học nào"));
    }

    @Test
    @DisplayName("deleteAttendanceRecovery should return error when event not found")
    void testDeleteAttendanceRecovery_EventNotFound() {
        // Arrange
        String eventId = "event-1";
        when(attendanceRecoveryRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.deleteAttendanceRecovery(eventId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Sự kiện khôi phục điểm danh sinh viên không tồn tại", apiResponse.getMessage());
    }

    @Test
    @DisplayName("createNewEventAttendanceRecovery should return error when facility not found")
    void testCreateNewEventAttendanceRecovery_FacilityNotFound() {
        // Arrange
        String facilityId = "facility-1";
        STCreateOrUpdateNewEventRequest request = new STCreateOrUpdateNewEventRequest();
        request.setName("New Event");
        request.setDescription("Description");
        request.setDay(System.currentTimeMillis());

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(facilityRepository.findById(facilityId)).thenReturn(null);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.createNewEventAttendanceRecovery(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Cơ sở không tồn tại", apiResponse.getMessage());
    }

    @Test
    @DisplayName("getDetailEventAttendanceRecovery should return error when event not found")
    void testGetDetailEventAttendanceRecovery_EventNotFound() {
        // Arrange
        String eventId = "event-1";
        when(attendanceRecoveryRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.getDetailEventAttendanceRecovery(eventId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Sự Kiện khôi phục điểm danh không tồn tại", apiResponse.getMessage());
    }

    @Test
    @DisplayName("updateEventAttendanceRecovery should return error when event not found")
    void testUpdateEventAttendanceRecovery_EventNotFound() {
        // Arrange
        String eventId = "event-1";
        STCreateOrUpdateNewEventRequest request = new STCreateOrUpdateNewEventRequest();
        request.setName("Updated Event");
        request.setDescription("Updated Description");
        request.setDay(System.currentTimeMillis());

        when(attendanceRecoveryRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.updateEventAttendanceRecovery(request, eventId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Sự kiện khôi phục điểm danh không tồn tại", apiResponse.getMessage());
    }

    @Test
    @DisplayName("getCachedHistoryLogByEvent should return cached data")
    void testGetCachedHistoryLogByEvent() {
        // Arrange
        String idImportLog = "import-1";
        EXDataRequest request = new EXDataRequest();
        String userId = "user-1";
        String facilityId = "facility-1";

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        PageableObject<?> cachedData = new PageableObject<>();
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(cachedData);

        // Act
        PageableObject<?> result = attendanceRecoveryService.getCachedHistoryLogByEvent(idImportLog, request);

        // Assert
        assertNotNull(result);
        assertSame(cachedData, result);
        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
    }

    @Test
    @DisplayName("getAllHistoryLogByEvent should return history log")
    void testGetAllHistoryLogByEvent() {
        // Arrange
        String idImportLog = "import-1";
        EXDataRequest request = new EXDataRequest();

        PageableObject<?> cachedData = new PageableObject<>();
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(cachedData);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.getAllHistoryLogByEvent(idImportLog, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy tất cả log import excel khôi phục điểm danh thành công", apiResponse.getMessage());
    }

    @Test
    @DisplayName("getCachedHistoryLogDetailEvent should return log details")
    void testGetCachedHistoryLogDetailEvent() {
        // Arrange
        String idImportLog = "import-1";
        String userId = "user-1";
        String facilityId = "facility-1";

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        List<ExImportLogDetailResponse> logDetails = Arrays.asList(
                mock(ExImportLogDetailResponse.class),
                mock(ExImportLogDetailResponse.class));
        when(historyLogRepository.getAllList(idImportLog, userId, facilityId)).thenReturn(logDetails);

        // Act
        List<ExImportLogDetailResponse> result = attendanceRecoveryService.getCachedHistoryLogDetailEvent(idImportLog);

        // Assert
        assertNotNull(result);
        assertEquals(logDetails, result);
        verify(historyLogRepository).getAllList(idImportLog, userId, facilityId);
    }

    @Test
    @DisplayName("getAllHistoryLogDetailEvent should return log detail response")
    void testGetAllHistoryLogDetailEvent() {
        // Arrange
        String idImportLog = "import-1";
        List<ExImportLogDetailResponse> logDetails = Arrays.asList(
                mock(ExImportLogDetailResponse.class),
                mock(ExImportLogDetailResponse.class));
        when(historyLogRepository.getAllList(idImportLog, null, null)).thenReturn(logDetails);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.getAllHistoryLogDetailEvent(idImportLog);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy chi tiết lịch sử import khôi phục điểm danh thành công", apiResponse.getMessage());
    }

    @Test
    @DisplayName("getCachedImportStudentSuccess should return success count")
    void testGetCachedImportStudentSuccess() {
        // Arrange
        String idImportLog = "import-1";
        String userId = "user-1";
        String facilityId = "facility-1";
        Integer type = 1;

        Integer expectedCount = 5;
        when(historyLogRepository.getAllLine(idImportLog, userId, facilityId, type)).thenReturn(expectedCount);

        // Act
        Integer result = attendanceRecoveryService.getCachedImportStudentSuccess(idImportLog, userId, facilityId, type);

        // Assert
        assertEquals(expectedCount, result);
        verify(historyLogRepository).getAllLine(idImportLog, userId, facilityId, type);
    }

    @Test
    @DisplayName("getAllImportStudentSuccess should return success count")
    void testGetAllImportStudentSuccess() {
        // Arrange
        String idImportLog = "import-1";
        String userId = "user-1";
        String facilityId = "facility-1";
        Integer type = 1;

        Integer expectedCount = 5;
        when(historyLogRepository.getAllLine(idImportLog, userId, facilityId, type)).thenReturn(expectedCount);

        // Act
        Integer result = attendanceRecoveryService.getAllImportStudentSuccess(idImportLog, userId, facilityId, type);

        // Assert
        assertEquals(expectedCount, result);
    }

    @Test
    @DisplayName("getCachedHasStudentAttendanceRecovery should return true when has import log")
    void testGetCachedHasStudentAttendanceRecovery_HasImportLog() {
        // Arrange
        String idAttendanceRecovery = "recovery-1";
        AttendanceRecovery attendanceRecovery = mock(AttendanceRecovery.class);
        ImportLog importLog = mock(ImportLog.class);

        when(attendanceRecoveryRepository.findById(idAttendanceRecovery)).thenReturn(Optional.of(attendanceRecovery));
        when(attendanceRecovery.getImportLog()).thenReturn(importLog);

        // Act
        Boolean result = attendanceRecoveryService.getCachedHasStudentAttendanceRecovery(idAttendanceRecovery);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("getCachedHasStudentAttendanceRecovery should return false when no import log")
    void testGetCachedHasStudentAttendanceRecovery_NoImportLog() {
        // Arrange
        String idAttendanceRecovery = "recovery-1";
        AttendanceRecovery attendanceRecovery = mock(AttendanceRecovery.class);

        when(attendanceRecoveryRepository.findById(idAttendanceRecovery)).thenReturn(Optional.of(attendanceRecovery));
        when(attendanceRecovery.getImportLog()).thenReturn(null);

        // Act
        Boolean result = attendanceRecoveryService.getCachedHasStudentAttendanceRecovery(idAttendanceRecovery);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("getCachedHasStudentAttendanceRecovery should return false when event not found")
    void testGetCachedHasStudentAttendanceRecovery_EventNotFound() {
        // Arrange
        String idAttendanceRecovery = "recovery-1";
        when(attendanceRecoveryRepository.findById(idAttendanceRecovery)).thenReturn(Optional.empty());

        // Act
        Boolean result = attendanceRecoveryService.getCachedHasStudentAttendanceRecovery(idAttendanceRecovery);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isHasStudentAttendanceRecovery should return success when event found")
    void testIsHasStudentAttendanceRecovery_Success() {
        // Arrange
        String idAttendanceRecovery = "recovery-1";
        AttendanceRecovery attendanceRecovery = mock(AttendanceRecovery.class);
        ImportLog importLog = mock(ImportLog.class);

        when(attendanceRecoveryRepository.findById(idAttendanceRecovery)).thenReturn(Optional.of(attendanceRecovery));
        when(attendanceRecovery.getImportLog()).thenReturn(importLog);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.isHasStudentAttendanceRecovery(idAttendanceRecovery);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Kiểm tra sự kiện có sinh viên thành công", apiResponse.getMessage());
        assertTrue((Boolean) apiResponse.getData());
    }

    @Test
    @DisplayName("isHasStudentAttendanceRecovery should return error when event not found")
    void testIsHasStudentAttendanceRecovery_EventNotFound() {
        // Arrange
        String idAttendanceRecovery = "recovery-1";
        when(attendanceRecoveryRepository.findById(idAttendanceRecovery)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.isHasStudentAttendanceRecovery(idAttendanceRecovery);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không tìm thấy sự kiện", apiResponse.getMessage());
    }

    @Test
    @DisplayName("deleteAttendanceRecordByAttendanceRecovery should delete records successfully")
    void testDeleteAttendanceRecordByAttendanceRecovery_Success() {
        // Arrange
        String idAttendanceRecovery = "recovery-1";
        AttendanceRecovery attendanceRecovery = mock(AttendanceRecovery.class);
        ImportLog importLog = mock(ImportLog.class);

        when(attendanceRecoveryRepository.findById(idAttendanceRecovery)).thenReturn(Optional.of(attendanceRecovery));
        when(attendanceRecovery.getImportLog()).thenReturn(importLog);
        when(attendanceRecovery.getName()).thenReturn("Test Event");
        when(attendanceRecovery.getId()).thenReturn(idAttendanceRecovery);
        when(importLog.getId()).thenReturn("import-1");

        List<Attendance> attendanceList = Arrays.asList(mock(Attendance.class), mock(Attendance.class));
        when(attendanceRepository.findAllByAttendanceRecoveryId(idAttendanceRecovery)).thenReturn(attendanceList);

        List<ImportLogDetail> importLogDetailList = Arrays.asList(mock(ImportLogDetail.class),
                mock(ImportLogDetail.class));
        when(historyLogRepository.getAllByImportLog("import-1")).thenReturn(importLogDetailList);

        when(historyLogRepository.findById(idAttendanceRecovery)).thenReturn(Optional.of(importLog));

        // Act
        ResponseEntity<?> response = attendanceRecoveryService
                .deleteAttendanceRecordByAttendanceRecovery(idAttendanceRecovery);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Xóa dữ liệu điểm danh thành công", apiResponse.getMessage());

        verify(attendanceRepository).deleteAll(attendanceList);
        verify(historyLogDetailRepository).deleteAll(importLogDetailList);
        verify(historyLogRepository).deleteById("import-1");
        verify(attendanceRecoveryRepository).save(attendanceRecovery);
    }

    @Test
    @DisplayName("deleteAttendanceRecordByAttendanceRecovery should return error when event not found")
    void testDeleteAttendanceRecordByAttendanceRecovery_EventNotFound() {
        // Arrange
        String idAttendanceRecovery = "recovery-1";
        when(attendanceRecoveryRepository.findById(idAttendanceRecovery)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = attendanceRecoveryService
                .deleteAttendanceRecordByAttendanceRecovery(idAttendanceRecovery);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không tìm thấy sự kiện khôi phục điểm danh", apiResponse.getMessage());
    }
}