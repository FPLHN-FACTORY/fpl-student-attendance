package udpm.hn.studentattendance.core.teacher.factory.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.factory.service.TCFactoryService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFactoryRequest;

@ExtendWith(MockitoExtension.class)
class TCFactoryRestControllerTest {
    @Mock
    private TCFactoryService tcFactoryService;

    @InjectMocks
    private TCFactoryRestController tcFactoryRestController;

    @Test
    void testTCFactoryRestControllerExists() {
        assertNotNull(tcFactoryRestController);
    }

    @Test
    void testGetAllFactoryByTeacher() {
        TCFactoryRequest request = new TCFactoryRequest();
        when(tcFactoryService.getAllFactoryByTeacher(request)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = tcFactoryRestController.getAllFactoryByTeacher(request);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testGetAllProjectByFacility() {
        when(tcFactoryService.getAllProjectByFacility()).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = tcFactoryRestController.getAllProjectByFacility();
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testGetAllSemester() {
        when(tcFactoryService.getAllSemester()).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = tcFactoryRestController.getAllSemester();
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }
}
