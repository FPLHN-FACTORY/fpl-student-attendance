package udpm.hn.studentattendance.infrastructure.excel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteExcelConstant;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;
import udpm.hn.studentattendance.infrastructure.excel.service.EXPlanDateService;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteExcelConstant.URL_API_PLAN_DATE)
public class EXPlanDateRestController {

    private final EXPlanDateService exPlanDateService;

    @PostMapping(RouteExcelConstant.DEFAULT_UPLOAD)
    private ResponseEntity<?> getDataFromFile(@ModelAttribute EXUploadRequest request) {
        return exPlanDateService.getDataFromFile(request);
    }

    @PostMapping(RouteExcelConstant.DEFAULT_IMPORT)
    private ResponseEntity<?> importItem(@ModelAttribute EXImportRequest request) {
        return exPlanDateService.importItem(request);
    }

    @GetMapping(RouteExcelConstant.DEFAULT_DOWNLOAD_TEMPLATE)
    private ResponseEntity<?> downloadTemplate() {
        return exPlanDateService.downloadTemplate();
    }

}
