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
import udpm.hn.studentattendance.infrastructure.excel.service.EXStaffService;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteExcelConstant.URL_API_STAFF)
public class EXStaffRestController implements IEXDefaultController {

    private final EXStaffService service;

    @Override
    public ResponseEntity<?> getDataFromFile(EXUploadRequest request) {
        return service.getDataFromFile(request);
    }

    @Override
    public ResponseEntity<?> importItem(EXImportRequest request) {
        return service.importItem(request);
    }

    @Override
    public ResponseEntity<?> downloadTemplate(EXDataRequest request) {
        return service.downloadTemplate(request);
    }

    @Override
    public ResponseEntity<?> historyLog(EXDataRequest request) {
        return service.historyLog(request);
    }

    @Override
    public ResponseEntity<?> historyLogDetail(EXDataRequest request, String id) {
        return service.historyLogDetail(request, id);
    }
}
