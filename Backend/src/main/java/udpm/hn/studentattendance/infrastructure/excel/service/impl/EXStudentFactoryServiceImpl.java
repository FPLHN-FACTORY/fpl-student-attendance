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
//        Map<String, Object> dataRequest = request.getData();
//        String factoryId = (String) dataRequest.get("factoryId");
//        if (factoryId != null){
//            return null;
//        } else {
//            Factory factory = tcFactoryExtendRepository.findById(idFactory).orElse(null);
//
//            if (factory == null) {
//                return null;
//            }
//
//            if (!sessionHelper.getUserRole().contains(RoleConstant.ADMIN) && !sessionHelper.getUserRole().contains(RoleConstant.STAFF)) {
//                if (!Objects.equals(factory.getUserStaff().getId(), sessionHelper.getUserId())) {
//                    return null;
//                }
//            }
//
//            List<TCPlanDateStudentFactoryResponse> lstData = tcStudentFactoryExtendRepository.getAllPlanDateAttendanceByIdFactory(factory.getId());
//
//            try (Workbook workbook = new XSSFWorkbook();
//                 ByteArrayOutputStream data = new ByteArrayOutputStream()) {
//                String filename =
//                        "PlanDate-attendance-" + CodeGeneratorUtils.generateCodeFromString(factory.getName()).toLowerCase() + ".xlsx";
//
//                List<String> headers = new ArrayList<>(List.of("Mã sinh viên", "Họ tên sinh viên"));
//
//                Set<String> stPlanDate = lstData.stream()
//                        .map(this::buildCellPlanDate)
//                        .collect(Collectors.toSet());
//                List<String> lstPlanDate = stPlanDate.stream()
//                        .sorted()
//                        .toList();
//
//                Set<ExStudentModel> stPStudent = lstData.stream()
//                        .map(o -> new ExStudentModel(o.getCode(), o.getName()))
//                        .collect(Collectors.toCollection(LinkedHashSet::new));
//                List<ExStudentModel> lstStudent = stPStudent.stream()
//                        .toList();
//
//                headers.addAll(lstPlanDate);
//                headers.add("Tổng");
//                headers.add("Vắng(%)");
//
//                Sheet sheet = ExcelUtils.createTemplate(workbook, "Data Export", headers, new ArrayList<>());
//
//                Map<Object, String> colorMap = new HashMap<>();
//                colorMap.put("Có mặt", "#a9d08e");
//                colorMap.put("Vắng mặt", "#ff7d7d");
//
//
//                int row = 1;
//                for (ExStudentModel student: lstStudent) {
//                    String studentCode = student.getCode();
//                    String studentName = student.getName();
//
//                    List<Object> dataCell = new ArrayList<>();
//                    dataCell.add(studentCode);
//                    dataCell.add(studentName);
//
//                    int total_absent = 0;
//                    for(String namePlanDate: lstPlanDate) {
//                        TCPlanDateStudentFactoryResponse planDate = lstData.stream().filter(s -> s.getCode().equals(studentCode) && buildCellPlanDate(s).equals(namePlanDate)).findFirst().orElse(null);
//                        if (planDate == null || planDate.getStartDate() > DateTimeUtils.getCurrentTimeMillis()) {
//                            dataCell.add(" - ");
//                            continue;
//                        }
//                        if (planDate.getStatus() == AttendanceStatus.PRESENT.ordinal()) {
//                            dataCell.add("Có mặt");
//                        } else {
//                            total_absent++;
//                            dataCell.add("Vắng mặt");
//                        }
//                    }
//
//                    dataCell.add(total_absent + "/" + lstPlanDate.size());
//                    dataCell.add(Math.round((double) total_absent / lstPlanDate.size() * 1000) / 10.0 + "%");
//
//                    ExcelUtils.insertRow(sheet, row, dataCell, colorMap);
//                    row++;
//                }
//                workbook.write(data);
//
//                HttpHeaders headersHttp = new HttpHeaders();
//                headersHttp.setContentDisposition(ContentDisposition.builder("attachment").filename(filename).build());
//                headersHttp.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
//                headersHttp.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//                return new ResponseEntity<>(data.toByteArray(), headersHttp, HttpStatus.OK);
//            } catch (IOException e) {
//                return null;
//            }
//        }
        return null;
    }

//    private String buildCellPlanDate(TCPlanDateStudentFactoryResponse o) {
//        return DateTimeUtils.convertMillisToDate(o.getStartDate(), "dd-MM-yyyy") + " - Ca " + o.getShift();
//    }

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
