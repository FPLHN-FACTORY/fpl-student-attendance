package udpm.hn.studentattendance.core.admin.userstaff.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.userstaff.service.ADStaffService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.core.admin.userstaff.model.request.ADCreateUpdateStaffRequest;
import udpm.hn.studentattendance.core.admin.userstaff.model.request.ADStaffRequest;

@ExtendWith(MockitoExtension.class)
class ADStaffRestControllerTest {
    @Mock
    private ADStaffService adStaffService;

    @InjectMocks
    private ADStaffRestController adStaffRestController;

    @Test
    void testADStaffRestControllerExists() {
        assertNotNull(adStaffRestController);
    }

    @Test
    void testGetAllStaffs() {
        ADStaffRequest staffRequest = new ADStaffRequest();
        when(adStaffService.getAllStaffByFilter(staffRequest)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = adStaffRestController.getAllStaffs(staffRequest);

        assertNotNull(response);
        verify(adStaffService).getAllStaffByFilter(staffRequest);
    }

    @Test
    void testGetStaffById() {
        String staffId = "123";
        when(adStaffService.getStaffById(staffId)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = adStaffRestController.getStaffById(staffId);

        assertNotNull(response);
        verify(adStaffService).getStaffById(staffId);
    }

    @Test
    void testCreateStaff() {
        ADCreateUpdateStaffRequest staffRequest = new ADCreateUpdateStaffRequest();
        when(adStaffService.createStaff(staffRequest)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = adStaffRestController.createStaff(staffRequest);

        assertNotNull(response);
        verify(adStaffService).createStaff(staffRequest);
    }

    @Test
    void testUpdateStaff() {
        String staffId = "123";
        ADCreateUpdateStaffRequest staffRequest = new ADCreateUpdateStaffRequest();
        when(adStaffService.updateStaff(staffRequest, staffId)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = adStaffRestController.updateStaff(staffRequest, staffId);

        assertNotNull(response);
        verify(adStaffService).updateStaff(staffRequest, staffId);
    }

    @Test
    void testDeleteStaff() {
        // Controller không có method deleteStaff, nên bỏ test này nếu không có trong
        // controller thực tế
    }
}
