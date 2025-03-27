package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;
import udpm.hn.studentattendance.infrastructure.excel.service.ExFactoryService;

public class ExFactoryServiceImpl implements ExFactoryService {
    @Override
    public ResponseEntity<?> getDataFromFile(EXUploadRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<?> importItem(EXImportRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<?> downloadTemplate(EXDataRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<?> historyLog(EXDataRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<?> historyLogDetail(EXDataRequest request, String id) {
        return null;
    }
}
