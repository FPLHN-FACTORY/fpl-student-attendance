package udpm.hn.studentattendance.infrastructure.excel.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;
import udpm.hn.studentattendance.infrastructure.excel.service.EXStudentService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EXStudentRestControllerTest {
    @Mock
    private EXStudentService exStudentService;

    @InjectMocks
    private EXStudentRestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDataFromFile() {
        EXUploadRequest request = new EXUploadRequest();
        when(exStudentService.getDataFromFile(request)).thenReturn((ResponseEntity) ResponseEntity.ok("data"));
        ResponseEntity<?> res = controller.getDataFromFile(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("data", res.getBody());
    }

    @Test
    void testImportItem() {
        EXImportRequest request = new EXImportRequest();
        when(exStudentService.importItem(request)).thenReturn((ResponseEntity) ResponseEntity.ok("imported"));
        ResponseEntity<?> res = controller.importItem(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("imported", res.getBody());
    }

    @Test
    void testExportData() {
        EXDataRequest request = new EXDataRequest();
        when(exStudentService.exportData(request)).thenReturn((ResponseEntity) ResponseEntity.ok("exported"));
        ResponseEntity<?> res = controller.exportData(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("exported", res.getBody());
    }

    @Test
    void testDownloadTemplate() {
        EXDataRequest request = new EXDataRequest();
        when(exStudentService.downloadTemplate(request)).thenReturn((ResponseEntity) ResponseEntity.ok("template"));
        ResponseEntity<?> res = controller.downloadTemplate(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("template", res.getBody());
    }

    @Test
    void testHistoryLog() {
        EXDataRequest request = new EXDataRequest();
        when(exStudentService.historyLog(request)).thenReturn((ResponseEntity) ResponseEntity.ok("history"));
        ResponseEntity<?> res = controller.historyLog(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("history", res.getBody());
    }

    @Test
    void testHistoryLogDetail() {
        EXDataRequest request = new EXDataRequest();
        when(exStudentService.historyLogDetail(request, "1")).thenReturn((ResponseEntity) ResponseEntity.ok("detail"));
        ResponseEntity<?> res = controller.historyLogDetail(request, "1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("detail", res.getBody());
    }
}
