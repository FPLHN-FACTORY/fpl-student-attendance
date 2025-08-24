package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.studentfactory.service.impl.USStudentFactoryServiceImpl;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EXStudentFactoryServiceImplTest {
    @Mock
    private USStudentFactoryServiceImpl usStudentFactoryService;
    @Mock
    private ExcelHelper excelHelper;
    @InjectMocks
    private EXStudentFactoryServiceImpl exStudentFactoryService;

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
        var response = exStudentFactoryService.getDataFromFile(request);
        assertNotNull(response);
        ResponseEntity<?> entity = response;
        assertEquals(502, entity.getStatusCodeValue()); // HttpStatus.BAD_GATEWAY
        assertTrue(((udpm.hn.studentattendance.infrastructure.common.ApiResponse) entity.getBody()).getMessage()
                .contains("Vui lòng tải lên file Excel"));
    }

    // Thêm các test case khác nếu cần thiết
}
