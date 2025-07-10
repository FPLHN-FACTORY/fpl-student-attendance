package udpm.hn.studentattendance.core.staff.plan.services.impl;

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
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanFactoryRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDFactoryResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanFactoryResponse;
import udpm.hn.studentattendance.core.staff.plan.repositories.*;
import udpm.hn.studentattendance.entities.*;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SPDPlanFactoryServiceImplTest {

    @Mock
    private SPDPlanRepository spdPlanRepository;

    @Mock
    private SPDPlanFactoryRepository spdPlanFactoryRepository;

    @Mock
    private SPDFactoryRepository spdFactoryRepository;

    @Mock
    private SPDPlanDateRepository spdPlanDateRepository;

    @Mock
    private SPDFacilityShiftRepository spdFacilityShiftRepository;

    @Mock
    private CommonUserStudentRepository commonUserStudentRepository;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private UserActivityLogHelper userActivityLogHelper;

    @InjectMocks
    private SPDPlanFactoryServiceImpl planFactoryService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("getAllList should return paginated plan factory list")
    void testGetAllList() {
        // Arrange
        String facilityId = "facility-1";
        SPDFilterPlanFactoryRequest request = new SPDFilterPlanFactoryRequest();

        lenient().when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        Page<SPDPlanFactoryResponse> page = new PageImpl<>(new ArrayList<>());
        when(spdPlanFactoryRepository.getAllByFilter(any(Pageable.class), eq(request))).thenReturn(page);

        // Act
        ResponseEntity<?> response = planFactoryService.getAllList(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());

        assertEquals(facilityId, request.getIdFacility());
        verify(spdPlanFactoryRepository).getAllByFilter(any(Pageable.class), eq(request));
    }

    @Test
    @DisplayName("getListFactory should return factory list for a plan")
    void testGetListFactory_Success() {
        // Arrange
        String planId = "plan-1";
        String projectId = "project-1";
        String facilityId = "facility-1";

        lenient().when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        Plan plan = mock(Plan.class);
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(projectId);
        SubjectFacility subjectFacility = mock(SubjectFacility.class);
        Facility facility = mock(Facility.class);
        when(facility.getId()).thenReturn(facilityId);

        when(plan.getProject()).thenReturn(project);
        when(project.getSubjectFacility()).thenReturn(subjectFacility);
        when(subjectFacility.getFacility()).thenReturn(facility);

        when(spdPlanRepository.findById(planId)).thenReturn(Optional.of(plan));

        List<SPDFactoryResponse> mockFactories = Arrays.asList(
                mock(SPDFactoryResponse.class),
                mock(SPDFactoryResponse.class));
        when(spdPlanFactoryRepository.getListFactory(projectId)).thenReturn(mockFactories);

        // Act
        ResponseEntity<?> response = planFactoryService.getListFactory(planId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());
        assertEquals(mockFactories, apiResponse.getData());

        verify(spdPlanRepository).findById(planId);
        verify(spdPlanFactoryRepository).getListFactory(projectId);
    }

    @Test
    @DisplayName("getListFactory should return error when plan not found")
    void testGetListFactory_PlanNotFound() {
        // Arrange
        String planId = "non-existent-plan";
        String facilityId = "facility-1";

        lenient().when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        lenient().when(spdPlanRepository.findById(planId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = planFactoryService.getListFactory(planId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Kế hoạch không tồn tại hoặc đã bị xoá", apiResponse.getMessage());

        verify(spdPlanFactoryRepository, never()).getListFactory(anyString());
    }

    @Test
    @DisplayName("changeStatus should toggle plan factory status")
    void testChangeStatus_Success() {
        // Arrange
        String planFactoryId = "plan-factory-1";
        String facilityId = "facility-1";

        lenient().when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        PlanFactory planFactory = mock(PlanFactory.class);
        when(planFactory.getId()).thenReturn(planFactoryId);
        when(planFactory.getStatus()).thenReturn(EntityStatus.ACTIVE);

        Factory factory = mock(Factory.class);
        when(factory.getName()).thenReturn("Test Factory");

        Plan plan = mock(Plan.class);
        when(plan.getName()).thenReturn("Test Plan");
        when(plan.getFromDate()).thenReturn(System.currentTimeMillis());
        when(plan.getToDate()).thenReturn(System.currentTimeMillis() + 86400000L);

        when(planFactory.getFactory()).thenReturn(factory);
        when(planFactory.getPlan()).thenReturn(plan);

        // Mock the repository methods
        when(spdPlanFactoryRepository.findById(planFactoryId)).thenReturn(Optional.of(planFactory));

        // Mock getDetail method to return valid response
        SPDPlanFactoryResponse planFactoryResponse = mock(SPDPlanFactoryResponse.class);
        when(planFactoryResponse.getStatus()).thenReturn(EntityStatus.ACTIVE.ordinal());
        when(spdPlanFactoryRepository.getDetail(planFactoryId, facilityId))
                .thenReturn(Optional.of(planFactoryResponse));

        // Mock the save method to return the updated entity
        when(spdPlanFactoryRepository.save(any(PlanFactory.class))).thenReturn(planFactory);

        doNothing().when(userActivityLogHelper).saveLog(anyString());

        // Act
        ResponseEntity<?> response = planFactoryService.changeStatus(planFactoryId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thay đổi trạng thái kế hoạch thành công", apiResponse.getMessage());

        verify(planFactory).setStatus(EntityStatus.INACTIVE);
        verify(spdPlanFactoryRepository).save(planFactory);
        verify(userActivityLogHelper).saveLog(anyString());
    }

    @Test
    @DisplayName("deletePlanFactory should delete inactive plan factory")
    void testDeletePlanFactory_Success() {
        // Arrange
        String planFactoryId = "plan-factory-1";

        PlanFactory planFactory = mock(PlanFactory.class);
        when(planFactory.getId()).thenReturn(planFactoryId);
        when(planFactory.getStatus()).thenReturn(EntityStatus.INACTIVE);

        Factory factory = mock(Factory.class);
        when(factory.getName()).thenReturn("Test Factory");

        Plan plan = mock(Plan.class);
        when(plan.getName()).thenReturn("Test Plan");
        when(plan.getFromDate()).thenReturn(System.currentTimeMillis());
        when(plan.getToDate()).thenReturn(System.currentTimeMillis() + 86400000L);

        when(planFactory.getFactory()).thenReturn(factory);
        when(planFactory.getPlan()).thenReturn(plan);

        when(spdPlanFactoryRepository.findById(planFactoryId)).thenReturn(Optional.of(planFactory));

        when(spdPlanFactoryRepository.deleteAllAttendanceByIdPlanFactory(planFactoryId)).thenReturn(1);
        when(spdPlanFactoryRepository.deleteAllPlanDateByIdPlanFactory(planFactoryId)).thenReturn(1);
        doNothing().when(spdPlanFactoryRepository).delete(planFactory);
        doNothing().when(userActivityLogHelper).saveLog(anyString());

        // Act
        ResponseEntity<?> response = planFactoryService.deletePlanFactory(planFactoryId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Xoá thành công nhóm xưởng ra khỏi kế hoạch", apiResponse.getMessage());

        verify(spdPlanFactoryRepository).deleteAllAttendanceByIdPlanFactory(planFactoryId);
        verify(spdPlanFactoryRepository).deleteAllPlanDateByIdPlanFactory(planFactoryId);
        verify(spdPlanFactoryRepository).delete(planFactory);
        verify(userActivityLogHelper).saveLog(anyString());
    }

    @Test
    @DisplayName("deletePlanFactory should reject deletion of active plan factory")
    void testDeletePlanFactory_ActiveFactory() {
        // Arrange
        String planFactoryId = "plan-factory-1";

        PlanFactory planFactory = mock(PlanFactory.class);
        when(planFactory.getStatus()).thenReturn(EntityStatus.ACTIVE); // Active factory

        when(spdPlanFactoryRepository.findById(planFactoryId)).thenReturn(Optional.of(planFactory));

        // Act
        ResponseEntity<?> response = planFactoryService.deletePlanFactory(planFactoryId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không thể xoá nhóm xưởng đang triển khai trong kế hoạch này", apiResponse.getMessage());

        verify(spdPlanFactoryRepository, never()).deleteAllAttendanceByIdPlanFactory(anyString());
        verify(spdPlanFactoryRepository, never()).delete(any(PlanFactory.class));
    }

    @Test
    @DisplayName("getListShift should return facility shifts")
    void testGetListShift() {
        // Arrange
        String facilityId = "facility-1";
        lenient().when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        List<FacilityShift> mockShifts = Arrays.asList(
                mock(FacilityShift.class),
                mock(FacilityShift.class));

        when(spdFacilityShiftRepository.getAllList(facilityId)).thenReturn(mockShifts);

        // Act
        ResponseEntity<?> response = planFactoryService.getListShift();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());
        assertEquals(mockShifts, apiResponse.getData());

        verify(spdFacilityShiftRepository).getAllList(facilityId);
    }
}