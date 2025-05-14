package udpm.hn.studentattendance.infrastructure.excel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteExcelConstant;
import udpm.hn.studentattendance.infrastructure.excel.common.IEXDefaultController;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;
import udpm.hn.studentattendance.infrastructure.excel.service.EXStudentFactoryService;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteExcelConstant.URL_API_STUDENT_FACTORY)
public class EXStudentFactoryRestController implements IEXDefaultController {

    private final EXStudentFactoryService exStudentFactoryService;

    @Override
    public ResponseEntity<?> getDataFromFile(EXUploadRequest request) {
        return exStudentFactoryService.getDataFromFile(request);
    }

    @Override
    public ResponseEntity<?> importItem(EXImportRequest request) {
        return exStudentFactoryService.importItem(request);
    }

    @Override
    public ResponseEntity<?> exportData(EXDataRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<?> downloadTemplate(EXDataRequest request) {
        return exStudentFactoryService.downloadTemplate(request);
    }

    @Override
    public ResponseEntity<?> historyLog(EXDataRequest request) {
        return exStudentFactoryService.historyLog(request);
    }

    @Override
    public ResponseEntity<?> historyLogDetail(EXDataRequest request, String id) {
        return exStudentFactoryService.historyLogDetail(request, id);
    }
}
