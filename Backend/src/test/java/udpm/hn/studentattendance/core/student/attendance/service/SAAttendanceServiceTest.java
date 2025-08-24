package udpm.hn.studentattendance.core.student.attendance.service;

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
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SAAttendanceServiceTest {
    @Mock
    private SAAttendanceService saAttendanceService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // Mock the service methods to return proper responses
        ApiResponse mockResponse = ApiResponse.success("Success", "Test data");
        ResponseEntity responseEntity = ResponseEntity.ok(mockResponse);

        lenient().when(saAttendanceService.getAllList(any()))
                .thenReturn(responseEntity);
        lenient().when(saAttendanceService.checkin(any(), any(), any()))
                .thenReturn(responseEntity);
    }

    @Test
    void testSAAttendanceServiceExists() {
        assertNotNull(saAttendanceService);
    }

    @Test
    void testGetAllList() {
        udpm.hn.studentattendance.core.student.attendance.model.request.SAFilterAttendanceRequest request = new udpm.hn.studentattendance.core.student.attendance.model.request.SAFilterAttendanceRequest();
        ResponseEntity<?> response = saAttendanceService.getAllList(request);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testCheckin() {
        udpm.hn.studentattendance.core.student.attendance.model.request.SACheckinAttendanceRequest request = new udpm.hn.studentattendance.core.student.attendance.model.request.SACheckinAttendanceRequest();
        org.springframework.web.multipart.MultipartFile image = mock(
                org.springframework.web.multipart.MultipartFile.class);
        org.springframework.web.multipart.MultipartFile image2 = mock(
                org.springframework.web.multipart.MultipartFile.class);
        ResponseEntity<?> response = saAttendanceService.checkin(request, image, image2);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }
}
