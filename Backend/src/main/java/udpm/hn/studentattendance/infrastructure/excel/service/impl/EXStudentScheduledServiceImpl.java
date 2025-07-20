package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogDetailRepository;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogRepository;
import udpm.hn.studentattendance.infrastructure.excel.service.EXStudentScheduledService;

@Service
@RequiredArgsConstructor
public class EXStudentScheduledServiceImpl implements EXStudentScheduledService {

    private final EXImportLogRepository importLogRepository;

    private final EXImportLogDetailRepository importLogDetailRepository;

    private final SessionHelper sessionHelper;

    private final ExcelHelper excelHelper;


    @Override
    public ResponseEntity<?> getDataFromFile(EXUploadRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<?> importItem(EXImportRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<?> exportData(EXDataRequest request) {

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
