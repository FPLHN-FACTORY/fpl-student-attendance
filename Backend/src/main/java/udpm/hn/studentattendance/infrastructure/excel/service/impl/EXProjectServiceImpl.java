package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectCreateOrUpdateRequest;
import udpm.hn.studentattendance.core.staff.project.repository.STLevelProjectExtendRepository;
import udpm.hn.studentattendance.core.staff.project.repository.STProjectSemesterExtendRepository;
import udpm.hn.studentattendance.core.staff.project.repository.STProjectSubjectFacilityExtendRepository;
import udpm.hn.studentattendance.core.staff.project.service.STProjectManagementService;
import udpm.hn.studentattendance.entities.LevelProject;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.entities.SubjectFacility;
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
import udpm.hn.studentattendance.infrastructure.excel.service.EXProjectService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EXProjectServiceImpl implements EXProjectService {

    private final EXImportLogRepository importLogRepository;
    private final EXImportLogDetailRepository importLogDetailRepository;
    private final ExcelHelper excelHelper;
    private final SessionHelper sessionHelper;
    private final STProjectManagementService service;
    private final STProjectSemesterExtendRepository semesterExtendRepository;
    private final STProjectSubjectFacilityExtendRepository subjectFacilityExtendRepository;
    private final STLevelProjectExtendRepository levelProjectExtendRepository;

    @Override
    public ResponseEntity<?> getDataFromFile(EXUploadRequest request) {
        MultipartFile file;
        file = request.getFile();

        if (file.isEmpty() || file == null) {
            return RouterHelper.createResponseApi(
                    ApiResponse.error("Vui lòng tải lên file Excel"),
                    HttpStatus.BAD_GATEWAY
            );
        }
        try {
            List<Map<String, String>> data = ExcelHelper.readFile(file);
            return RouterHelper.responseSuccess("Tải lên Excel thành công", data);
        } catch (IOException e) {
            return RouterHelper.responseError("Lỗi khi xử lý Excel", e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> importItem(EXImportRequest request) {
        Map<String, String> item = request.getItem();

        // 1. Lấy dữ liệu từ cột
        String nameProject = item.get("TEN_DU_AN");
        String description = item.get("MO_TA");
        String levelRaw = item.get("CAP_DU_AN");     // "LevelName - LEVEL_CODE"
        String semesterRaw = item.get("HOC_KY");        // "SEMESTER_CODE"
        String subjectRaw = item.get("MON_HOC");       // "SubjectName - SUBJECT_ID"

        // 2. Validate bắt buộc
        if (nameProject == null || nameProject.isBlank()) {
            String msg = "Không được để trống tên dự án";
            excelHelper.saveLogError(ImportLogType.PROJECT, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }
        if (levelRaw == null || levelRaw.isBlank()) {
            String msg = "Không được để trống cấp dự án";
            excelHelper.saveLogError(ImportLogType.PROJECT, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }
        if (semesterRaw == null || semesterRaw.isBlank()) {
            String msg = "Không được để trống học kỳ";
            excelHelper.saveLogError(ImportLogType.PROJECT, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }
        if (subjectRaw == null || subjectRaw.isBlank()) {
            String msg = "Không được để trống môn học";
            excelHelper.saveLogError(ImportLogType.PROJECT, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }

        // 3. Parse cấp dự án và môn học
        String[] levelParts = levelRaw.split(" - ");
        if (levelParts.length < 2) {
            String msg = "Định dạng cấp dự án không hợp lệ. Vui lòng chọn từ dropdown.";
            excelHelper.saveLogError(ImportLogType.PROJECT, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }
        String levelCode = levelParts[1].trim();

        String semesterCode = semesterRaw.trim(); // đã là code

        String[] subjectParts = subjectRaw.split(" - ");
        if (subjectParts.length < 2) {
            String msg = "Định dạng môn học không hợp lệ. Vui lòng chọn từ dropdown.";
            excelHelper.saveLogError(ImportLogType.PROJECT, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }
        String subjectId = subjectParts[1].trim(); // ID là string

        // 4. Lookup entities
        Semester semester = semesterExtendRepository.getSemesterByCode(semesterCode);
        if (semester == null) {
            String msg = String.format("Học kỳ '%s' không tồn tại.", semesterCode);
            excelHelper.saveLogError(ImportLogType.PROJECT, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }

        SubjectFacility subject = subjectFacilityExtendRepository.getSubjectFacilityByName(sessionHelper.getFacilityId(), subjectId);
        if (subject == null) {
            String msg = String.format("Môn học với ID '%s' không tồn tại.", subjectId);
            excelHelper.saveLogError(ImportLogType.PROJECT, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }

        LevelProject levelProject = levelProjectExtendRepository.getAllLevelProjectByCode(EntityStatus.ACTIVE, levelCode);
        if (levelProject == null) {
            String msg = String.format("Cấp dự án '%s' không tồn tại.", levelCode);
            excelHelper.saveLogError(ImportLogType.PROJECT, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }

        // 5. Build DTO và gọi service
        USProjectCreateOrUpdateRequest dto = new USProjectCreateOrUpdateRequest();
        dto.setName(nameProject.trim());
        dto.setDescription(description == null ? null : description.trim());
        dto.setLevelProjectId(levelProject.getId());
        dto.setSemesterId(semester.getId());
        dto.setSubjectFacilityId(subjectId);  // giữ string ID

        ResponseEntity<ApiResponse> result = (ResponseEntity<ApiResponse>) service.createProject(dto);
        ApiResponse apiRes = result.getBody();
        if (apiRes.getStatus() == RestApiStatus.SUCCESS) {
            excelHelper.saveLogSuccess(ImportLogType.PROJECT, apiRes.getMessage(), request);
        } else {
            excelHelper.saveLogError(ImportLogType.PROJECT, apiRes.getMessage(), request);
        }
        return result;
    }

    @Override
    public ResponseEntity<?> downloadTemplate(EXDataRequest request) {
        String filename = "template-import-project.xlsx";
        List<String> headers = List.of("Tên dự án", "Mô tả", "Cấp dự án", "Học kỳ", "Môn học");

        List<String> semesterList = semesterExtendRepository
                .getAllSemester(EntityStatus.ACTIVE)
                .stream()
                .map(s -> s.getCode())
                .toList();

        List<String> subjectFacilityList = subjectFacilityExtendRepository
                .getSubjectFacility(sessionHelper.getFacilityId())
                .stream()
                .map(sf -> sf.getName() + " - " + sf.getId())
                .toList();

        List<String> levelProjectList = levelProjectExtendRepository.getLevelProject()
                .stream()
                .map(l -> l.getName() + " - " + l.getCode())
                .toList();

        try {
            byte[] data = createExcelStream(
                    "project", headers, new ArrayList<>(), levelProjectList, subjectFacilityList, semesterList
            );
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentDisposition(
                    ContentDisposition.builder("attachment").filename(filename).build()
            );
            httpHeaders.set(
                    HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                    HttpHeaders.CONTENT_DISPOSITION
            );
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return new ResponseEntity<>(data, httpHeaders, HttpStatus.OK);
        } catch (IOException e) {
            return RouterHelper.responseError("Không thể tạo file mẫu", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> historyLog(EXDataRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<ExImportLogResponse> data = PageableObject.of(
                importLogRepository.getListHistory(
                        pageable,
                        ImportLogType.PROJECT.ordinal(),
                        sessionHelper.getUserId(),
                        sessionHelper.getFacilityId()
                )
        );
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> historyLogDetail(EXDataRequest request, String id) {
        List<ExImportLogDetailResponse> data = importLogDetailRepository.getAllList(
                id,
                sessionHelper.getUserId(),
                sessionHelper.getFacilityId()
        );
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    /**
     * Tạo file Excel mẫu với dropdown cho cột Học kỳ và Môn học,
     * đồng thời tô màu nền cho hàng tiêu đề.
     */
    public static byte[] createExcelStream(
            String sheetName,
            List<String> headers,
            List<Map<String, String>> data,
            List<String> levelProjectList,
            List<String> subjectFacilityList,
            List<String> semesterList
    ) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);

        // Style cho header: màu xanh, đậm, wrap text
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setWrapText(true);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Style chung cho wrap text
        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        wrapStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Tạo header và gán style
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(
                sheet.getDefaultRowHeightInPoints() * 1.5f
        );

        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
            sheet.setDefaultColumnStyle(i, wrapStyle);
        }

        // Viết dữ liệu nếu có
        int rowIndex = 1;
        for (Map<String, String> rowData : data) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(rowData.getOrDefault(headers.get(i), ""));
                cell.setCellStyle(wrapStyle);
            }
        }

        // Auto-size các cột chung
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Tạo sheet DropdownData: SubjectFacility và Semester
        Sheet dropdownSheet = workbook.createSheet("DropdownData");

        for (int i = 0; i < levelProjectList.size(); i++) {
            Row row = dropdownSheet.createRow(i);
            row.createCell(0).setCellValue(subjectFacilityList.get(i));
        }

        for (int i = 0; i < subjectFacilityList.size(); i++) {
            Row row = dropdownSheet.createRow(i);
            row.createCell(0).setCellValue(subjectFacilityList.get(i));
        }

        for (int i = 0; i < semesterList.size(); i++) {
            Row row = dropdownSheet.getRow(i);
            if (row == null) row = dropdownSheet.createRow(i);
            row.createCell(1).setCellValue(semesterList.get(i));
        }

        // Named ranges
        Name levelProjectName = workbook.createName();
        levelProjectName.setNameName("LevelProjectList");
        levelProjectName.setRefersToFormula(
                "DropdownData!$A$1:$A$" + subjectFacilityList.size()
        );
        Name subjectFacilityName = workbook.createName();
        subjectFacilityName.setNameName("SubjectFacilityList");
        subjectFacilityName.setRefersToFormula(
                "DropdownData!$B$1:$B$" + subjectFacilityList.size()
        );
        Name semesterName = workbook.createName();
        semesterName.setNameName("SemesterList");
        semesterName.setRefersToFormula(
                "DropdownData!$C$1:$C$" + semesterList.size()
        );

        // Data validation cho cột Học kỳ (index 3) và Môn học (index 4)
        DataValidationHelper dvHelper = sheet.getDataValidationHelper();
        // Cấp dự án
        DataValidationConstraint levelProjectConstraint = dvHelper.createFormulaListConstraint("LevelProjectList");
        CellRangeAddressList levelProjectAddr = new CellRangeAddressList(1, 100, 3, 3);
        DataValidation levelProjectValid = dvHelper.createValidation(levelProjectConstraint, levelProjectAddr);
        levelProjectValid.setSuppressDropDownArrow(true);
        sheet.addValidationData(levelProjectValid);
        // Học kỳ
        DataValidationConstraint semesterConstraint = dvHelper.createFormulaListConstraint("SemesterList");
        CellRangeAddressList semesterAddr = new CellRangeAddressList(1, 100, 4, 3);
        DataValidation semesterValid = dvHelper.createValidation(semesterConstraint, semesterAddr);
        semesterValid.setSuppressDropDownArrow(true);
        sheet.addValidationData(semesterValid);
        // Môn học
        DataValidationConstraint subjectConstraint = dvHelper.createFormulaListConstraint("SubjectFacilityList");
        CellRangeAddressList subjectAddr = new CellRangeAddressList(1, 100, 5, 4);
        DataValidation subjectValid = dvHelper.createValidation(subjectConstraint, subjectAddr);
        subjectValid.setSuppressDropDownArrow(true);
        sheet.addValidationData(subjectValid);

        // Ẩn sheet DropdownData
        workbook.setSheetHidden(
                workbook.getSheetIndex(dropdownSheet),
                true
        );

        // Xuất file
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            workbook.write(baos);
            workbook.close();
            return baos.toByteArray();
        }
    }
}
