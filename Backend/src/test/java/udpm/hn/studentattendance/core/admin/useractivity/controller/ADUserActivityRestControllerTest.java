package udpm.hn.studentattendance.core.admin.useractivity.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.useractivity.service.ADUserActivityService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ADUserActivityRestControllerTest {
    @Mock
    private ADUserActivityService adUserActivityService;

    @InjectMocks
    private ADUserActivityRestController adUserActivityRestController;

    @Test
    void testADUserActivityRestControllerExists() {
        assertNotNull(adUserActivityRestController);
    }

    @Test
    void testGetAllUserLogActivity() {
        var request = mock(udpm.hn.studentattendance.infrastructure.common.model.request.UALFilterRequest.class);
        when(adUserActivityService.getAllUserActivity(request)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adUserActivityRestController.getAllUserLogActivity(request);
        assertNotNull(response);
        verify(adUserActivityService).getAllUserActivity(request);
    }

    @Test
    void testGetAllStaff() {
        when(adUserActivityService.getAllUserStaff()).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adUserActivityRestController.getAllStaff();
        assertNotNull(response);
        verify(adUserActivityService).getAllUserStaff();
    }

    @Test
    void testGetAllAdmin() {
        when(adUserActivityService.getAllUserAdmin()).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adUserActivityRestController.getAllAdmin();
        assertNotNull(response);
        verify(adUserActivityService).getAllUserAdmin();
    }

    @Test
    void testGetAllFacility() {
        when(adUserActivityService.getAllFacility()).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adUserActivityRestController.getAllFacility();
        assertNotNull(response);
        verify(adUserActivityService).getAllFacility();
    }
}