package udpm.hn.studentattendance.core.admin.levelproject.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectCreateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectSearchRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectUpdateRequest;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ADLevelProjectManagementServiceTest {
    @Mock
    private ADLevelProjectManagementService adLevelProjectManagementService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // Mock the service methods to return proper responses
        ApiResponse mockResponse = ApiResponse.success("Success", "Test data");
        ResponseEntity responseEntity = ResponseEntity.ok(mockResponse);

        lenient().when(adLevelProjectManagementService.getListLevelProject(any(ADLevelProjectSearchRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(adLevelProjectManagementService.createLevelProject(any(ADLevelProjectCreateRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(adLevelProjectManagementService.updateLevelProject(any(String.class),
                any(ADLevelProjectUpdateRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(adLevelProjectManagementService.detailLevelProject(any(String.class)))
                .thenReturn(responseEntity);
        lenient().when(adLevelProjectManagementService.changeStatus(any(String.class)))
                .thenReturn(responseEntity);
    }

    @Test
    void testADLevelProjectManagementServiceExists() {
        assertNotNull(adLevelProjectManagementService);
    }

    @Test
    void testGetListLevelProject() {
        ADLevelProjectSearchRequest request = new ADLevelProjectSearchRequest();
        ResponseEntity<?> response = adLevelProjectManagementService.getListLevelProject(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateLevelProject() {
        ADLevelProjectCreateRequest request = new ADLevelProjectCreateRequest();
        ResponseEntity<?> response = adLevelProjectManagementService.createLevelProject(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateLevelProject() {
        String id = "123";
        ADLevelProjectUpdateRequest request = new ADLevelProjectUpdateRequest();
        ResponseEntity<?> response = adLevelProjectManagementService.updateLevelProject(id, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testDetailLevelProject() {
        String id = "123";
        ResponseEntity<?> response = adLevelProjectManagementService.detailLevelProject(id);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testChangeStatus() {
        String id = "123";
        ResponseEntity<?> response = adLevelProjectManagementService.changeStatus(id);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
}
