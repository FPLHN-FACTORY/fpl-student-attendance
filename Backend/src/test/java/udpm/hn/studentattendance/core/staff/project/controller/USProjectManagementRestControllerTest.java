package udpm.hn.studentattendance.core.staff.project.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectCreateOrUpdateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectSearchRequest;
import udpm.hn.studentattendance.core.staff.project.model.response.USLevelProjectResponse;
import udpm.hn.studentattendance.core.staff.project.model.response.USSemesterResponse;
import udpm.hn.studentattendance.core.staff.project.model.response.USSubjectResponse;
import udpm.hn.studentattendance.core.staff.project.service.STLevelProjectManagementService;
import udpm.hn.studentattendance.core.staff.project.service.STProjectManagementService;
import udpm.hn.studentattendance.core.staff.project.service.STSemesterManagementService;
import udpm.hn.studentattendance.core.staff.project.service.STSubjectFacilityManagementService;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.SessionHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class USProjectManagementRestControllerTest {
    @Mock
    private SessionHelper sessionHelper;
    @Mock
    private STProjectManagementService service;
    @Mock
    private STLevelProjectManagementService serviceLevel;
    @Mock
    private STSemesterManagementService serviceSemester;
    @Mock
    private STSubjectFacilityManagementService serviceSubjectFacility;

    @InjectMocks
    private USProjectManagementRestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetListProject() {
        USProjectSearchRequest req = new USProjectSearchRequest();
        when(sessionHelper.getFacilityId()).thenReturn("FAC123");
        when(service.getListProject(req)).thenReturn((ResponseEntity) ResponseEntity.ok("list"));
        ResponseEntity<?> res = controller.getListProject(req);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("list", res.getBody());
        assertEquals("FAC123", req.getFacilityId());
    }

    @Test
    void testGetProject() {
        when(service.detailProject("1")).thenReturn((ResponseEntity) ResponseEntity.ok("detail"));
        ResponseEntity<?> res = controller.getProject("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("detail", res.getBody());
    }

    @Test
    void testAddProject() {
        USProjectCreateOrUpdateRequest req = new USProjectCreateOrUpdateRequest();
        when(service.createProject(req)).thenReturn((ResponseEntity) ResponseEntity.ok("created"));
        ResponseEntity<?> res = controller.addProject(req);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("created", res.getBody());
    }

    @Test
    void testUpdateProject() {
        USProjectCreateOrUpdateRequest req = new USProjectCreateOrUpdateRequest();
        when(service.updateProject("1", req)).thenReturn((ResponseEntity) ResponseEntity.ok("updated"));
        ResponseEntity<?> res = controller.updateProject("1", req);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("updated", res.getBody());
    }

    @Test
    void testChangeStatusProject() {
        when(service.changeStatus("1")).thenReturn((ResponseEntity) ResponseEntity.ok("changed"));
        ResponseEntity<?> res = controller.changeStatusProject("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("changed", res.getBody());
    }

    @Test
    void testGetLevelProject() {
        USLevelProjectResponse mockLevel = mock(USLevelProjectResponse.class);
        List<USLevelProjectResponse> list = List.of(mockLevel);
        when(serviceLevel.getComboboxLevelProject()).thenReturn(list);
        List<USLevelProjectResponse> res = controller.getLevelProject();
        assertEquals(list, res);
    }

    @Test
    void testGetSemesterCombobox() {
        USSemesterResponse mockSemester = mock(USSemesterResponse.class);
        List<USSemesterResponse> list = List.of(mockSemester);
        when(serviceSemester.getComboboxSemester()).thenReturn(list);
        List<USSemesterResponse> res = controller.getSemester();
        assertEquals(list, res);
    }

    @Test
    void testGetAllSemester() {
        List<Semester> list = List.of(new Semester());
        when(serviceSemester.getSemester()).thenReturn(list);
        List<Semester> res = controller.getAllSemester();
        assertEquals(list, res);
    }

    @Test
    void testGetSubjectFacility() {
        when(sessionHelper.getFacilityId()).thenReturn("FAC123");
        USSubjectResponse mockSubject = mock(USSubjectResponse.class);
        List<USSubjectResponse> list = List.of(mockSubject);
        when(serviceSubjectFacility.getComboboxSubjectFacility("FAC123")).thenReturn(list);
        List<USSubjectResponse> res = controller.getSubjectFacility();
        assertEquals(list, res);
    }

    @Test
    void testChangeStatusProjectPreviousSemester() {
        when(service.changeAllStatusPreviousSemester()).thenReturn((ResponseEntity) ResponseEntity.ok("changedAll"));
        ResponseEntity<?> res = controller.changeStatusProjectPreviousSemester();
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("changedAll", res.getBody());
    }
}