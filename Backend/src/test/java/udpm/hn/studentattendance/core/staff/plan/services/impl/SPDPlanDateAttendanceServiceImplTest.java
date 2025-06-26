package udpm.hn.studentattendance.core.staff.plan.services.impl;

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
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateAttendanceRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDModifyPlanDateAttendanceRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateAttendanceResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateStudentResponse;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDAttendanceRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanDateRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDUserStudentRepository;
import udpm.hn.studentattendance.core.student.schedule.repository.STDScheduleAttendanceRepository;
import udpm.hn.studentattendance.entities.Attendance;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;
import udpm.hn.studentattendance.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SPDPlanDateAttendanceServiceImplTest {

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private SPDAttendanceRepository attendanceRepository;

    @Mock
    private SPDPlanDateRepository planDateRepository;

    @Mock
    private SPDUserStudentRepository userStudentRepository;

    @Mock
    private STDScheduleAttendanceRepository scheduleAttendanceRepository;

    @Mock
    private UserActivityLogHelper userActivityLogHelper;

    @InjectMocks
    private SPDPlanDateAttendanceServiceImpl attendanceService;

    @Test
    @DisplayName("getDetail should return attendance details when found")
    void testGetDetail_Success() {
        // Arrange
        String planDateId = "plan-date-1";
        String facilityId = "facility-1";

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        SPDPlanDateAttendanceResponse mockResponse = mock(SPDPlanDateAttendanceResponse.class);
        when(attendanceRepository.getDetailPlanDate(planDateId, facilityId)).thenReturn(Optional.of(mockResponse));

        // Act
        ResponseEntity<?> response = attendanceService.getDetail(planDateId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Get dữ liệu thành công", apiResponse.getMessage());
        assertEquals(mockResponse, apiResponse.getData());

        verify(attendanceRepository).getDetailPlanDate(planDateId, facilityId);
    }

    @Test
    @DisplayName("getDetail should return error when attendance not found")
    void testGetDetail_NotFound() {
        // Arrange
        String planDateId = "non-existent-id";
        String facilityId = "facility-1";

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(attendanceRepository.getDetailPlanDate(planDateId, facilityId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = attendanceService.getDetail(planDateId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không tìm thấy kế hoạch", apiResponse.getMessage());
    }

    @Test
    @DisplayName("getAllList should return paginated attendance list")
    void testGetAllList() {
        // Arrange
        String facilityId = "facility-1";
        SPDFilterPlanDateAttendanceRequest request = new SPDFilterPlanDateAttendanceRequest();

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        Page<SPDPlanDateStudentResponse> page = new PageImpl<>(new ArrayList<>());
        when(attendanceRepository.getAllByFilter(any(Pageable.class), eq(request))).thenReturn(page);

        // Act
        ResponseEntity<?> response = attendanceService.getAllList(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());

        assertEquals(facilityId, request.getIdFacility());
        verify(attendanceRepository).getAllByFilter(any(Pageable.class), eq(request));
    }

    @Test
    @DisplayName("changeStatus should update attendance status to PRESENT when checkouts are not required")
    void testChangeStatus_PresentStatus() {
        // Arrange
        String planDateId = "plan-date-1";
        String userStudentId = "user-student-1";
        String studentName = "Test Student";

        SPDModifyPlanDateAttendanceRequest request = new SPDModifyPlanDateAttendanceRequest();
        request.setIdPlanDate(planDateId);
        request.setIdUserStudent(userStudentId);
        request.setStatus(0); // Checkin

        PlanDate planDate = mock(PlanDate.class);
        when(planDate.getId()).thenReturn(planDateId);
        when(planDate.getStartDate()).thenReturn(DateTimeUtils.getCurrentTimeMillis() - 3600000); // 1 hour ago
        when(planDate.getRequiredCheckin()).thenReturn(StatusType.ENABLE);
        when(planDate.getRequiredCheckout()).thenReturn(StatusType.DISABLE); // Checkout not required

        UserStudent userStudent = mock(UserStudent.class);
        when(userStudent.getId()).thenReturn(userStudentId);
        when(userStudent.getName()).thenReturn(studentName);
        when(userStudent.getStatus()).thenReturn(EntityStatus.ACTIVE);

        when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
        when(userStudentRepository.findById(userStudentId)).thenReturn(Optional.of(userStudent));

        // No existing attendance found
        when(attendanceRepository.findByPlanDate_IdAndUserStudent_Id(planDateId, userStudentId))
                .thenReturn(Optional.empty());

        Attendance savedAttendance = new Attendance();
        savedAttendance.setPlanDate(planDate);
        savedAttendance.setUserStudent(userStudent);
        savedAttendance.setAttendanceStatus(AttendanceStatus.PRESENT);

        when(attendanceRepository.save(any(Attendance.class))).thenReturn(savedAttendance);

        doNothing().when(userActivityLogHelper).saveLog(anyString());

        // Act
        ResponseEntity<?> response = attendanceService.changeStatus(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thay đổi trạng thái checkin thành công sinh viên " + studentName, apiResponse.getMessage());
        assertEquals(savedAttendance, apiResponse.getData());

        // Verify that attendance was saved with PRESENT status
        assertEquals(AttendanceStatus.PRESENT, savedAttendance.getAttendanceStatus());

        verify(attendanceRepository).save(any(Attendance.class));
        verify(userActivityLogHelper).saveLog(anyString());
    }

    @Test
    @DisplayName("changeStatus should update attendance status to CHECKIN when checkouts are required")
    void testChangeStatus_CheckinStatus() {
        // Arrange
        String planDateId = "plan-date-1";
        String userStudentId = "user-student-1";
        String studentName = "Test Student";

        SPDModifyPlanDateAttendanceRequest request = new SPDModifyPlanDateAttendanceRequest();
        request.setIdPlanDate(planDateId);
        request.setIdUserStudent(userStudentId);
        request.setStatus(0); // Checkin

        PlanDate planDate = mock(PlanDate.class);
        when(planDate.getId()).thenReturn(planDateId);
        when(planDate.getStartDate()).thenReturn(DateTimeUtils.getCurrentTimeMillis() - 3600000); // 1 hour ago
        when(planDate.getRequiredCheckin()).thenReturn(StatusType.ENABLE);
        when(planDate.getRequiredCheckout()).thenReturn(StatusType.ENABLE); // Checkout required

        UserStudent userStudent = mock(UserStudent.class);
        when(userStudent.getId()).thenReturn(userStudentId);
        when(userStudent.getName()).thenReturn(studentName);
        when(userStudent.getStatus()).thenReturn(EntityStatus.ACTIVE);

        when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
        when(userStudentRepository.findById(userStudentId)).thenReturn(Optional.of(userStudent));

        // No existing attendance found
        when(attendanceRepository.findByPlanDate_IdAndUserStudent_Id(planDateId, userStudentId))
                .thenReturn(Optional.empty());

        Attendance savedAttendance = new Attendance();
        savedAttendance.setPlanDate(planDate);
        savedAttendance.setUserStudent(userStudent);
        savedAttendance.setAttendanceStatus(AttendanceStatus.CHECKIN);

        when(attendanceRepository.save(any(Attendance.class))).thenReturn(savedAttendance);

        doNothing().when(userActivityLogHelper).saveLog(anyString());

        // Act
        ResponseEntity<?> response = attendanceService.changeStatus(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thay đổi trạng thái checkin thành công sinh viên " + studentName, apiResponse.getMessage());

        // Verify that attendance was saved with CHECKIN status
        assertEquals(AttendanceStatus.CHECKIN, savedAttendance.getAttendanceStatus());

        verify(attendanceRepository).save(any(Attendance.class));
        verify(userActivityLogHelper).saveLog(anyString());
    }

    @Test
    @DisplayName("changeStatus should reject checkout when checkin not performed")
    void testChangeStatus_RejectCheckout() {
        // Arrange
        String planDateId = "plan-date-1";
        String userStudentId = "user-student-1";

        SPDModifyPlanDateAttendanceRequest request = new SPDModifyPlanDateAttendanceRequest();
        request.setIdPlanDate(planDateId);
        request.setIdUserStudent(userStudentId);
        request.setStatus(1); // Checkout

        PlanDate planDate = mock(PlanDate.class);
        when(planDate.getId()).thenReturn(planDateId);
        when(planDate.getStartDate()).thenReturn(DateTimeUtils.getCurrentTimeMillis() - 3600000); // 1 hour ago
        when(planDate.getRequiredCheckin()).thenReturn(StatusType.ENABLE);
        when(planDate.getRequiredCheckout()).thenReturn(StatusType.ENABLE);

        UserStudent userStudent = mock(UserStudent.class);
        when(userStudent.getId()).thenReturn(userStudentId);
        when(userStudent.getStatus()).thenReturn(EntityStatus.ACTIVE);

        when(planDateRepository.findById(planDateId)).thenReturn(Optional.of(planDate));
        when(userStudentRepository.findById(userStudentId)).thenReturn(Optional.of(userStudent));

        // Attendance exists but not checked in
        Attendance existingAttendance = new Attendance();
        existingAttendance.setAttendanceStatus(AttendanceStatus.ABSENT); // Not CHECKIN
        when(attendanceRepository.findByPlanDate_IdAndUserStudent_Id(planDateId, userStudentId))
                .thenReturn(Optional.of(existingAttendance));

        // Act
        ResponseEntity<?> response = attendanceService.changeStatus(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không thể checkout khi chưa checkin", apiResponse.getMessage());

        verify(attendanceRepository, never()).save(any(Attendance.class));
    }
}