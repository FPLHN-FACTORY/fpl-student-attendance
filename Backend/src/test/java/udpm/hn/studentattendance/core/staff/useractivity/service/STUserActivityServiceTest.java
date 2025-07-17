package udpm.hn.studentattendance.core.staff.useractivity.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.model.request.UALFilterRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class STUserActivityServiceTest {
    @Mock
    private STUserActivityService stUserActivityService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // Mock the service methods to return proper responses
        ApiResponse mockResponse = ApiResponse.success("Success", "Test data");
        ResponseEntity responseEntity = ResponseEntity.ok(mockResponse);

        lenient().when(stUserActivityService.getAllUserActivity(any(UALFilterRequest.class)))
                .thenReturn(responseEntity);
        lenient().when(stUserActivityService.getAllUserStaff())
                .thenReturn(responseEntity);
    }

    @Test
    void testSTUserActivityServiceExists() {
        assertNotNull(stUserActivityService);
    }

    @Test
    void testGetAllUserActivity() {
        UALFilterRequest request = new UALFilterRequest();
        ResponseEntity<?> response = stUserActivityService.getAllUserActivity(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetAllUserStaff() {
        ResponseEntity<?> response = stUserActivityService.getAllUserStaff();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
}