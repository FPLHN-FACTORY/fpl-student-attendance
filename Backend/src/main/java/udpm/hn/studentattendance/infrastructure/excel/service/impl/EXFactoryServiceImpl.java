package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.admin.userstaff.model.response.ADStaffResponse;
import udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.response.USFactoryResponse;
import udpm.hn.studentattendance.core.staff.factory.repository.factory.USFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.repository.factory.USProjectFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.repository.factory.USStaffFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.service.USFactoryService;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.ImportLogType;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogDetailResponse;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogResponse;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogDetailRepository;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogRepository;
import udpm.hn.studentattendance.infrastructure.excel.service.EXFactoryService;
import udpm.hn.studentattendance.utils.ExcelUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EXFactoryServiceImpl implements EXFactoryService {

    private final USFactoryService factoryService;
    private final USProjectFactoryExtendRepository projectFactoryExtendRepository;
    private final USStaffFactoryExtendRepository staffFactoryExtendRepository;
    private final EXImportLogRepository importLogRepository;
    private final EXImportLogDetailRepository importLogDetailRepository;
    private final SessionHelper sessionHelper;
    private final ExcelHelper excelHelper;

    private final USFactoryExtendRepository factoryExtendRepository;

    @Override
    public ResponseEntity<?> getDataFromFile(EXUploadRequest request) {
        MultipartFile file = request.getFile();
        if (file.isEmpty()) {
            return RouterHelper.createResponseApi(ApiResponse.error("Vui lòng tải lên file excel"), HttpStatus.BAD_GATEWAY);
        }
        try {
            List<Map<String, String>> raw = ExcelHelper.readFile(file);
            // Chỉ giữ dòng nào có TEN_NHOM_XUONG
            List<Map<String, String>> data = raw.stream()
                    .filter(row -> {
                        String name = row.get("TEN_NHOM_XUONG");
                        return name != null && !name.trim().isEmpty();
                    })
                    .collect(Collectors.toList());
            return RouterHelper.responseSuccess("Tải lên file excel thành công", data);
        } catch (IOException e) {
            return RouterHelper.responseError("Lỗi khi xử lý file excel", e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> importItem(EXImportRequest request) {
        Map<String, String> item = request.getItem();

        // Tên nhóm xưởng
        String factoryName = item.get("TEN_NHOM_XUONG");
        if (factoryName == null || factoryName.trim().isEmpty()) {
            String msg = "Tên nhóm xưởng không được để trống.";
            excelHelper.saveLogError(ImportLogType.FACTORY, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }
        String factoryDescription = item.get("MO_TA");

        // Giảng viên: giữ nguyên parse từ "Name (Code)"
        String lecturerValue = item.get("GIANG_VIEN");
        if (lecturerValue == null || lecturerValue.trim().isEmpty()) {
            String msg = "Thông tin giảng viên không được để trống.";
            excelHelper.saveLogError(ImportLogType.FACTORY, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }
        int lecturerStart = lecturerValue.lastIndexOf("(");
        int lecturerEnd = lecturerValue.lastIndexOf(")");
        if (lecturerStart < 0 || lecturerEnd < 0 || lecturerStart >= lecturerEnd) {
            String msg = "Định dạng giảng viên không hợp lệ. Vui lòng chọn giá trị từ menu sổ xuống.";
            excelHelper.saveLogError(ImportLogType.FACTORY, msg + " => " + lecturerValue, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }
        String lectureCode = lecturerValue.substring(lecturerStart + 1, lecturerEnd);
        Optional<UserStaff> userStaff = staffFactoryExtendRepository.findUserStaffByCode(lectureCode);
        if (userStaff.isEmpty()) {
            String msg = "Giảng viên không tồn tại: " + lecturerValue;
            excelHelper.saveLogError(ImportLogType.FACTORY, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }

        // Lấy projectId từ cột ẩn
        String projectId = item.get("DAYLADUAN");
        if (projectId == null || projectId.trim().isEmpty()) {
            String msg = "Thông tin dự án không được để trống.";
            excelHelper.saveLogError(ImportLogType.FACTORY, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }

        USFactoryCreateUpdateRequest createReq = new USFactoryCreateUpdateRequest();
        createReq.setFactoryName(factoryName);
        createReq.setFactoryDescription(factoryDescription);
        createReq.setIdProject(projectId);
        createReq.setIdUserStaff(userStaff.get().getId());

        @SuppressWarnings("unchecked")
        ResponseEntity<ApiResponse> result = (ResponseEntity<ApiResponse>) factoryService.createFactory(createReq);
        ApiResponse resp = result.getBody();
        if (resp.getStatus() == RestApiStatus.SUCCESS) {
            excelHelper.saveLogSuccess(ImportLogType.FACTORY, resp.getMessage(), request);
        } else {
            excelHelper.saveLogError(ImportLogType.FACTORY, resp.getMessage(), request);
        }
        return result;
    }

    @Override
    public ResponseEntity<?> exportData(EXDataRequest request) {
        List<USFactoryResponse> list = factoryExtendRepository.exportAllFactory(sessionHelper.getFacilityId());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream data = new ByteArrayOutputStream()) {
            String filename =
                    "Factory" + ".xlsx";
            List<String> headers = List.of("STT", "Tên nhóm xưởng ", "Dự án", "Bộ môn", "Giảng viên", "Mô tả");

            Sheet sheet = ExcelUtils.createTemplate(workbook, "Data Export", headers, new ArrayList<>());

            for (int i = 0; i < list.size(); i++) {
                int row = i + 1;
                USFactoryResponse factoryResponse = list.get(i);
                String index = String.valueOf(row);
                String name = factoryResponse.getName();
                String projectName = factoryResponse.getProjectName();
                String subjectCode = factoryResponse.getSubjectCode();
                String staffName = factoryResponse.getStaffName();
                String factoryDescription = factoryResponse.getFactoryDescription();

                List<Object> dataCell = List.of(index, name, projectName, subjectCode, staffName, factoryDescription);
                ExcelUtils.insertRow(sheet, row, dataCell);
            }
            sheet.setColumnWidth(2, 30 * 256);
            sheet.setColumnWidth(3, 40 * 256);
            sheet.setColumnWidth(4, 40 * 256);
            sheet.setColumnWidth(5, 30 * 256);
            sheet.setColumnWidth(6, 30 * 256);
            workbook.write(data);

            HttpHeaders headersHttp = new HttpHeaders();
            headersHttp.setContentDisposition(ContentDisposition.builder("attachment").filename(filename).build());
            headersHttp.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
            headersHttp.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return new ResponseEntity<>(data.toByteArray(), headersHttp, HttpStatus.OK);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public ResponseEntity<?> downloadTemplate(EXDataRequest request) {
        String filename = "template-import-factory.xlsx";
        List<String> headers = List.of("Tên Nhóm Xưởng", "Mô Tả", "Dự Án", "Giảng Viên");

        var projects = projectFactoryExtendRepository.getAllProject(sessionHelper.getFacilityId());
        List<String> projectNames = projects.stream().map(p -> p.getProjectName()).collect(Collectors.toList());
        List<String> projectIds = projects.stream().map(p -> p.getId()).collect(Collectors.toList());

        List<String> staffList = staffFactoryExtendRepository.getListUserStaff(
                EntityStatus.ACTIVE, EntityStatus.ACTIVE,
                sessionHelper.getFacilityId(), RoleConstant.TEACHER)
                .stream().map(s -> s.getName() + " (" + s.getCode() + ")")
                .collect(Collectors.toList());

        try {
            byte[] data = createExcelStream("factory", headers, new ArrayList<>(),
                    projectNames, projectIds, staffList);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentDisposition(ContentDisposition.builder("attachment").filename(filename).build());
            httpHeaders.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
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
                        pageable, ImportLogType.FACTORY.ordinal(),
                        sessionHelper.getUserId(), sessionHelper.getFacilityId()));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> historyLogDetail(EXDataRequest request, String id) {
        List<ExImportLogDetailResponse> data = importLogDetailRepository
                .getAllList(id, sessionHelper.getUserId(), sessionHelper.getFacilityId());
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    /**
     * Tạo file Excel mẫu với dropdown chỉ hiển thị tên dự án,
     * lấy id ngầm qua cột ẩn; staff giữ nguyên dạng "Name (Code)".
     */
    public static byte[] createExcelStream(
            String sheetName,
            List<String> headers,
            List<Map<String, String>> data,
            List<String> projectNames,
            List<String> projectIds,
            List<String> staffList) throws IOException {

        final int MAX_ROWS = 100;
        Workbook workbook = new XSSFWorkbook();
        // ngay sau khi tạo workbook, trước khi write ra ByteArrayOutputStream
        workbook.setForceFormulaRecalculation(true);
        Sheet sheet = workbook.createSheet(sheetName);

        // Style header
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setWrapText(true);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        wrapStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Header chính
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(sheet.getDefaultRowHeightInPoints() * 1.5f);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
            sheet.setDefaultColumnStyle(i, wrapStyle);
        }

        // Thêm cột ẩn ProjectID
        int projectIdColIdx = headers.size();
        Cell projIdHeader = headerRow.createCell(projectIdColIdx);
        projIdHeader.setCellValue("DAY_LA_DU_AN");
        projIdHeader.setCellStyle(headerStyle);
        sheet.setDefaultColumnStyle(projectIdColIdx, wrapStyle);

        // Dữ liệu mẫu (nếu có)
        int rowIndex = 1;
        for (Map<String, String> rowData : data) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(rowData.getOrDefault(headers.get(i), ""));
                cell.setCellStyle(wrapStyle);
            }
        }

        // Kích thước cột
        for (int i = 0; i < headers.size(); i++) sheet.autoSizeColumn(i);
        sheet.setColumnWidth(1, 30 * 256);
        sheet.setColumnWidth(2, 30 * 256);
        sheet.setColumnWidth(3, 20 * 256);

        // Sheet DropdownData
        Sheet dropdownSheet = workbook.createSheet("DropdownData");
        for (int i = 0; i < projectNames.size(); i++) {
            Row row = dropdownSheet.createRow(i);
            row.createCell(0).setCellValue(projectNames.get(i));
            row.createCell(1).setCellValue(projectIds.get(i));
        }
        for (int i = 0; i < staffList.size(); i++) {
            Row row = dropdownSheet.getRow(i);
            if (row == null) row = dropdownSheet.createRow(i);
            row.createCell(2).setCellValue(staffList.get(i));
        }

        // Named ranges
        Name projectNameList = workbook.createName();
        projectNameList.setNameName("ProjectNameList");
        projectNameList.setRefersToFormula("DropdownData!$A$1:$A$" + projectNames.size());
        Name projectIdList = workbook.createName();
        projectIdList.setNameName("ProjectIdList");
        projectIdList.setRefersToFormula("DropdownData!$B$1:$B$" + projectIds.size());
        Name staffNameList = workbook.createName();
        staffNameList.setNameName("StaffList");
        staffNameList.setRefersToFormula("DropdownData!$C$1:$C$" + staffList.size());

        DataValidationHelper dvHelper = sheet.getDataValidationHelper();
        // Project dropdown chỉ tên
        DataValidationConstraint projCons = dvHelper.createFormulaListConstraint("ProjectNameList");
        CellRangeAddressList projAddr = new CellRangeAddressList(1, MAX_ROWS, 2, 2);
        DataValidation projValid = dvHelper.createValidation(projCons, projAddr);
        projValid.setSuppressDropDownArrow(true);
        sheet.addValidationData(projValid);

        // Staff dropdown giữ nguyên
        DataValidationConstraint staffCons = dvHelper.createFormulaListConstraint("StaffList");
        CellRangeAddressList staffAddr = new CellRangeAddressList(1, MAX_ROWS, 3, 3);
        DataValidation staffValid = dvHelper.createValidation(staffCons, staffAddr);
        staffValid.setSuppressDropDownArrow(true);
        sheet.addValidationData(staffValid);

        // Công thức lookup ProjectID
        for (int r = 1; r <= MAX_ROWS; r++) {
            Row row = sheet.getRow(r);
            if (row == null) row = sheet.createRow(r);
            String excelRow = String.valueOf(r + 1);
            Cell pidCell = row.createCell(projectIdColIdx);
            pidCell.setCellFormula(String.format(
                    "IF($C$%s<>\"\",VLOOKUP($C$%s,DropdownData!$A$1:$B$%d,2,FALSE),\"\")",
                    excelRow, excelRow, projectNames.size()));
            pidCell.setCellStyle(wrapStyle);
        }

        // Ẩn cột ẩn và sheet phụ
        sheet.setColumnHidden(projectIdColIdx, true);
        workbook.setSheetHidden(workbook.getSheetIndex(dropdownSheet), true);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            workbook.write(baos);
            workbook.close();
            return baos.toByteArray();
        }
    }
}
