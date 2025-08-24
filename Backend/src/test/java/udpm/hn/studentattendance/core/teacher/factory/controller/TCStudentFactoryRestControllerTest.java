package udpm.hn.studentattendance.core.teacher.factory.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.factory.service.TCStudentFactoryService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCStudentFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCPlanDateStudentFactoryRequest;

@ExtendWith(MockitoExtension.class)
class TCStudentFactoryRestControllerTest {
    @Mock
    private TCStudentFactoryService tcStudentFactoryService;

    @InjectMocks
    private TCStudentFactoryRestController tcStudentFactoryRestController;

    @Test
    void testTCStudentFactoryRestControllerExists() {
        assertNotNull(tcStudentFactoryRestController);
    }

    @Test
    void testGetAllStudentFactory() {
        TCStudentFactoryRequest request = new TCStudentFactoryRequest();
        when(tcStudentFactoryService.getAllStudentFactory(request)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = tcStudentFactoryRestController.getAllStudentFactory(request);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(tcStudentFactoryService).getAllStudentFactory(request);
    }

    @Test
    void testGetDetailAttendance() {
        TCPlanDateStudentFactoryRequest request = new TCPlanDateStudentFactoryRequest();
        when(tcStudentFactoryService.getDetailAttendance(request)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = tcStudentFactoryRestController.getDetailAttendance(request);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(tcStudentFactoryService).getDetailAttendance(request);
    }

    @Test
    void testDeleteStudentFactory() {
        String id = "studentFactory123";
        when(tcStudentFactoryService.deleteStudentFactoryById(id)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = tcStudentFactoryRestController.deleteStudentFactory(id);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(tcStudentFactoryService).deleteStudentFactoryById(id);
    }
}
