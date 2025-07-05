package udpm.hn.studentattendance.core.teacher.studentattendance.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.core.teacher.studentattendance.model.request.TeacherModifyStudentAttendanceRequest;
import udpm.hn.studentattendance.core.teacher.studentattendance.model.request.TeacherStudentAttendanceRequest;
import udpm.hn.studentattendance.core.teacher.studentattendance.model.response.TeacherStudentAttendanceResponse;
import udpm.hn.studentattendance.core.teacher.studentattendance.repository.TeacherStudentAttendanceRepository;
import udpm.hn.studentattendance.entities.Attendance;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.entities.PlanFactory;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;
import udpm.hn.studentattendance.repositories.PlanDateRepository;
import udpm.hn.studentattendance.repositories.UserStudentRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TeacherStudentAttendanceServiceImplTest {

    @Mock
    private TeacherStudentAttendanceRepository attendanceRepository;

    @Mock
    private UserStudentRepository userStudentRepository;

    @Mock
    private PlanDateRepository planDateRepository;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private RedisService redisService;

    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @InjectMocks
    private TeacherStudentAttendanceServiceImpl attendanceService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(attendanceService, "redisTTL", 3600L);
    }

    @Test
    @DisplayName("createAttendance should create attendance entries for students")
    void testCreateAttendance_Success() {
        // Arrange
        String planDateId = "plan-date-1";
        List<String> studentIds = Arrays.asList("student-1", "student-2");

        when(attendanceRepository.getUserStudentIdsByPlanDate(planDateId)).thenReturn(studentIds);

        // Student 1 already has attendance record
        String existingAttendanceId = "attendance-1";
        when(attendanceRepository.findAttendanceIdByPlanDateAndStudent(planDateId, "student-1"))
                .thenReturn(Optional.of(existingAttendanceId));

        Attendance existingAttendance = new Attendance();
        existingAttendance.setId(existingAttendanceId);
        when(attendanceRepository.findById(existingAttendanceId)).thenReturn(Optional.of(existingAttendance));

        // Student 2 needs new attendance record
        when(attendanceRepository.findAttendanceIdByPlanDateAndStudent(planDateId, "student-2"))
                .thenReturn(Optional.empty());

        UserStudent student2 = mock(UserStudent.class);
        when(userStudentRepository.findById("student-2")).thenReturn(Optional.of(student2));

        PlanDate planDate = mock(PlanDate.class);
        when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));

        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<?> response = attendanceService.createAttendance(planDateId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Tạo điểm danh sinh viên thành công", apiResponse.getMessage());

        @SuppressWarnings("unchecked")
        List<Attendance> results = (List<Attendance>) apiResponse.getData();
        assertEquals(2, results.size());

        // Verify both entries have PRESENT status
        assertEquals(AttendanceStatus.PRESENT, results.get(0).getAttendanceStatus());
        assertEquals(AttendanceStatus.PRESENT, results.get(1).getAttendanceStatus());

        // Verify repository calls
        verify(attendanceRepository).getUserStudentIdsByPlanDate(planDateId);
        verify(attendanceRepository, times(2)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("createAttendance should return error when no students found")
    void testCreateAttendance_NoStudents() {
        // Arrange
        String planDateId = "plan-date-1";
        when(attendanceRepository.getUserStudentIdsByPlanDate(planDateId)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<?> response = attendanceService.createAttendance(planDateId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không tìm thấy sinh viên cho ngày điểm danh", apiResponse.getMessage());

        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    @DisplayName("getAllByPlanDate should return list of student attendances")
    void testGetAllByPlanDate() {
        // Arrange
        String planDateId = "plan-date-1";
        String facilityId = "facility-1";

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        List<TeacherStudentAttendanceResponse> mockResponses = Arrays.asList(
                mock(TeacherStudentAttendanceResponse.class),
                mock(TeacherStudentAttendanceResponse.class));

        when(attendanceRepository.getAllByPlanDate(planDateId, facilityId)).thenReturn(mockResponses);

        // Act
        ResponseEntity<?> response = attendanceService.getAllByPlanDate(planDateId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy tất cả sinh viên nhóm xưởng", apiResponse.getMessage());
        assertEquals(mockResponses, apiResponse.getData());

        verify(attendanceRepository).getAllByPlanDate(planDateId, facilityId);
    }

    @Test
    @DisplayName("updateStatusAttendance should update attendance status for students")
    void testUpdateStatusAttendance_Success() {
        // Arrange
        String userId = "teacher-1";
        when(sessionHelper.getUserId()).thenReturn(userId);

        TeacherModifyStudentAttendanceRequest request = new TeacherModifyStudentAttendanceRequest();
        List<TeacherStudentAttendanceRequest> students = new ArrayList<>();

        // Student 1 with existing attendance
        TeacherStudentAttendanceRequest student1Request = new TeacherStudentAttendanceRequest();
        student1Request.setIdAttendance("attendance-1");
        student1Request.setIdUserStudent("student-1");
        student1Request.setIdPlanDate("plan-date-1");
        student1Request.setStatus(AttendanceStatus.PRESENT.ordinal());
        students.add(student1Request);

        // Student 2 with no existing attendance
        TeacherStudentAttendanceRequest student2Request = new TeacherStudentAttendanceRequest();
        student2Request.setIdUserStudent("student-2");
        student2Request.setIdPlanDate("plan-date-1");
        student2Request.setStatus(AttendanceStatus.PRESENT.ordinal());
        students.add(student2Request);

        request.setStudents(students);

        // Setup plan date
        PlanDate planDate = mock(PlanDate.class);
        PlanFactory planFactory = mock(PlanFactory.class);
        Factory factory = mock(Factory.class);
        UserStaff teacher = mock(UserStaff.class);

        when(planDate.getPlanFactory()).thenReturn(planFactory);
        when(planFactory.getFactory()).thenReturn(factory);
        when(factory.getUserStaff()).thenReturn(teacher);
        when(teacher.getId()).thenReturn(userId);

        when(planDateRepository.findById("plan-date-1")).thenReturn(Optional.of(planDate));

        // Setup students
        UserStudent student1 = mock(UserStudent.class);
        when(userStudentRepository.findById("student-1")).thenReturn(Optional.of(student1));

        UserStudent student2 = mock(UserStudent.class);
        when(userStudentRepository.findById("student-2")).thenReturn(Optional.of(student2));

        // Setup existing attendance
        Attendance attendance1 = new Attendance();
        attendance1.setId("attendance-1");
        when(attendanceRepository.findById("attendance-1")).thenReturn(Optional.of(attendance1));

        when(attendanceRepository.saveAllAndFlush(anyList())).thenReturn(Arrays.asList(attendance1));

        // Act
        ResponseEntity<?> response = attendanceService.updateStatusAttendance(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Cập nhật trạng thái điểm danh thành công", apiResponse.getMessage());

        verify(attendanceRepository).saveAllAndFlush(anyList());
    }

    @Test
    @DisplayName("updateStatusAttendance should return error when no changes made")
    void testUpdateStatusAttendance_NoChanges() {
        // Arrange
        TeacherModifyStudentAttendanceRequest request = new TeacherModifyStudentAttendanceRequest();
        List<TeacherStudentAttendanceRequest> students = new ArrayList<>();

        // Invalid student request (missing IDs)
        TeacherStudentAttendanceRequest invalidRequest = new TeacherStudentAttendanceRequest();
        students.add(invalidRequest);

        request.setStudents(students);

        // Act
        ResponseEntity<?> response = attendanceService.updateStatusAttendance(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không có thay đổi nào", apiResponse.getMessage());

        verify(attendanceRepository, never()).saveAllAndFlush(anyList());
    }
}