package udpm.hn.studentattendance.core.admin.userstaff.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import udpm.hn.studentattendance.core.admin.userstaff.model.request.ADCreateUpdateStaffRequest;
import udpm.hn.studentattendance.core.admin.userstaff.model.request.ADStaffRequest;

@ExtendWith(MockitoExtension.class)
class ADStaffServiceTest {
    @Mock
    private ADStaffService adStaffService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // Mock the service methods to return proper responses
        ApiResponse mockResponse = ApiResponse.success("Success", "Test data");
        ResponseEntity responseEntity = ResponseEntity.ok(mockResponse);

        lenient().when(adStaffService.getAllStaffByFilter(any(ADStaffRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(adStaffService.getStaffById(any(String.class)))
                .thenReturn(responseEntity);
        lenient().when(adStaffService.createStaff(any(ADCreateUpdateStaffRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(adStaffService.updateStaff(any(ADCreateUpdateStaffRequest.class), any(String.class)))
                .thenReturn(responseEntity);
    }

    @Test
    void testADStaffServiceExists() {
        assertNotNull(adStaffService);
    }

    @Test
    void testGetAllStaff() {
        ADStaffRequest staffRequest = new ADStaffRequest();
        ResponseEntity<?> response = adStaffService.getAllStaffByFilter(staffRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetStaffById() {
        String staffId = "123";
        ResponseEntity<?> response = adStaffService.getStaffById(staffId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateStaff() {
        ADCreateUpdateStaffRequest staffRequest = new ADCreateUpdateStaffRequest();
        ResponseEntity<?> response = adStaffService.createStaff(staffRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateStaff() {
        String staffId = "123";
        ADCreateUpdateStaffRequest staffRequest = new ADCreateUpdateStaffRequest();
        ResponseEntity<?> response = adStaffService.updateStaff(staffRequest, staffId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
}
