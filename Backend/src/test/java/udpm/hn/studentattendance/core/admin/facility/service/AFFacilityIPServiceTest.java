package udpm.hn.studentattendance.core.admin.facility.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFAddOrUpdateFacilityIPRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityIPRequest;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AFFacilityIPServiceTest {
    @Mock
    private AFFacilityIPService afFacilityIPService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // Mock the service methods to return proper responses
        ApiResponse mockResponse = ApiResponse.success("Success", "Test data");
        ResponseEntity responseEntity = ResponseEntity.ok(mockResponse);

        lenient().when(afFacilityIPService.getAllList(any(AFFilterFacilityIPRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(afFacilityIPService.addIP(any(AFAddOrUpdateFacilityIPRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(afFacilityIPService.updateIP(any(AFAddOrUpdateFacilityIPRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(afFacilityIPService.deleteIP(any(String.class)))
                .thenReturn(responseEntity);
        lenient().when(afFacilityIPService.changeStatus(any(String.class)))
                .thenReturn(responseEntity);
    }

    @Test
    void testAFFacilityIPServiceExists() {
        assertNotNull(afFacilityIPService);
    }

    @Test
    void testGetAllList() {
        AFFilterFacilityIPRequest request = new AFFilterFacilityIPRequest();
        ResponseEntity<?> response = afFacilityIPService.getAllList(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testAddIP() {
        AFAddOrUpdateFacilityIPRequest request = new AFAddOrUpdateFacilityIPRequest();
        ResponseEntity<?> response = afFacilityIPService.addIP(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateIP() {
        AFAddOrUpdateFacilityIPRequest request = new AFAddOrUpdateFacilityIPRequest();
        ResponseEntity<?> response = afFacilityIPService.updateIP(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteIP() {
        String id = "123";
        ResponseEntity<?> response = afFacilityIPService.deleteIP(id);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testChangeStatus() {
        String id = "123";
        ResponseEntity<?> response = afFacilityIPService.changeStatus(id);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
}
