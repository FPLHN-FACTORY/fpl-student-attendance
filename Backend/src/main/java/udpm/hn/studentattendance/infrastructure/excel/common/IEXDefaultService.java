package udpm.hn.studentattendance.infrastructure.excel.common;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;

public interface IEXDefaultService {

    ResponseEntity<?> getDataFromFile(EXUploadRequest request);

    ResponseEntity<?> importItem(EXImportRequest request);

    ResponseEntity<?> exportData(EXDataRequest request);

    ResponseEntity<?> downloadTemplate(EXDataRequest request);

    ResponseEntity<?> historyLog(EXDataRequest request);

    ResponseEntity<?> historyLogDetail(EXDataRequest request, String id);

}
