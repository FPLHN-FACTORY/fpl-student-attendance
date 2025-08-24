package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.staff.plan.services.impl.SPDPlanDateAttendanceServiceImpl;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EXPlanDateServiceImplTest {
    @Mock
    private SPDPlanDateAttendanceServiceImpl spdPlanDateAttendanceService;
    @Mock
    private ExcelHelper excelHelper;
    @InjectMocks
    private EXPlanDateServiceImpl exPlanDateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDataFromFile_EmptyFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        EXUploadRequest request = mock(EXUploadRequest.class);
        when(request.getFile()).thenReturn(file);
        var response = exPlanDateService.getDataFromFile(request);
        assertNotNull(response);
        assertEquals(502, response.getStatusCodeValue()); // HttpStatus.BAD_GATEWAY
        assertTrue(response.getBody().getMessage().contains("Vui lòng tải lên file Excel"));
    }

    // Thêm các test case khác nếu cần thiết
}
