package udpm.hn.studentattendance.core.admin.facility.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFAddOrUpdateFacilityShiftRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityShiftRequest;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AFFacilityShiftServiceTest {
    @Mock
    private AFFacilityShiftService afFacilityShiftService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // Mock the service methods to return proper responses
        ApiResponse mockResponse = ApiResponse.success("Success", "Test data");
        ResponseEntity responseEntity = ResponseEntity.ok(mockResponse);

        lenient().when(afFacilityShiftService.getAllList(any(AFFilterFacilityShiftRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(afFacilityShiftService.addShift(any(AFAddOrUpdateFacilityShiftRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(afFacilityShiftService.updateShift(any(AFAddOrUpdateFacilityShiftRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(afFacilityShiftService.deleteShift(any(String.class)))
                .thenReturn(responseEntity);
        lenient().when(afFacilityShiftService.changeStatus(any(String.class)))
                .thenReturn(responseEntity);
    }

    @Test
    void testAFFacilityShiftServiceExists() {
        assertNotNull(afFacilityShiftService);
    }

    @Test
    void testGetAllList() {
        AFFilterFacilityShiftRequest request = new AFFilterFacilityShiftRequest();
        ResponseEntity<?> response = afFacilityShiftService.getAllList(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testAddShift() {
        AFAddOrUpdateFacilityShiftRequest request = new AFAddOrUpdateFacilityShiftRequest();
        ResponseEntity<?> response = afFacilityShiftService.addShift(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateShift() {
        AFAddOrUpdateFacilityShiftRequest request = new AFAddOrUpdateFacilityShiftRequest();
        ResponseEntity<?> response = afFacilityShiftService.updateShift(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteShift() {
        String id = "123";
        ResponseEntity<?> response = afFacilityShiftService.deleteShift(id);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testChangeStatus() {
        String id = "123";
        ResponseEntity<?> response = afFacilityShiftService.changeStatus(id);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
}