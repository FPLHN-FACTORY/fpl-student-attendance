package udpm.hn.studentattendance.core.staff.attendancerecovery.service.impl;

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
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STAttendanceRecoveryRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STCreateOrUpdateNewEventRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STStudentAttendanceRecoveryAddRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.repository.*;
import udpm.hn.studentattendance.entities.*;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogDetailResponse;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.response.STAttendanceRecoveryResponse;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @InjectMocks
    private STAttendanceRecoveryServiceImpl attendanceRecoveryService;

    @Test
    @DisplayName("getListAttendanceRecovery should return paginated list successfully")
    void testGetListAttendanceRecovery() {
        // Arrange
        STAttendanceRecoveryRequest request = new STAttendanceRecoveryRequest();
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        Page<STAttendanceRecoveryResponse> mockPage = new PageImpl<>(new ArrayList<>());
        when(attendanceRecoveryRepository.getListAttendanceRecovery(any(), eq(facilityId), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.getListAttendanceRecovery(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy danh sách sự kiện thành công", apiResponse.getMessage());

        verify(attendanceRecoveryRepository).getListAttendanceRecovery(any(), eq(facilityId), any(Pageable.class));
    }

    @Test
    @DisplayName("deleteAttendanceRecovery should delete event and related attendance records")
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
    @DisplayName("deleteAttendanceRecovery should return error when event not found")
    void testDeleteAttendanceRecovery_NotFound() {
        // Arrange
        String eventId = "nonexistent";
        when(attendanceRecoveryRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.deleteAttendanceRecovery(eventId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Sự kiện khôi phục điểm danh sinh viên không tồn tại", apiResponse.getMessage());

        verify(attendanceRepository, never()).deleteAll(any());
        verify(attendanceRecoveryRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("getAllSemester should return active semesters")
    void testGetAllSemester() {
        // Arrange
        List<Semester> semesters = Arrays.asList(mock(Semester.class), mock(Semester.class));
        when(semesterRepository.getAllSemester(EntityStatus.ACTIVE)).thenReturn(semesters);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.getAllSemester();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy tất cả học kỳ thành công", apiResponse.getMessage());
        assertEquals(semesters, apiResponse.getData());
    }

    @Test
    @DisplayName("createNewEventAttendanceRecovery should create new event successfully")
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
        assertEquals("Thêm sự kiện khôi phục điểm danh mới thành công", apiResponse.getMessage());
        assertEquals(savedEvent, apiResponse.getData());

        verify(attendanceRecoveryRepository).save(any(AttendanceRecovery.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thêm sự kiện khôi phục điểm danh mới"));
    }

    @Test
    @DisplayName("updateEventAttendanceRecovery should update event successfully")
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
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Cập nhật sự kiện khôi phục điểm danh thành công", apiResponse.getMessage());

        verify(attendanceRecoveryRepository).save(existingEvent);
        verify(userActivityLogHelper).saveLog(contains("vừa cập nhật sự kiện khôi phục điểm danh"));
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
    @DisplayName("importAttendanceRecoveryStudent should return error for invalid student code")
    void testImportAttendanceRecoveryStudent_InvalidStudent() {
        // Arrange
        STStudentAttendanceRecoveryAddRequest request = new STStudentAttendanceRecoveryAddRequest();
        request.setStudentCode("INVALID");
        request.setDay(System.currentTimeMillis());
        request.setAttendanceRecoveryId("recovery-1");

        when(studentRepository.getStudentByCode("INVALID", EntityStatus.ACTIVE)).thenReturn(null);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.importAttendanceRecoveryStudent(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không tìm thấy mã sinh viên INVALID", apiResponse.getMessage());

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("importAttendanceRecoveryStudent should return error for invalid request data")
    void testImportAttendanceRecoveryStudent_InvalidRequest() {
        // Arrange
        STStudentAttendanceRecoveryAddRequest request = new STStudentAttendanceRecoveryAddRequest();
        request.setStudentCode(null);
        request.setDay(null);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.importAttendanceRecoveryStudent(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Dữ liệu đầu vào không hợp lệ", apiResponse.getMessage());
    }

    @Test
    @DisplayName("isHasStudentAttendanceRecovery should check if event has students")
    void testIsHasStudentAttendanceRecovery() {
        // Arrange
        String eventId = "event-1";
        AttendanceRecovery event = mock(AttendanceRecovery.class);
        ImportLog importLog = mock(ImportLog.class);
        when(event.getImportLog()).thenReturn(importLog);
        when(attendanceRecoveryRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.isHasStudentAttendanceRecovery(eventId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Kiểm tra sự kiện có sinh viên thành công", apiResponse.getMessage());
        assertEquals(true, apiResponse.getData());
    }

    @Test
    @DisplayName("deleteAttendanceRecordByAttendanceRecovery should delete all related records")
    void testDeleteAttendanceRecordByAttendanceRecovery() {
        // Arrange
        String eventId = "event-1";
        AttendanceRecovery event = mock(AttendanceRecovery.class);
        ImportLog importLog = mock(ImportLog.class);
        when(importLog.getId()).thenReturn("import-log-1");
        when(event.getId()).thenReturn(eventId);
        when(event.getImportLog()).thenReturn(importLog);
        when(event.getName()).thenReturn("Event Name");
        when(attendanceRecoveryRepository.findById(eventId)).thenReturn(Optional.of(event));

        List<Attendance> attendanceList = Arrays.asList(mock(Attendance.class));
        when(attendanceRepository.findAllByAttendanceRecoveryId(eventId)).thenReturn(attendanceList);

        List<ImportLogDetail> importLogDetails = Arrays.asList(mock(ImportLogDetail.class));
        when(historyLogRepository.getAllByImportLog("import-log-1")).thenReturn(importLogDetails);

        when(historyLogRepository.findById(eventId)).thenReturn(Optional.of(importLog));

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.deleteAttendanceRecordByAttendanceRecovery(eventId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Xóa dữ liệu điểm danh thành công", apiResponse.getMessage());

        verify(attendanceRepository).deleteAll(attendanceList);
        verify(historyLogDetailRepository).deleteAll(importLogDetails);
        verify(userActivityLogHelper).saveLog(contains("đã xóa dữ liệu điểm danh của sự kiện khôi phục"));
    }

    @Test
    @DisplayName("getAllHistoryLogByEvent should return paginated history logs")
    void testGetAllHistoryLogByEvent() {
        // Arrange
        String importLogId = "import-1";
        EXDataRequest request = new EXDataRequest();
        String userId = "user-1";
        String facilityId = "facility-1";

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        Page<ExImportLogResponse> mockPage = new PageImpl<>(new ArrayList<>());
        when(historyLogRepository.getListHistory(any(Pageable.class), eq(6), eq(userId), eq(facilityId),
                eq(importLogId)))
                .thenReturn(mockPage);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.getAllHistoryLogByEvent(importLogId, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy tất cả log import excel khôi phục điểm danh thành công", apiResponse.getMessage());

        verify(historyLogRepository).getListHistory(any(Pageable.class), eq(6), eq(userId), eq(facilityId),
                eq(importLogId));
    }

    @Test
    @DisplayName("getAllHistoryLogDetailEvent should return detail log list")
    void testGetAllHistoryLogDetailEvent() {
        // Arrange
        String importLogId = "import-1";
        String userId = "user-1";
        String facilityId = "facility-1";

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        List<ExImportLogDetailResponse> logDetails = Arrays.asList(
                mock(ExImportLogDetailResponse.class),
                mock(ExImportLogDetailResponse.class));
        when(historyLogRepository.getAllList(importLogId, userId, facilityId)).thenReturn(logDetails);

        // Act
        ResponseEntity<?> response = attendanceRecoveryService.getAllHistoryLogDetailEvent(importLogId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy chi tiết lịch sử import khôi phục điểm danh thành công", apiResponse.getMessage());
        assertEquals(logDetails, apiResponse.getData());
    }

    @Test
    @DisplayName("getAllImportStudentSuccess should return count of successful imports")
    void testGetAllImportStudentSuccess() {
        // Arrange
        String importLogId = "import-1";
        String userId = "user-1";
        String facilityId = "facility-1";
        Integer type = 1;
        Integer expectedCount = 10;

        when(historyLogRepository.getAllLine(importLogId, userId, facilityId, type)).thenReturn(expectedCount);

        // Act
        Integer result = attendanceRecoveryService.getAllImportStudentSuccess(importLogId, userId, facilityId, type);

        // Assert
        assertEquals(expectedCount, result);
        verify(historyLogRepository).getAllLine(importLogId, userId, facilityId, type);
    }
}