package udpm.hn.studentattendance.core.staff.attendancerecovery.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.attendancerecovery.service.STAttendanceRecoveryService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class USAttendanceRecoveryRestControllerTest {
    @Mock
    private STAttendanceRecoveryService usAttendanceRecoveryService;

    @InjectMocks
    private STAttendanceRecoveryController usAttendanceRecoveryRestController;

    @Test
    void testUSAttendanceRecoveryRestControllerExists() {
        assertNotNull(usAttendanceRecoveryRestController);
    }

    @Test
    void testGetListAttendanceRecovery() {
        udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STAttendanceRecoveryRequest request = mock(
                udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STAttendanceRecoveryRequest.class);
        when(usAttendanceRecoveryService.getListAttendanceRecovery(request)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usAttendanceRecoveryRestController.getListAttendanceRecovery(request);
        assertNotNull(response);
        verify(usAttendanceRecoveryService).getListAttendanceRecovery(request);
    }

    @Test
    void testGetDetailEventAttendanceRecovery() {
        String recoveryId = "123";
        when(usAttendanceRecoveryService.getDetailEventAttendanceRecovery(recoveryId))
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usAttendanceRecoveryRestController.getDetailEventAttendanceRecovery(recoveryId);
        assertNotNull(response);
        verify(usAttendanceRecoveryService).getDetailEventAttendanceRecovery(recoveryId);
    }

    @Test
    void testCreateNewEvent() {
        udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STCreateOrUpdateNewEventRequest request = mock(
                udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STCreateOrUpdateNewEventRequest.class);
        when(usAttendanceRecoveryService.createNewEventAttendanceRecovery(request))
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usAttendanceRecoveryRestController.createNewEvent(request);
        assertNotNull(response);
        verify(usAttendanceRecoveryService).createNewEventAttendanceRecovery(request);
    }

    @Test
    void testUpdateEvent() {
        String recoveryId = "123";
        udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STCreateOrUpdateNewEventRequest request = mock(
                udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STCreateOrUpdateNewEventRequest.class);
        when(usAttendanceRecoveryService.updateEventAttendanceRecovery(request, recoveryId))
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usAttendanceRecoveryRestController.updateEvent(request, recoveryId);
        assertNotNull(response);
        verify(usAttendanceRecoveryService).updateEventAttendanceRecovery(request, recoveryId);
    }

    @Test
    void testDeleteAttendanceRecovery() {
        String recoveryId = "123";
        when(usAttendanceRecoveryService.deleteAttendanceRecovery(recoveryId)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usAttendanceRecoveryRestController.deleteAttendanceRecovery(recoveryId);
        assertNotNull(response);
        verify(usAttendanceRecoveryService).deleteAttendanceRecovery(recoveryId);
    }
}
