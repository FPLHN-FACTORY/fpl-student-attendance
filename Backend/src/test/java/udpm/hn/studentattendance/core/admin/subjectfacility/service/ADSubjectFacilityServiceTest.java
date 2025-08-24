package udpm.hn.studentattendance.core.admin.subjectfacility.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilityCreateRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilityUpdateRequest;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ADSubjectFacilityServiceTest {
    @Mock
    private ADSubjectFacilityService adSubjectFacilityService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // Mock the service methods to return proper responses
        ApiResponse mockResponse = ApiResponse.success("Success", "Test data");
        ResponseEntity responseEntity = ResponseEntity.ok(mockResponse);

        lenient().when(adSubjectFacilityService.getListSubjectFacility(any(ADSubjectFacilitySearchRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(adSubjectFacilityService.createSubjectFacility(any(ADSubjectFacilityCreateRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(adSubjectFacilityService.updateSubjectFacility(any(String.class),
                any(ADSubjectFacilityUpdateRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(adSubjectFacilityService.detailSubjectFacility(any(String.class)))
                .thenReturn(responseEntity);
        lenient().when(adSubjectFacilityService.changeStatus(any(String.class)))
                .thenReturn(responseEntity);
    }

    @Test
    void testADSubjectFacilityServiceExists() {
        assertNotNull(adSubjectFacilityService);
    }

    @Test
    void testGetListSubjectFacility() {
        ADSubjectFacilitySearchRequest request = new ADSubjectFacilitySearchRequest();
        ResponseEntity<?> response = adSubjectFacilityService.getListSubjectFacility(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateSubjectFacility() {
        ADSubjectFacilityCreateRequest request = new ADSubjectFacilityCreateRequest();
        ResponseEntity<?> response = adSubjectFacilityService.createSubjectFacility(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateSubjectFacility() {
        String id = "123";
        ADSubjectFacilityUpdateRequest request = new ADSubjectFacilityUpdateRequest();
        ResponseEntity<?> response = adSubjectFacilityService.updateSubjectFacility(id, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testDetailSubjectFacility() {
        String id = "123";
        ResponseEntity<?> response = adSubjectFacilityService.detailSubjectFacility(id);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testChangeStatus() {
        String id = "123";
        ResponseEntity<?> response = adSubjectFacilityService.changeStatus(id);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
}
