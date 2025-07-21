package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.staff.attendancerecovery.service.STAttendanceRecoveryService;
import udpm.hn.studentattendance.core.staff.attendancerecovery.repository.STAttendanceRecoveryRepository;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserActivityLogRepository;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogDetailRepository;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EXAttendanceRecoveryServiceImplTest {
    @Mock
    private EXImportLogRepository importLogRepository;
    @Mock
    private EXImportLogDetailRepository importLogDetailRepository;
    @Mock
    private SessionHelper sessionHelper;
    @Mock
    private ExcelHelper excelHelper;
    @Mock
    private STAttendanceRecoveryService attendanceRecoveryService;
    @Mock
    private STAttendanceRecoveryRepository attendanceRecoveryRepository;
    @Mock
    private UserActivityLogHelper userActivityLogHelper;
    @Mock
    private CommonUserActivityLogRepository userActivityLogRepository;
    @InjectMocks
    private EXAttendanceRecoveryServiceImpl exAttendanceRecoveryService;

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
        var response = exAttendanceRecoveryService.getDataFromFile(request);
        assertNotNull(response);
        ResponseEntity<?> entity = response;
        assertEquals(502, entity.getStatusCodeValue()); // HttpStatus.BAD_GATEWAY
        assertTrue(((udpm.hn.studentattendance.infrastructure.common.ApiResponse) entity.getBody()).getMessage()
                .contains("Vui lòng tải lên file excel"));
    }
}
