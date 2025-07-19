package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STStudentAttendanceRecoveryAddRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.repository.STAttendanceRecoveryRepository;
import udpm.hn.studentattendance.core.staff.attendancerecovery.service.STAttendanceRecoveryService;
import udpm.hn.studentattendance.entities.*;
import udpm.hn.studentattendance.helpers.*;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserActivityLogRepository;
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

    private final UserActivityLogHelper userActivityLogHelper;

    private final CommonUserActivityLogRepository userActivityLogRepository;

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

        // Lấy ImportLog chung cho toàn bộ quá trình import
        ImportLog importLog = getOrCreateImportLog(request);

        String idAttendanceRecovery = (String) data.get("attendanceRecoveryId");

        // Set ImportLog cho AttendanceRecovery ngay từ đầu
        setAttendanceRecoveryImportLog(idAttendanceRecovery, importLog);

        String dayString = item.get("NGAY_DIEM_DANH");

        if (dayString.isEmpty() || dayString == null || dayString.equals("")) {
            String msg = "Thông tin về ngày điểm danh không được để trống.";
            createImportLogDetail(importLog, request, msg, EntityStatus.INACTIVE);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }

        Long dayAttendance;
        LocalDateTime attendanceDateTime;
        try {
            attendanceDateTime = parseAttendanceDate(dayString);
            dayAttendance = convertToEpochMilli(attendanceDateTime);
        } catch (Exception e) {
            String msg = "Định dạng ngày điểm danh không hợp lệ. Vui lòng sử dụng format: dd/MM/yyyy hoặc dd/MM/yyyy HH:mm:ss";
            createImportLogDetail(importLog, request, msg, EntityStatus.INACTIVE);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }

        LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        if (attendanceDateTime.isAfter(currentDateTime)) {
            String msg = "Không thể khôi phục điểm danh cho ngày trong tương lai. Ngày điểm danh phải là ngày hiện tại hoặc trong quá khứ.";
            createImportLogDetail(importLog, request, msg, EntityStatus.INACTIVE);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }

        String studentCode = item.get("MA_SINH_VIEN");
        if (studentCode.isEmpty() || studentCode == null || studentCode.equals("")) {
            String msg = "Thông tin về mã sinh viên không được để trống.";
            createImportLogDetail(importLog, request, msg, EntityStatus.INACTIVE);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }

        // Create attendance recovery request
        STStudentAttendanceRecoveryAddRequest stStudentAttendanceRecoveryRequest = new STStudentAttendanceRecoveryAddRequest();
        stStudentAttendanceRecoveryRequest.setStudentCode(studentCode);
        stStudentAttendanceRecoveryRequest.setDay(dayAttendance);
        stStudentAttendanceRecoveryRequest.setAttendanceRecoveryId(idAttendanceRecovery);

        // Process attendance recovery
        ResponseEntity<ApiResponse> result = (ResponseEntity<ApiResponse>) attendanceRecoveryService
                .importAttendanceRecoveryStudent(stStudentAttendanceRecoveryRequest);
        ApiResponse response = result.getBody();

        // Create import log detail cho kết quả xử lý
        EntityStatus status = response.getStatus() == RestApiStatus.SUCCESS ? EntityStatus.ACTIVE : EntityStatus.INACTIVE;
        createImportLogDetail(importLog, request, response.getMessage(), status);

        // Update total student count
        updateAttendanceRecoveryTotalStudent(idAttendanceRecovery, importLog);


        return result;
    }

    private LocalDateTime parseAttendanceDate(String dayString) {
        LocalDateTime localDateTime;

        if (dayString.contains(":")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            localDateTime = LocalDateTime.parse(dayString, formatter);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.parse(dayString, formatter);
            localDateTime = localDate.atStartOfDay();
        }

        return localDateTime;
    }

    private Long convertToEpochMilli(LocalDateTime localDateTime) {
        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
        ZonedDateTime zonedDateTime = localDateTime.atZone(vietnamZone);
        return zonedDateTime.toInstant().toEpochMilli();
    }

    private void createImportLogDetail(ImportLog importLog, EXImportRequest request, String message, EntityStatus status) {
        ImportLogDetail importLogDetail = new ImportLogDetail();
        importLogDetail.setImportLog(importLog);
        importLogDetail.setLine(request.getLine());
        importLogDetail.setMessage(message);
        importLogDetail.setStatus(status);

        importLogDetailRepository.save(importLogDetail);
    }

    private void setAttendanceRecoveryImportLog(String idAttendanceRecovery, ImportLog importLog) {
        Optional<AttendanceRecovery> attendanceRecoveryOptional = attendanceRecoveryRepository.findById(idAttendanceRecovery);
        if (attendanceRecoveryOptional.isPresent()) {
            AttendanceRecovery attendanceRecovery = attendanceRecoveryOptional.get();
            attendanceRecovery.setImportLog(importLog);
            attendanceRecoveryRepository.save(attendanceRecovery);
        }
    }

    private void updateAttendanceRecoveryTotalStudent(String idAttendanceRecovery, ImportLog importLog) {
        int totalStudent = attendanceRecoveryService.getAllImportStudentSuccess(
                importLog.getId(),
                sessionHelper.getUserId(),
                sessionHelper.getFacilityId(),
                6
        );

        Optional<AttendanceRecovery> attendanceRecoveryOptional = attendanceRecoveryRepository.findById(idAttendanceRecovery);
        if (attendanceRecoveryOptional.isPresent()) {
            AttendanceRecovery attendanceRecovery = attendanceRecoveryOptional.get();
            attendanceRecovery.setTotalStudent(totalStudent);
            attendanceRecovery.setImportLog(importLog);
            attendanceRecoveryRepository.save(attendanceRecovery);

//            userActivityLogHelper.saveLog(
//                    "vừa thêm danh sách sinh viên vào sự kiện: " + attendanceRecoveryOptional.get().getName());

        }
    }


    private ImportLog getOrCreateImportLog(EXImportRequest request) {
        ImportLog importLog = importLogRepository.findByIdUserAndCodeAndFileNameAndFacility_Id(
                sessionHelper.getUserId(),
                request.getCode(),
                request.getFileName(),
                sessionHelper.getFacilityId()
        ).orElse(null);

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
            newImportLog.setType(6);
            newImportLog.setFileName(request.getFileName());
            importLog = importLogRepository.save(newImportLog);
        }

        return importLog;
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



