package udpm.hn.studentattendance.infrastructure.excel.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;

public interface EXPlanDateService {

    ResponseEntity<?> getDataFromFile(EXUploadRequest request);

    ResponseEntity<?> importItem(EXImportRequest request);

    ResponseEntity<?> downloadTemplate();

}
