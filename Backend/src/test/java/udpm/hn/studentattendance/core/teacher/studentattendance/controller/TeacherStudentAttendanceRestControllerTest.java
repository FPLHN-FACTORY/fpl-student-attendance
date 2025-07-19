package udpm.hn.studentattendance.core.teacher.studentattendance.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.studentattendance.model.request.TeacherModifyStudentAttendanceRequest;
import udpm.hn.studentattendance.core.teacher.studentattendance.service.TeacherStudentAttendanceService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherStudentAttendanceRestControllerTest {
    @Mock
    private TeacherStudentAttendanceService service;

    @InjectMocks
    private TeacherStudentAttendanceRestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateBulk() {
        when(service.createAttendance("1")).thenReturn((ResponseEntity) ResponseEntity.ok("created"));
        ResponseEntity<?> res = controller.createBulk("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("created", res.getBody());
    }

    @Test
    void testShow() {
        when(service.getAllByPlanDate("1")).thenReturn((ResponseEntity) ResponseEntity.ok("attendance"));
        ResponseEntity<?> res = controller.show("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("attendance", res.getBody());
    }

    @Test
    void testUpdateStatus() {
        TeacherModifyStudentAttendanceRequest request = new TeacherModifyStudentAttendanceRequest();
        when(service.updateStatusAttendance(request)).thenReturn((ResponseEntity) ResponseEntity.ok("updated"));
        ResponseEntity<?> res = controller.updateStatus(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("updated", res.getBody());
    }
}