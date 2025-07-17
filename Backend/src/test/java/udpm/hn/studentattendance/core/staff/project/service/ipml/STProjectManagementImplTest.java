package udpm.hn.studentattendance.core.staff.project.service.ipml;

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
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectCreateOrUpdateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectSearchRequest;
import udpm.hn.studentattendance.core.staff.project.model.response.USProjectResponse;
import udpm.hn.studentattendance.core.staff.project.repository.*;
import udpm.hn.studentattendance.entities.*;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.constants.SemesterName;

import java.util.ArrayList;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class STProjectManagementImplTest {

    @Mock
    private STProjectExtendRepository projectManagementRepository;

    @Mock
    private STLevelProjectExtendRepository levelProjectRepository;

    @Mock
    private STProjectSemesterExtendRepository semesterRepository;

    @Mock
    private STProjectSubjectFacilityExtendRepository subjectFacilityRepository;

    @Mock
    private CommonUserStudentRepository commonUserStudentRepository;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private STProjectChangeSemesterExtendRepository deleteBulkByProject;

    @Mock
    private UserActivityLogHelper userActivityLogHelper;

    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @InjectMocks
    private STProjectManagementImpl projectManagementService;

    @Test
    @DisplayName("getListProject should return paginated project list")
    void testGetListProject() {
        // Arrange
        USProjectSearchRequest request = new USProjectSearchRequest();

        Page<USProjectResponse> page = new PageImpl<>(new ArrayList<>());
        when(projectManagementRepository.getListProject(any(Pageable.class), eq(request)))
                .thenReturn(page);

        // Act
        ResponseEntity<?> response = projectManagementService.getListProject(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy danh sách dự án thành công", apiResponse.getMessage());

        verify(projectManagementRepository).getListProject(any(Pageable.class), eq(request));
    }

    @Test
    @DisplayName("createProject should create a new project successfully")
    void testCreateProject_Success() {
        // Given
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        USProjectCreateOrUpdateRequest request = new USProjectCreateOrUpdateRequest();
        request.setName("Test Project");
        request.setDescription("Test Description");
        request.setLevelProjectId("level-1");
        request.setSemesterId("semester-1");
        request.setSubjectFacilityId("sf-1");

        LevelProject levelProject = mock(LevelProject.class);
        when(levelProject.getId()).thenReturn("level-1");
        when(levelProject.getName()).thenReturn("Level 1");

        Semester semester = mock(Semester.class);
        when(semester.getId()).thenReturn("semester-1");
        when(semester.getSemesterName()).thenReturn(SemesterName.FALL);
        when(semester.getYear()).thenReturn(2023);
        // Set future date to make test pass
        long futureDate = System.currentTimeMillis() + 86400000L; // tomorrow
        when(semester.getToDate()).thenReturn(futureDate);

        Subject subject = mock(Subject.class);
        when(subject.getName()).thenReturn("Test Subject");

        SubjectFacility subjectFacility = mock(SubjectFacility.class);
        when(subjectFacility.getId()).thenReturn("sf-1");
        when(subjectFacility.getSubject()).thenReturn(subject);

        when(levelProjectRepository.findById("level-1")).thenReturn(Optional.of(levelProject));
        when(semesterRepository.findById("semester-1")).thenReturn(Optional.of(semester));
        when(subjectFacilityRepository.findById("sf-1")).thenReturn(Optional.of(subjectFacility));

        when(projectManagementRepository.isExistProjectInSameLevel(
                anyString(), anyString(), anyString(), anyString(), isNull()))
                .thenReturn(false);

        Project savedProject = new Project();
        savedProject.setId("new-project-id");
        savedProject.setName(request.getName());
        savedProject.setDescription(request.getDescription());
        savedProject.setLevelProject(levelProject);
        savedProject.setSemester(semester);
        savedProject.setSubjectFacility(subjectFacility);
        savedProject.setStatus(EntityStatus.ACTIVE);

        when(projectManagementRepository.save(any(Project.class))).thenReturn(savedProject);
        doNothing().when(userActivityLogHelper).saveLog(anyString());

        // Then
        ResponseEntity<?> response = projectManagementService.createProject(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Thêm dự án thành công", apiResponse.getMessage());

        verify(projectManagementRepository).save(any(Project.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thêm 1 dự án mới:"));
    }

    @Test
    @DisplayName("createProject should reject when semester is ended")
    void testCreateProject_EndedSemester() {
        // Arrange
        USProjectCreateOrUpdateRequest request = new USProjectCreateOrUpdateRequest();
        request.setName("Test Project");
        request.setLevelProjectId("level-1");
        request.setSemesterId("semester-past");
        request.setSubjectFacilityId("sf-1");

        LevelProject levelProject = mock(LevelProject.class);

        Semester semester = mock(Semester.class);
        // Set past date to trigger validation error
        long pastDate = System.currentTimeMillis() - 86400000L; // yesterday
        when(semester.getToDate()).thenReturn(pastDate);

        when(levelProjectRepository.findById("level-1")).thenReturn(Optional.of(levelProject));
        when(semesterRepository.findById("semester-past")).thenReturn(Optional.of(semester));

        // Act
        ResponseEntity<?> response = projectManagementService.createProject(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Vui lòng chọn học kỳ đang hoặc chưa diễn ra cho dự án mới", apiResponse.getMessage());

        verify(projectManagementRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("createProject should reject when project already exists")
    void testCreateProject_AlreadyExists() {
        // Arrange
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        USProjectCreateOrUpdateRequest request = new USProjectCreateOrUpdateRequest();
        request.setName("Existing Project");
        request.setLevelProjectId("level-1");
        request.setSemesterId("semester-1");
        request.setSubjectFacilityId("sf-1");

        LevelProject levelProject = mock(LevelProject.class);
        when(levelProject.getId()).thenReturn("level-1");
        when(levelProject.getName()).thenReturn("Level 1");

        Semester semester = mock(Semester.class);
        when(semester.getId()).thenReturn("semester-1");
        when(semester.getSemesterName()).thenReturn(SemesterName.FALL);
        when(semester.getYear()).thenReturn(2023);
        long futureDate = System.currentTimeMillis() + 86400000L; // tomorrow
        when(semester.getToDate()).thenReturn(futureDate);

        Subject subject = mock(Subject.class);
        when(subject.getName()).thenReturn("Test Subject");

        SubjectFacility subjectFacility = mock(SubjectFacility.class);
        when(subjectFacility.getId()).thenReturn("sf-1");
        when(subjectFacility.getSubject()).thenReturn(subject);

        when(levelProjectRepository.findById("level-1")).thenReturn(Optional.of(levelProject));
        when(semesterRepository.findById("semester-1")).thenReturn(Optional.of(semester));
        when(subjectFacilityRepository.findById("sf-1")).thenReturn(Optional.of(subjectFacility));

        when(projectManagementRepository.isExistProjectInSameLevel(
                anyString(), anyString(), anyString(), anyString(), isNull()))
                .thenReturn(true);

        // Act
        ResponseEntity<?> response = projectManagementService.createProject(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertTrue(apiResponse.getMessage().contains("Dự án này đã tồn tại ở"));

        verify(projectManagementRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("changeStatus should toggle project status")
    void testChangeStatus() {
        // Arrange
        String projectId = "project-1";

        Project project = new Project();
        project.setId(projectId);
        project.setName("Test Project");
        project.setStatus(EntityStatus.ACTIVE);

        when(projectManagementRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectManagementRepository.save(any(Project.class))).thenReturn(project);
        doNothing().when(userActivityLogHelper).saveLog(anyString());

        // Act
        ResponseEntity<?> response = projectManagementService.changeStatus(projectId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Chuyển trạng thái thành công", apiResponse.getMessage());

        assertEquals(EntityStatus.INACTIVE, project.getStatus());
        verify(projectManagementRepository).save(project);
        verify(userActivityLogHelper).saveLog(anyString());
    }
}