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
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddOrUpdatePlanRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterCreatePlanRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDLevelProjectResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDProjectResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDSubjectResponse;
import udpm.hn.studentattendance.core.staff.plan.repositories.*;
import udpm.hn.studentattendance.entities.Plan;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.entities.SubjectFacility;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.constants.SemesterName;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;
import udpm.hn.studentattendance.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SPDPlanServiceImplTest {

    @Mock
    private SPDPlanRepository spdPlanRepository;

    @Mock
    private SPDSubjectRepository spdSubjectRepository;

    @Mock
    private SPDLevelProjectRepository spdLevelProjectRepository;

    @Mock
    private SPDSemesterRepository spdSemesterRepository;

    @Mock
    private SPDProjectRepository spdProjectRepository;

    @Mock
    private CommonUserStudentRepository commonUserStudentRepository;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private UserActivityLogHelper userActivityLogHelper;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private SPDPlanServiceImpl planService;

    @Test
    @DisplayName("getAllSubject should return cached data when available")
    void testGetAllSubject_WithCachedData() {
        // Arrange
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        List<SPDSubjectResponse> cachedData = Arrays.asList(mock(SPDSubjectResponse.class));
        when(redisService.get("plan:subjects:" + facilityId)).thenReturn(cachedData);

        // Act
        ResponseEntity<?> response = planService.getAllSubject();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy dữ liệu bộ môn thành công (cached)", apiResponse.getMessage());
        assertEquals(cachedData, apiResponse.getData());

        verify(redisService).get("plan:subjects:" + facilityId);
        verify(spdSubjectRepository, never()).getAllByFacility(anyString());
    }

    @Test
    @DisplayName("getAllSubject should query database when cache is missed")
    void testGetAllSubject_WithoutCachedData() {
        // Arrange
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(redisService.get(anyString())).thenReturn(null);

        List<SPDSubjectResponse> dbData = Arrays.asList(mock(SPDSubjectResponse.class));
        when(spdSubjectRepository.getAllByFacility(facilityId)).thenReturn(dbData);

        ReflectionTestUtils.setField(planService, "redisTTL", 3600L);

        // Act
        ResponseEntity<?> response = planService.getAllSubject();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy dữ liệu bộ môn thành công", apiResponse.getMessage());
        assertEquals(dbData, apiResponse.getData());

        verify(redisService).get("plan:subjects:" + facilityId);
        verify(spdSubjectRepository).getAllByFacility(facilityId);
        verify(redisService).set(eq("plan:subjects:" + facilityId), eq(dbData), eq(3600L));
    }

    @Test
    @DisplayName("getAllLevel should return all level projects")
    void testGetAllLevel() {
        // Arrange
        when(redisService.get(anyString())).thenReturn(null);
        List<SPDLevelProjectResponse> levels = Arrays.asList(mock(SPDLevelProjectResponse.class));
        when(spdLevelProjectRepository.getAll()).thenReturn(levels);

        ReflectionTestUtils.setField(planService, "redisTTL", 3600L);

        // Act
        ResponseEntity<?> response = planService.getAllLevel();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy dữ liệu level thành công", apiResponse.getMessage());
        assertEquals(levels, apiResponse.getData());

        verify(spdLevelProjectRepository).getAll();
        verify(redisService).set(anyString(), eq(levels), eq(3600L * 2));
    }

    @Test
    @DisplayName("getListSemester should return all semester names")
    void testGetListSemester() {
        // Arrange
        when(redisService.get(anyString())).thenReturn(null);
        ReflectionTestUtils.setField(planService, "redisTTL", 3600L);

        // Act
        ResponseEntity<?> response = planService.getListSemester();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy dữ liệu học kỳ thành công", apiResponse.getMessage());

        @SuppressWarnings("unchecked")
        List<String> semesterNames = (List<String>) apiResponse.getData();
        assertEquals(SemesterName.values().length, semesterNames.size());

        verify(redisService).set(anyString(), any(), eq(3600L * 24));
    }

    @Test
    @DisplayName("getAllList should return paginated plan list")
    void testGetAllList() {
        // Arrange
        String facilityId = "facility-1";
        SPDFilterPlanRequest request = new SPDFilterPlanRequest();

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(redisService.get(anyString())).thenReturn(null);

        Page<SPDPlanResponse> planPage = new PageImpl<>(new ArrayList<>());
        when(spdPlanRepository.getAllByFilter(any(Pageable.class), any(SPDFilterPlanRequest.class)))
                .thenReturn(planPage);

        ReflectionTestUtils.setField(planService, "redisTTL", 3600L);

        // Act
        ResponseEntity<?> response = planService.getAllList(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());

        verify(spdPlanRepository).getAllByFilter(any(Pageable.class), eq(request));
        verify(redisService).set(anyString(), any(PageableObject.class), eq(3600L));
        assertEquals(facilityId, request.getIdFacility());
    }

    @Test
    @DisplayName("createPlan should create new plan successfully")
    void testCreatePlan_Success() {
        // Arrange
        SPDAddOrUpdatePlanRequest request = new SPDAddOrUpdatePlanRequest();
        request.setIdProject("project-1");
        request.setName("Test Plan");
        request.setDescription("Description");
        request.setMaxLateArrival(15);

        // Setup dates (today and tomorrow)
        long today = DateTimeUtils.getCurrentTimeMillis();
        long tomorrow = today + 24 * 60 * 60 * 1000;
        request.setRangeDate(Arrays.asList(today, tomorrow));

        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        Project project = mock(Project.class);
        when(project.getId()).thenReturn("project-1");
        when(project.getName()).thenReturn("Test Project");
        when(project.getStatus()).thenReturn(EntityStatus.ACTIVE);

        Semester semester = mock(Semester.class);
        when(semester.getStatus()).thenReturn(EntityStatus.ACTIVE);
        when(semester.getFromDate()).thenReturn(today - 24 * 60 * 60 * 1000); // Yesterday
        when(semester.getToDate()).thenReturn(tomorrow + 24 * 60 * 60 * 1000); // Day after tomorrow
        when(project.getSemester()).thenReturn(semester);

        SubjectFacility subjectFacility = mock(SubjectFacility.class);
        Facility facility = mock(Facility.class);
        when(facility.getId()).thenReturn(facilityId);
        when(subjectFacility.getFacility()).thenReturn(facility);
        when(project.getSubjectFacility()).thenReturn(subjectFacility);

        when(spdProjectRepository.findById("project-1")).thenReturn(Optional.of(project));
        when(spdPlanRepository.isExistsProjectInPlan("project-1", null)).thenReturn(false);

        Plan savedPlan = new Plan();
        savedPlan.setId("plan-1");
        savedPlan.setName(request.getName());
        when(spdPlanRepository.save(any(Plan.class))).thenReturn(savedPlan);

        // Act
        ResponseEntity<?> response = planService.createPlan(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Tạo mới kế hoạch thành công", apiResponse.getMessage());

        verify(spdPlanRepository).save(any(Plan.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thêm 1 kế hoạch mới"));
    }

    @Test
    @DisplayName("createPlan should reject if project already has plan")
    void testCreatePlan_ExistingProject() {
        // Arrange
        SPDAddOrUpdatePlanRequest request = new SPDAddOrUpdatePlanRequest();
        request.setIdProject("project-1");
        request.setRangeDate(Arrays.asList(
                DateTimeUtils.getCurrentTimeMillis(),
                DateTimeUtils.getCurrentTimeMillis() + 86400000L));

        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        Project project = mock(Project.class);
        when(project.getId()).thenReturn("project-1");
        when(project.getName()).thenReturn("Test Project");
        when(project.getStatus()).thenReturn(EntityStatus.ACTIVE);

        Semester semester = mock(Semester.class);
        when(semester.getStatus()).thenReturn(EntityStatus.ACTIVE);
        when(project.getSemester()).thenReturn(semester);

        SubjectFacility subjectFacility = mock(SubjectFacility.class);
        Facility facility = mock(Facility.class);
        when(facility.getId()).thenReturn(facilityId);
        when(subjectFacility.getFacility()).thenReturn(facility);
        when(project.getSubjectFacility()).thenReturn(subjectFacility);

        when(spdProjectRepository.findById("project-1")).thenReturn(Optional.of(project));
        when(spdPlanRepository.isExistsProjectInPlan("project-1", null)).thenReturn(true);

        // Act
        ResponseEntity<?> response = planService.createPlan(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertTrue(apiResponse.getMessage().contains("đã được triển khai"));

        verify(spdPlanRepository, never()).save(any(Plan.class));
    }
}