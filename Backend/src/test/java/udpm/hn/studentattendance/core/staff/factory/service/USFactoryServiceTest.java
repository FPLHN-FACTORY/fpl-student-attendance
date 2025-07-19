package udpm.hn.studentattendance.core.staff.factory.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryRequest;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class USFactoryServiceTest {
    @Mock
    private USFactoryService usFactoryService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // Mock the service methods to return proper responses
        ApiResponse mockResponse = ApiResponse.success("Success", "Test data");
        ResponseEntity responseEntity = ResponseEntity.ok(mockResponse);

        lenient().when(usFactoryService.getAllFactory(any(USFactoryRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(usFactoryService.getDetailFactory(any(String.class)))
                .thenReturn(responseEntity);
        lenient().when(usFactoryService.createFactory(any(USFactoryCreateUpdateRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(usFactoryService.updateFactory(any(USFactoryCreateUpdateRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(usFactoryService.changeStatus(any(String.class)))
                .thenReturn(responseEntity);
    }

    @Test
    void testUSFactoryServiceExists() {
        assertNotNull(usFactoryService);
    }

    @Test
    void testGetAllFactory() {
        USFactoryRequest request = new USFactoryRequest();
        ResponseEntity<?> response = usFactoryService.getAllFactory(request);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetDetailFactory() {
        String factoryId = "123";
        ResponseEntity<?> response = usFactoryService.getDetailFactory(factoryId);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateFactory() {
        USFactoryCreateUpdateRequest factoryRequest = new USFactoryCreateUpdateRequest();
        ResponseEntity<?> response = usFactoryService.createFactory(factoryRequest);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateFactory() {
        USFactoryCreateUpdateRequest factoryRequest = new USFactoryCreateUpdateRequest();
        ResponseEntity<?> response = usFactoryService.updateFactory(factoryRequest);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testChangeStatus() {
        String factoryId = "123";
        ResponseEntity<?> response = usFactoryService.changeStatus(factoryId);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
}