package udpm.hn.studentattendance.core.teacher.teachingschedule.service.impl;

import org.junit.jupiter.api.BeforeEach;
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
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @InjectMocks
    private TCTeachingScheduleServiceImpl teachingScheduleService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(teachingScheduleService, "MAX_LATE_ARRIVAL", 15);
    }

    @Test
    @DisplayName("getAllTeachingScheduleByStaff should return paginated teaching schedules")
    void testGetAllTeachingScheduleByStaff() {
        // Arrange
        String userId = "teacher-1";
        TCTeachingScheduleRequest request = new TCTeachingScheduleRequest();

        when(sessionHelper.getUserId()).thenReturn(userId);

        Page<TCTeachingScheduleResponse> page = new PageImpl<>(new ArrayList<>());
        when(teachingScheduleRepository.getAllTeachingScheduleByStaff(eq(userId), any(Pageable.class), eq(request)))
                .thenReturn(page);

        // Act
        ResponseEntity<?> response = teachingScheduleService.getAllTeachingScheduleByStaff(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getMessage().contains("Lấy tất cả lịch dạy của"));

        verify(teachingScheduleRepository).getAllTeachingScheduleByStaff(eq(userId), any(Pageable.class), eq(request));
    }

    @Test
    @DisplayName("getAllFactoryByStaff should return factories for a teacher")
    void testGetAllFactoryByStaff() {
        // Arrange
        String userId = "teacher-1";
        when(sessionHelper.getUserId()).thenReturn(userId);

        List<Factory> mockFactories = Arrays.asList(
                mock(Factory.class),
                mock(Factory.class));

        when(factoryRepository.getAllFactoryByStaff(userId, EntityStatus.ACTIVE)).thenReturn(mockFactories);

        // Act
        ResponseEntity<?> response = teachingScheduleService.getAllFactoryByStaff();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getMessage().contains("Lấy tất cả nhóm xửng của"));
        assertEquals(mockFactories, apiResponse.getData());

        verify(factoryRepository).getAllFactoryByStaff(userId, EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("getAllProjectByStaff should return projects for a teacher")
    void testGetAllProjectByStaff() {
        // Arrange
        String userId = "teacher-1";
        when(sessionHelper.getUserId()).thenReturn(userId);

        List<Project> mockProjects = Arrays.asList(
                mock(Project.class),
                mock(Project.class));

        when(projectRepository.getAllProject(userId, EntityStatus.ACTIVE)).thenReturn(mockProjects);

        // Act
        ResponseEntity<?> response = teachingScheduleService.getAllProjectByStaff();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getMessage().contains("Lấy tất cả dự án đang dạy của"));
        assertEquals(mockProjects, apiResponse.getData());

        verify(projectRepository).getAllProject(userId, EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("getAllSubjectByStaff should return subjects for a teacher")
    void testGetAllSubjectByStaff() {
        // Arrange
        String userId = "teacher-1";
        when(sessionHelper.getUserId()).thenReturn(userId);

        List<Subject> mockSubjects = Arrays.asList(
                mock(Subject.class),
                mock(Subject.class));

        when(subjectRepository.getAllSubjectByStaff(userId, EntityStatus.ACTIVE)).thenReturn(mockSubjects);

        // Act
        ResponseEntity<?> response = teachingScheduleService.getAllSubjectByStaff();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getMessage().contains("Lấy tất cả môn học của"));
        assertEquals(mockSubjects, apiResponse.getData());

        verify(subjectRepository).getAllSubjectByStaff(userId, EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("updatePlanDate should update plan date details")
    void testUpdatePlanDate_Success() {
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
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Cập nhật thông tin buổi học thành công", apiResponse.getMessage());

        // Verify fields were updated
        verify(planDate).setDescription(request.getDescription());
        verify(planDate).setLateArrival(request.getLateArrival());
        verify(planDate).setLink(request.getLink());
        verify(planDate).setRoom(request.getRoom());
        verify(teachingScheduleRepository).save(planDate);
    }

    @Test
    @DisplayName("updatePlanDate should reject when MAX_LATE_ARRIVAL exceeded")
    void testUpdatePlanDate_MaxLateArrivalExceeded() {
        // Arrange
        String planDateId = "plan-date-1";
        String userId = "teacher-1";

        when(sessionHelper.getUserId()).thenReturn(userId);

        TCTSPlanDateUpdateRequest request = new TCTSPlanDateUpdateRequest();
        request.setIdPlanDate(planDateId);
        request.setLateArrival(20); // Exceeds MAX_LATE_ARRIVAL (15)

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

        // Act
        ResponseEntity<?> response = teachingScheduleService.updatePlanDate(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thời gian điểm danh muộn nhất không quá 15 phút", apiResponse.getMessage());

        verify(teachingScheduleRepository, never()).save(any(PlanDate.class));
    }

    @Test
    @DisplayName("updatePlanDate should reject when plan date not found")
    void testUpdatePlanDate_PlanDateNotFound() {
        // Arrange
        String planDateId = "non-existent-id";

        TCTSPlanDateUpdateRequest request = new TCTSPlanDateUpdateRequest();
        request.setIdPlanDate(planDateId);

        when(teachingScheduleRepository.findById(planDateId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = teachingScheduleService.updatePlanDate(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không tìm thấy lịch dạy", apiResponse.getMessage());

        verify(teachingScheduleRepository, never()).save(any(PlanDate.class));
    }

    @Test
    @DisplayName("getDetailPlanDate should return plan date details")
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
    }

    @Test
    @DisplayName("changeTypePlanDate should toggle between online and offline modes")
    void testChangeTypePlanDate_Success() {
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

        // Verify type was changed to ONLINE
        verify(planDate).setType(ShiftType.ONLINE);
        verify(teachingScheduleRepository).save(planDate);
    }
}