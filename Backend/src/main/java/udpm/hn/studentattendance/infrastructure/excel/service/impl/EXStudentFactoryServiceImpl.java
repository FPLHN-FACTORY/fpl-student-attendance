package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_StudentFactoryAddRequest;
import udpm.hn.studentattendance.core.staff.factory.service.Staff_StudentFactoryService;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.ImportLogType;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogDetailResponse;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogResponse;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogDetailRepository;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogRepository;
import udpm.hn.studentattendance.infrastructure.excel.service.EXStudentFactoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EXStudentFactoryServiceImpl implements EXStudentFactoryService {

    private final Staff_StudentFactoryService service;

    private final EXImportLogRepository importLogRepository;

    private final EXImportLogDetailRepository importLogDetailRepository;

    private final ExcelHelper excelHelper;

    private final SessionHelper sessionHelper;

    @Override
    public ResponseEntity<?> getDataFromFile(EXUploadRequest request) {
        MultipartFile file = request.getFile();
        if (file.isEmpty()) {
            return RouterHelper.createResponseApi(ApiResponse.error("Vui lòng tải lên file Excel"), HttpStatus.BAD_GATEWAY);
        }

        try {
            List<Map<String, String>> data = ExcelHelper.readFile(file);
            return RouterHelper.responseSuccess("Tải lên file Excel thành công", data);
        } catch (IOException e) {
            return RouterHelper.responseError("Lỗi khi xử lý file Excel", e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> importItem(EXImportRequest request) {
        Map<String, Object> data = request.getData();
        Map<String, String> item = request.getItem();

        String idFactory = (String) data.get("idFactory");

        Staff_StudentFactoryAddRequest addRequest = new Staff_StudentFactoryAddRequest();
        addRequest.setFactoryId(idFactory);
        addRequest.setStudentCode(item.get("MA_SINH_VIEN"));

        ResponseEntity<ApiResponse> result = (ResponseEntity<ApiResponse>) service.createStudent(addRequest);
        ApiResponse response = result.getBody();

        if (response.getStatus() == RestApiStatus.SUCCESS) {
            excelHelper.saveLogSuccess(ImportLogType.STUDENT_FACTORY, response.getMessage(), request);
        } else {
            excelHelper.saveLogError(ImportLogType.STUDENT_FACTORY, response.getMessage(), request);
        }
        return result;
    }

    @Override
    public ResponseEntity<?> downloadTemplate(EXDataRequest request) {
        String filename = "template-import-student-factory.xlsx";

        List<String> headers = List.of("Mã Sinh Viên");
        byte[] data = ExcelHelper.createExcelStream("student-factory", headers, new ArrayList<>());

        if (data == null) {
            return null;
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentDisposition(ContentDisposition.builder("attachment").filename(filename).build());
        httpHeaders.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return new ResponseEntity<>(data, httpHeaders, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> historyLog(EXDataRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<ExImportLogResponse> data = PageableObject.of(importLogRepository.getListHistory
                (pageable, ImportLogType.STUDENT_FACTORY.ordinal(), sessionHelper.getUserId(), sessionHelper.getFacilityId()));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> historyLogDetail(EXDataRequest request, String id) {
        List<ExImportLogDetailResponse> data = importLogDetailRepository.getAllList
                (id, sessionHelper.getUserId(), sessionHelper.getFacilityId());
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }
}
