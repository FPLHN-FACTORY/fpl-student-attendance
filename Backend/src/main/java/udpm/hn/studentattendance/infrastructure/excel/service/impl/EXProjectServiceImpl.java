package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.staff.project.repository.STProjectSemesterExtendRepository;
import udpm.hn.studentattendance.core.staff.project.repository.STProjectSubjectFacilityExtendRepository;
import udpm.hn.studentattendance.core.staff.project.service.STProjectManagementService;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.ImportLogType;
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

    @Override
    public ResponseEntity<?> getDataFromFile(EXUploadRequest request) {

        MultipartFile file = request.getFile();
        if (file.isEmpty()) {
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

        return null;
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
                .map(sf -> sf.getName())
                .toList();

        try {
            byte[] data = createExcelStream(
                    "project", headers, new ArrayList<>(), subjectFacilityList, semesterList
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
        Name subjectFacilityName = workbook.createName();
        subjectFacilityName.setNameName("SubjectFacilityList");
        subjectFacilityName.setRefersToFormula(
                "DropdownData!$A$1:$A$" + subjectFacilityList.size()
        );
        Name semesterName = workbook.createName();
        semesterName.setNameName("SemesterList");
        semesterName.setRefersToFormula(
                "DropdownData!$B$1:$B$" + semesterList.size()
        );

        // Data validation cho cột Học kỳ (index 3) và Môn học (index 4)
        DataValidationHelper dvHelper = sheet.getDataValidationHelper();
        // Học kỳ
        DataValidationConstraint semesterConstraint = dvHelper.createFormulaListConstraint("SemesterList");
        CellRangeAddressList semesterAddr = new CellRangeAddressList(1, 100, 3, 3);
        DataValidation semesterValid = dvHelper.createValidation(semesterConstraint, semesterAddr);
        semesterValid.setSuppressDropDownArrow(true);
        sheet.addValidationData(semesterValid);
        // Môn học
        DataValidationConstraint subjectConstraint = dvHelper.createFormulaListConstraint("SubjectFacilityList");
        CellRangeAddressList subjectAddr = new CellRangeAddressList(1, 100, 4, 4);
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
