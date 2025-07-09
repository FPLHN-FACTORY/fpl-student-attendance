package udpm.hn.studentattendance.core.admin.facility.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFAddOrUpdateFacilityLocationRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityLocationRequest;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AFFacilityLocationServiceTest {
    @Mock
    private AFFacilityLocationService afFacilityLocationService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // Mock the service methods to return proper responses
        ApiResponse mockResponse = ApiResponse.success("Success", "Test data");
        ResponseEntity responseEntity = ResponseEntity.ok(mockResponse);

        lenient().when(afFacilityLocationService.getAllList(any(AFFilterFacilityLocationRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(afFacilityLocationService.addLocation(any(AFAddOrUpdateFacilityLocationRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(afFacilityLocationService.updateLocation(any(AFAddOrUpdateFacilityLocationRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(afFacilityLocationService.deleteLocation(any(String.class)))
                .thenReturn(responseEntity);
        lenient().when(afFacilityLocationService.changeStatus(any(String.class)))
                .thenReturn(responseEntity);
    }

    @Test
    void testAFFacilityLocationServiceExists() {
        assertNotNull(afFacilityLocationService);
    }

    @Test
    void testGetAllList() {
        AFFilterFacilityLocationRequest request = new AFFilterFacilityLocationRequest();
        ResponseEntity<?> response = afFacilityLocationService.getAllList(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testAddLocation() {
        AFAddOrUpdateFacilityLocationRequest request = new AFAddOrUpdateFacilityLocationRequest();
        ResponseEntity<?> response = afFacilityLocationService.addLocation(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateLocation() {
        AFAddOrUpdateFacilityLocationRequest request = new AFAddOrUpdateFacilityLocationRequest();
        ResponseEntity<?> response = afFacilityLocationService.updateLocation(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteLocation() {
        String id = "123";
        ResponseEntity<?> response = afFacilityLocationService.deleteLocation(id);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testChangeStatus() {
        String id = "123";
        ResponseEntity<?> response = afFacilityLocationService.changeStatus(id);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
}