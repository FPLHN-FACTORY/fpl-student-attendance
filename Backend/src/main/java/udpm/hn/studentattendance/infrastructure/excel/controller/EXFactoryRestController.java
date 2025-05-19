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
import udpm.hn.studentattendance.infrastructure.excel.service.EXFactoryService;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteExcelConstant.URL_API_FACTORY)
public class EXFactoryRestController implements IEXDefaultController {

    private final EXFactoryService factoryService;

    @Override
    public ResponseEntity<?> getDataFromFile(EXUploadRequest request) {
        return factoryService.getDataFromFile(request);
    }

    @Override
    public ResponseEntity<?> importItem(EXImportRequest request) {

        return factoryService.importItem(request);
    }

    @Override
    public ResponseEntity<?> exportData(EXDataRequest request) {
        return factoryService.exportData(request);
    }

    @Override
    public ResponseEntity<?> downloadTemplate(EXDataRequest request) {

        return factoryService.downloadTemplate(request);
    }

    @Override
    public ResponseEntity<?> historyLog(EXDataRequest request) {
        return factoryService.historyLog(request);
    }

    @Override
    public ResponseEntity<?> historyLogDetail(EXDataRequest request, String id) {

        return factoryService.historyLogDetail(request, id);
    }
}
