package udpm.hn.studentattendance.infrastructure.excel.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteExcelConstant;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;

public interface IEXDefaultController {

    @PostMapping(RouteExcelConstant.DEFAULT_UPLOAD)
    ResponseEntity<?> getDataFromFile(@ModelAttribute EXUploadRequest request);

    @PostMapping(RouteExcelConstant.DEFAULT_IMPORT)
    ResponseEntity<?> importItem(@RequestBody EXImportRequest request);

    @PostMapping(RouteExcelConstant.DEFAULT_DOWNLOAD_TEMPLATE)
    ResponseEntity<?> downloadTemplate(@RequestBody EXDataRequest request);

    @PostMapping(RouteExcelConstant.DEFAULT_HISTORY_LOG)
    ResponseEntity<?> historyLog(@RequestBody EXDataRequest request);

    @PostMapping(RouteExcelConstant.DEFAULT_HISTORY_LOG + "/{id}")
    ResponseEntity<?> historyLogDetail(@RequestBody EXDataRequest request, @PathVariable String id);

}
