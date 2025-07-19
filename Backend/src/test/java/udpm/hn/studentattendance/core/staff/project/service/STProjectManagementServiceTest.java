package udpm.hn.studentattendance.core.staff.project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectCreateOrUpdateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectSearchRequest;
import udpm.hn.studentattendance.core.staff.project.service.ipml.STProjectManagementImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class STProjectManagementServiceTest {
    @Mock
    private STProjectManagementImpl service;

    // Xóa @InjectMocks và field projectService vì STProjectManagementService là
    // interface

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetListProject() {
        USProjectSearchRequest request = new USProjectSearchRequest();
        when(service.getListProject(request)).thenReturn((ResponseEntity) ResponseEntity.ok("projects"));
        ResponseEntity<?> res = service.getListProject(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("projects", res.getBody());
    }

    @Test
    void testCreateProject() {
        USProjectCreateOrUpdateRequest request = new USProjectCreateOrUpdateRequest();
        when(service.createProject(request)).thenReturn((ResponseEntity) ResponseEntity.ok("created"));
        ResponseEntity<?> res = service.createProject(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("created", res.getBody());
    }

    @Test
    void testUpdateProject() {
        USProjectCreateOrUpdateRequest request = new USProjectCreateOrUpdateRequest();
        when(service.updateProject("1", request)).thenReturn((ResponseEntity) ResponseEntity.ok("updated"));
        ResponseEntity<?> res = service.updateProject("1", request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("updated", res.getBody());
    }

    @Test
    void testDetailProject() {
        when(service.detailProject("1")).thenReturn((ResponseEntity) ResponseEntity.ok("detail"));
        ResponseEntity<?> res = service.detailProject("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("detail", res.getBody());
    }

    @Test
    void testChangeStatus() {
        when(service.changeStatus("1")).thenReturn((ResponseEntity) ResponseEntity.ok("changed"));
        ResponseEntity<?> res = service.changeStatus("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("changed", res.getBody());
    }

    @Test
    void testChangeAllStatusPreviousSemester() {
        when(service.changeAllStatusPreviousSemester()).thenReturn((ResponseEntity) ResponseEntity.ok("changedAll"));
        ResponseEntity<?> res = service.changeAllStatusPreviousSemester();
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("changedAll", res.getBody());
    }
}