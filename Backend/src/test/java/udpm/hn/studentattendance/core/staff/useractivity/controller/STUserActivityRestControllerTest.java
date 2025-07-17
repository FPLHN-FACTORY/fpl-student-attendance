package udpm.hn.studentattendance.core.staff.useractivity.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.useractivity.service.STUserActivityService;
import udpm.hn.studentattendance.infrastructure.common.model.request.UALFilterRequest;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class STUserActivityRestControllerTest {
    @Mock
    private STUserActivityService stUserActivityService;

    @InjectMocks
    private STUserActivityRestController stUserActivityRestController;

    @Test
    void testSTUserActivityRestControllerExists() {
        assertNotNull(stUserActivityRestController);
    }

        @Test
    void testGetAllUserLogActivity() {
        UALFilterRequest request = new UALFilterRequest();
        when(stUserActivityService.getAllUserActivity(request)).thenReturn(ResponseEntity.ok().build());
        
        ResponseEntity<?> response = stUserActivityRestController.getAllUserLogActivity(request);
        
        assertNotNull(response);
        verify(stUserActivityService).getAllUserActivity(request);
    }

    @Test
    void testGetAllStaffByFacility() {
        when(stUserActivityService.getAllUserStaff()).thenReturn(ResponseEntity.ok().build());
        
        ResponseEntity<?> response = stUserActivityRestController.getAllStaffByFacility();
        
        assertNotNull(response);
        verify(stUserActivityService).getAllUserStaff();
    }
}