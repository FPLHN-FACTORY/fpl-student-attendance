package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.staff.factory.model.request.userstudentfactory.USStudentFactoryAddRequest;
import udpm.hn.studentattendance.core.staff.factory.model.response.USPlanDateStudentFactoryResponse;
import udpm.hn.studentattendance.core.staff.factory.repository.factory.USFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.repository.userstudentfactory.USStudentFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.service.USStudentFactoryService;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCPlanDateStudentFactoryResponse;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;
import udpm.hn.studentattendance.infrastructure.constants.ImportLogType;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.excel.model.dto.ExStudentModel;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogDetailResponse;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogResponse;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogDetailRepository;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogRepository;
import udpm.hn.studentattendance.infrastructure.excel.service.EXStudentFactoryService;
import udpm.hn.studentattendance.repositories.UserStudentFactoryRepository;
import udpm.hn.studentattendance.utils.CodeGeneratorUtils;
import udpm.hn.studentattendance.utils.DateTimeUtils;
import udpm.hn.studentattendance.utils.ExcelUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EXStudentFactoryServiceImpl implements EXStudentFactoryService {

    private final USStudentFactoryService service;

    private final EXImportLogRepository importLogRepository;

    private final EXImportLogDetailRepository importLogDetailRepository;

    private final ExcelHelper excelHelper;

    private final SessionHelper sessionHelper;

    private final USFactoryExtendRepository factoryExtendRepository;

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

        String code = item.get("MA_SINH_VIEN");
        if (code == null || code.trim().isEmpty()) {
            String msg = "Mã sinh viên không được để trống.";
            excelHelper.saveLogError(ImportLogType.STUDENT_FACTORY, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }

        USStudentFactoryAddRequest addRequest = new USStudentFactoryAddRequest();
        addRequest.setFactoryId(idFactory);
        addRequest.setStudentCode(code);

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
    public ResponseEntity<?> exportData(EXDataRequest request) {
        String factoryId = (String) request.getData().get("idFactory");
        Factory factory = factoryExtendRepository.findById(factoryId).orElse(null);
        if (factory == null) {
            return RouterHelper.responseError("Không tìm thấy xưởng", HttpStatus.BAD_REQUEST);
        }
        if (!sessionHelper.getUserRole().contains(RoleConstant.ADMIN)
                && !Objects.equals(factory.getUserStaff().getId(), sessionHelper.getUserId())) {
            return RouterHelper.responseError("Không có quyền truy cập", HttpStatus.UNAUTHORIZED);
        }

        List<USPlanDateStudentFactoryResponse> lstData = factoryExtendRepository
                .getAllPlanDateAttendanceByIdFactory(factory.getId());

        // Lấy danh sách sinh viên duy nhất
        Map<String, List<USPlanDateStudentFactoryResponse>> grouped = lstData.stream()
                .collect(Collectors.groupingBy(USPlanDateStudentFactoryResponse::getCode));

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            String filename = "student-factory-" + CodeGeneratorUtils
                    .generateCodeFromString(factory.getName()).toLowerCase() + ".xlsx";
            List<String> headers = new ArrayList<>(List.of("Mã sinh viên", "Họ tên sinh viên", "Tổng buổi", "Tổng vắng(%)"));

            Sheet sheet = ExcelUtils.createTemplate(workbook, "Summary", headers, List.of());
            int row = 1;

            // Xử lý cho từng sinh viên
            for (Map.Entry<String, List<USPlanDateStudentFactoryResponse>> entry : grouped.entrySet()) {
                String studentCode = entry.getKey();
                List<USPlanDateStudentFactoryResponse> records = entry.getValue();
                List<USPlanDateStudentFactoryResponse> occurred = records.stream()
                        .toList();
                int totalSessions = occurred.size();
                int totalAbsent = (int) occurred.stream()
                        .filter(d -> d.getStatus() != AttendanceStatus.PRESENT.ordinal() && d.getStartDate() <= System.currentTimeMillis())
                        .count();
                String percent = totalSessions > 0
                        ? Math.round((double) totalAbsent / totalSessions * 1000) / 10.0 + "%"
                        : "0%";
                String studentName = records.get(0).getName();

                ExcelUtils.insertRow(sheet, row++, List.of(studentCode, studentName, totalAbsent + "/" + totalSessions, percent));
            }

            sheet.setColumnWidth(0, 20 * 256);
            sheet.setColumnWidth(1, 30 * 256);
            sheet.setColumnWidth(2, 15 * 256);
            sheet.setColumnWidth(3, 20 * 256);

            workbook.write(out);
            HttpHeaders headersHttp = new HttpHeaders();
            headersHttp.setContentDisposition(
                    ContentDisposition.builder("attachment").filename(filename).build());
            headersHttp.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
            headersHttp.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return new ResponseEntity<>(out.toByteArray(), headersHttp, HttpStatus.OK);
        } catch (IOException e) {
            return RouterHelper.responseError("Lỗi khi xuất Excel", HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
