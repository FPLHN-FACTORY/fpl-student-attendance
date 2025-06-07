package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STStudentAttendanceRecoveryRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.repository.STAttendanceRecoveryRepository;
import udpm.hn.studentattendance.core.staff.attendancerecovery.service.STAttendanceRecoveryService;
import udpm.hn.studentattendance.entities.AttendanceRecovery;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.ImportLog;
import udpm.hn.studentattendance.entities.ImportLogDetail;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.ImportLogType;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogDetailResponse;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogResponse;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogDetailRepository;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogRepository;
import udpm.hn.studentattendance.infrastructure.excel.service.EXAttendanceRecoveryService;
import udpm.hn.studentattendance.utils.ExcelUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EXAttendanceRecoveryServiceImpl implements EXAttendanceRecoveryService {

    private final EXImportLogRepository importLogRepository;

    private final EXImportLogDetailRepository importLogDetailRepository;

    private final SessionHelper sessionHelper;

    private final ExcelHelper excelHelper;

    private final STAttendanceRecoveryService attendanceRecoveryService;

    private final STAttendanceRecoveryRepository attendanceRecoveryRepository;

    @Override
    public ResponseEntity<?> getDataFromFile(EXUploadRequest request) {
        MultipartFile file = request.getFile();
        if (file.isEmpty()) {
            return RouterHelper.createResponseApi(ApiResponse.error("Vui lòng tải lên file excel"), HttpStatus.BAD_GATEWAY);
        }
        try {
            List<Map<String, String>> data = ExcelHelper.readFile(file);

            return RouterHelper.responseSuccess("Tải lên excel thành công", data);
        } catch (Exception e) {
            return RouterHelper.responseError("Lỗi khi xử lý excel", e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> importItem(EXImportRequest request) {
        Map<String, String> item = request.getItem();
        Map<String, Object> data = request.getData();

        String idAttendanceRecovery = (String) data.get("attendanceRecoveryId");
        String dayString = item.get("NGAY_DIEM_DANH");
        if (dayString.isEmpty() || dayString == null || dayString.equals("")) {
            String msg = "Thông tin về ngày điểm danh không được để trống.";
            excelHelper.saveLogError(ImportLogType.ATTENDANCE_RECOVERY, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }

        Long dayAttendance;
        try {
            LocalDateTime localDateTime;

            if (dayString.contains(":")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                localDateTime = LocalDateTime.parse(dayString, formatter);
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate localDate = LocalDate.parse(dayString, formatter);
                localDateTime = localDate.atStartOfDay();
            }

            ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
            ZonedDateTime zonedDateTime = localDateTime.atZone(vietnamZone);
            dayAttendance = zonedDateTime.toInstant().toEpochMilli();

        } catch (Exception e) {
            String msg = "Định dạng ngày điểm danh không hợp lệ. Vui lòng sử dụng format: dd/MM/yyyy hoặc dd/MM/yyyy HH:mm:ss";
            excelHelper.saveLogError(ImportLogType.ATTENDANCE_RECOVERY, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }

        String studentCode = item.get("MA_SINH_VIEN");
        if (studentCode.isEmpty() || studentCode == null || studentCode.equals("")) {
            String msg = "Thông tin về mã sinh viên không được để trống.";
            excelHelper.saveLogError(ImportLogType.ATTENDANCE_RECOVERY, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }

        STStudentAttendanceRecoveryRequest stStudentAttendanceRecoveryRequest = new STStudentAttendanceRecoveryRequest();
        stStudentAttendanceRecoveryRequest.setStudentCode(studentCode);
        stStudentAttendanceRecoveryRequest.setDay(dayAttendance);
        stStudentAttendanceRecoveryRequest.setAttendanceRecoveryId(idAttendanceRecovery);

        Optional<AttendanceRecovery> attendanceRecoveryOptional = attendanceRecoveryRepository.findById(idAttendanceRecovery);
        AttendanceRecovery attendanceRecovery = attendanceRecoveryOptional.get();
        attendanceRecovery.setTotalStudent(request.getLine());
        attendanceRecoveryRepository.save(attendanceRecovery);

        ResponseEntity<ApiResponse> result = (ResponseEntity<ApiResponse>) attendanceRecoveryService.importAttendanceRecoveryStudent(stStudentAttendanceRecoveryRequest);
        ApiResponse response = result.getBody();
        if (response.getStatus() == RestApiStatus.SUCCESS) {
            ImportLog importLog = importLogRepository.findByIdUserAndCodeAndFileNameAndFacility_Id
                    (sessionHelper.getUserId(), request.getCode(), request.getFileName(), sessionHelper.getFacilityId()).orElse(null);
            if (importLog == null) {
                Facility facility = new Facility();
                facility.setId(sessionHelper.getFacilityId());

                if (facility.getId() == null) {
                    facility = null;
                }

                ImportLog newImportLog = new ImportLog();
                newImportLog.setIdUser(sessionHelper.getUserId());
                newImportLog.setFacility(facility);
                newImportLog.setCode(request.getCode());
//                newImportLog.setType(6);
                newImportLog.setFileName(request.getFileName());
                importLog = importLogRepository.save(newImportLog);
            }

            ImportLogDetail importLogDetail = new ImportLogDetail();
            importLogDetail.setImportLog(importLog);
            importLogDetail.setLine(request.getLine());
            importLogDetail.setMessage(response.getMessage());
            importLogDetail.setStatus(EntityStatus.ACTIVE);

            importLogDetailRepository.save(importLogDetail);
            attendanceRecovery.setImportLog(importLog);
            attendanceRecoveryRepository.save(attendanceRecovery);
        } else {
            ImportLog importLog = importLogRepository.findByIdUserAndCodeAndFileNameAndFacility_Id
                    (sessionHelper.getUserId(), request.getCode(), request.getFileName(), sessionHelper.getFacilityId()).orElse(null);
            if (importLog == null) {
                Facility facility = new Facility();
                facility.setId(sessionHelper.getFacilityId());

                if (facility.getId() == null) {
                    facility = null;
                }

                ImportLog newImportLog = new ImportLog();
                newImportLog.setIdUser(sessionHelper.getUserId());
                newImportLog.setFacility(facility);
                newImportLog.setCode(request.getCode());
//                newImportLog.setType(6);
                newImportLog.setFileName(request.getFileName());
                importLog = importLogRepository.save(newImportLog);
            }

            ImportLogDetail importLogDetail = new ImportLogDetail();
            importLogDetail.setImportLog(importLog);
            importLogDetail.setLine(request.getLine());
            importLogDetail.setMessage(response.getMessage());
            importLogDetail.setStatus(EntityStatus.INACTIVE);

            importLogDetailRepository.save(importLogDetail);
            attendanceRecovery.setImportLog(importLog);
            attendanceRecoveryRepository.save(attendanceRecovery);
        }
        return result;
    }

    @Override
    public ResponseEntity<?> exportData(EXDataRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<byte[]> downloadTemplate(EXDataRequest request) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream data = new ByteArrayOutputStream()) {
            String filename = "template-import-attendance-recovery.xlsx";
            List<String> headers = List.of("Ngày điểm danh", "Mã sinh viên");
            int firstRow = 1;
            int lastRow = 500;

            Sheet templateSheet = ExcelUtils.createTemplate(workbook, "Data Import", headers, new ArrayList<>());
            ExcelUtils.addDateValidation(templateSheet, firstRow, lastRow, 0, "dd/MM/yyyy HH:mm:ss", "01/01/1900 00:00:00", "31/12/9999 00:00:00");
            workbook.write(data);

            HttpHeaders headersHttp = new HttpHeaders();
            headersHttp.setContentDisposition(ContentDisposition.builder("attachment").filename(filename).build());
            headersHttp.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
            headersHttp.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return new ResponseEntity<>(data.toByteArray(), headersHttp, HttpStatus.OK);
        } catch (IOException | ParseException e) {
            return null;
        }
    }

    @Override
    public ResponseEntity<?> historyLog(EXDataRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<ExImportLogResponse> data = PageableObject.of(importLogRepository.getListHistory
                (pageable, ImportLogType.ATTENDANCE_RECOVERY.ordinal(), sessionHelper.getUserId(), sessionHelper.getFacilityId()));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> historyLogDetail(EXDataRequest request, String id) {
        List<ExImportLogDetailResponse> data = importLogDetailRepository.getAllList(id, sessionHelper.getUserId(), sessionHelper.getFacilityId());
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }
}
