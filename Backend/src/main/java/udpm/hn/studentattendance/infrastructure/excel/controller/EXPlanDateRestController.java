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
import udpm.hn.studentattendance.infrastructure.excel.service.EXPlanDateService;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteExcelConstant.URL_API_PLAN_DATE)
public class EXPlanDateRestController implements IEXDefaultController {

    private final EXPlanDateService exPlanDateService;

    @Override
    public ResponseEntity<?> getDataFromFile(EXUploadRequest request) {
        return exPlanDateService.getDataFromFile(request);
    }

    @Override
    public ResponseEntity<?> importItem(EXImportRequest request) {
        return exPlanDateService.importItem(request);
    }

    @Override
    public ResponseEntity<?> downloadTemplate(EXDataRequest request) {
        return exPlanDateService.downloadTemplate(request);
    }

    @Override
    public ResponseEntity<?> historyLog(EXDataRequest request) {
        return exPlanDateService.historyLog(request);
    }

    @Override
    public ResponseEntity<?> historyLogDetail(EXDataRequest request, String id) {
        return exPlanDateService.historyLogDetail(request, id);
    }

}
