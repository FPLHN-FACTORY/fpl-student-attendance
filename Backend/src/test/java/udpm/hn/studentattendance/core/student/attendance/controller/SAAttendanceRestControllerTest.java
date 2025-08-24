package udpm.hn.studentattendance.core.student.attendance.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.student.attendance.service.SAAttendanceService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SAAttendanceRestControllerTest {
    @Mock
    private SAAttendanceService saAttendanceService;

    @InjectMocks
    private SAAttendanceRestController saAttendanceRestController;

    @Test
    void testSAAttendanceRestControllerExists() {
        assertNotNull(saAttendanceRestController);
    }

    @Test
    void testGetAllList() {
        udpm.hn.studentattendance.core.student.attendance.model.request.SAFilterAttendanceRequest request = mock(
                udpm.hn.studentattendance.core.student.attendance.model.request.SAFilterAttendanceRequest.class);
        when(saAttendanceService.getAllList(request)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = saAttendanceRestController.getAllList(request);
        assertNotNull(response);
        verify(saAttendanceService).getAllList(request);
    }

    @Test
    void testCheckin() {
        udpm.hn.studentattendance.core.student.attendance.model.request.SACheckinAttendanceRequest request = mock(
                udpm.hn.studentattendance.core.student.attendance.model.request.SACheckinAttendanceRequest.class);
        org.springframework.web.multipart.MultipartFile image = mock(
                org.springframework.web.multipart.MultipartFile.class);
        org.springframework.web.multipart.MultipartFile image2 = mock(
                org.springframework.web.multipart.MultipartFile.class);
        String signature = "test-signature";
        when(saAttendanceService.checkin(request, image, image2)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = saAttendanceRestController.checkin(request, image, image2, signature);
        assertNotNull(response);
        verify(saAttendanceService).checkin(request, image, image2);
    }
}
