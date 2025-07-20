package udpm.hn.studentattendance.core.admin.semester.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.semester.model.request.ADCreateUpdateSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.service.ADSemesterService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ADSemesterRestControllerTest {
    @Mock
    private ADSemesterService adSemesterService;

    @InjectMocks
    private ADSemesterRestController adSemesterRestController;

    @Test
    void testADSemesterRestControllerExists() {
        assertNotNull(adSemesterRestController);
    }

    @Test
    void testGetSemesterById() {
        String semesterId = "123";
        when(adSemesterService.getSemesterById(semesterId)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adSemesterRestController.getSemesterById(semesterId);
        assertNotNull(response);
        verify(adSemesterService).getSemesterById(semesterId);
    }

    @Test
    void testCreateSemester() {
        ADCreateUpdateSemesterRequest semesterRequest = new ADCreateUpdateSemesterRequest();
        when(adSemesterService.createSemester(semesterRequest)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adSemesterRestController.createSemester(semesterRequest);
        assertNotNull(response);
        verify(adSemesterService).createSemester(semesterRequest);
    }

    @Test
    void testUpdateSemester() {
        ADCreateUpdateSemesterRequest semesterRequest = new ADCreateUpdateSemesterRequest();
        when(adSemesterService.updateSemester(semesterRequest)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adSemesterRestController.updateSemester(semesterRequest);
        assertNotNull(response);
        verify(adSemesterService).updateSemester(semesterRequest);
    }

    @Test
    void testChangeStatusSemester() {
        String semesterId = "123";
        when(adSemesterService.changeStatusSemester(semesterId)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adSemesterRestController.changeStatusSemester(semesterId);
        assertNotNull(response);
        verify(adSemesterService).changeStatusSemester(semesterId);
    }
}
