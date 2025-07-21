package udpm.hn.studentattendance.core.staff.student.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.student.model.request.USStudentCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.student.model.request.USStudentRequest;
import udpm.hn.studentattendance.core.staff.student.service.STStudentService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class USStudentRestControllerTest {
    @Mock
    private STStudentService studentService;

    @InjectMocks
    private USStudentRestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllStudent() {
        USStudentRequest request = new USStudentRequest();
        when(studentService.getAllStudentByFacility(request))
                .thenReturn((ResponseEntity) ResponseEntity.ok("students"));
        ResponseEntity<?> res = controller.getAllStudent(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("students", res.getBody());
    }

    @Test
    void testGetStudentById() {
        when(studentService.getDetailStudent("1")).thenReturn((ResponseEntity) ResponseEntity.ok("student"));
        ResponseEntity<?> res = controller.getStudentById("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("student", res.getBody());
    }

    @Test
    void testCreateStudent() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        when(studentService.createStudent(request)).thenReturn((ResponseEntity) ResponseEntity.ok("created"));
        ResponseEntity<?> res = controller.createStudent(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("created", res.getBody());
    }

    @Test
    void testUpdateStudent() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        when(studentService.updateStudent(request)).thenReturn((ResponseEntity) ResponseEntity.ok("updated"));
        ResponseEntity<?> res = controller.updateStudent(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("updated", res.getBody());
    }

    @Test
    void testChangeStatus() {
        when(studentService.changeStatusStudent("1")).thenReturn((ResponseEntity) ResponseEntity.ok("changed"));
        ResponseEntity<?> res = controller.changeStatus("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("changed", res.getBody());
    }

    @Test
    void testChangeFaceStudent() {
        when(studentService.deleteFaceStudentFactory("1"))
                .thenReturn((ResponseEntity) ResponseEntity.ok("faceDeleted"));
        ResponseEntity<?> res = controller.changeFaceStudent("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("faceDeleted", res.getBody());
    }

    @Test
    void testIsNotExistFace() {
        when(studentService.isExistFace()).thenReturn((ResponseEntity) ResponseEntity.ok("exist"));
        ResponseEntity<?> res = controller.isNotExistFace();
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("exist", res.getBody());
    }
}
