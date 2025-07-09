package udpm.hn.studentattendance.core.staff.studentfactory.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class USStudentFactoryServiceTest {
    @Mock
    private USStudentFactoryService usStudentFactoryService;

    @Test
    void testUSStudentFactoryServiceExists() {
        assertNotNull(usStudentFactoryService);
    }

    @Test
    void testGetAllStudentInFactory() {
        String factoryId = "123";
        udpm.hn.studentattendance.core.staff.studentfactory.model.request.USStudentFactoryRequest request = new udpm.hn.studentattendance.core.staff.studentfactory.model.request.USStudentFactoryRequest();
        when(usStudentFactoryService.getAllStudentInFactory(factoryId, request))
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usStudentFactoryService.getAllStudentInFactory(factoryId, request);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteStudentInFactory() {
        String studentFactoryId = "123";
        when(usStudentFactoryService.deleteStudentInFactory(studentFactoryId))
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usStudentFactoryService.deleteStudentInFactory(studentFactoryId);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testChangeStatus() {
        String studentFactoryId = "123";
        when(usStudentFactoryService.changeStatus(studentFactoryId))
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usStudentFactoryService.changeStatus(studentFactoryId);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetAllStudent() {
        udpm.hn.studentattendance.core.staff.studentfactory.model.request.USUserStudentRequest request = new udpm.hn.studentattendance.core.staff.studentfactory.model.request.USUserStudentRequest();
        when(usStudentFactoryService.getAllStudent(request))
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usStudentFactoryService.getAllStudent(request);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateOrDeleteStudentFactory() {
        udpm.hn.studentattendance.core.staff.studentfactory.model.request.USStudentFactoryCreateUpdateRequest request = new udpm.hn.studentattendance.core.staff.studentfactory.model.request.USStudentFactoryCreateUpdateRequest();
        when(usStudentFactoryService.createOrDeleteStudentFactory(request))
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usStudentFactoryService.createOrDeleteStudentFactory(request);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetStudentFactoryExist() {
        String id = "123";
        when(usStudentFactoryService.getStudentFactoryExist(id))
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usStudentFactoryService.getStudentFactoryExist(id);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testDetailStudentFactory() {
        String id = "123";
        when(usStudentFactoryService.detailStudentFactory(id))
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usStudentFactoryService.detailStudentFactory(id);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetAllPlanDateByStudent() {
        String id = "123";
        udpm.hn.studentattendance.core.staff.studentfactory.model.request.USPDDetailShiftByStudentRequest request = new udpm.hn.studentattendance.core.staff.studentfactory.model.request.USPDDetailShiftByStudentRequest();
        when(usStudentFactoryService.getAllPlanDateByStudent(request, id))
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usStudentFactoryService.getAllPlanDateByStudent(request, id);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
}