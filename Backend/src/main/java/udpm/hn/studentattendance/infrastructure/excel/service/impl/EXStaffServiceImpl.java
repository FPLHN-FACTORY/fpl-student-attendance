package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.admin.userstaff.model.request.ADCreateUpdateStaffRequest;
import udpm.hn.studentattendance.core.admin.userstaff.repository.ADStaffFacilityExtendRepository;
import udpm.hn.studentattendance.core.admin.userstaff.service.ADStaffService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
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
import udpm.hn.studentattendance.infrastructure.excel.service.EXStaffService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EXStaffServiceImpl implements EXStaffService {

    private final ADStaffService staffService;
    private final EXImportLogRepository importLogRepository;
    private final EXImportLogDetailRepository importLogDetailRepository;
    private final SessionHelper sessionHelper;
    private final ADStaffFacilityExtendRepository facilityRepository;
    private final ExcelHelper excelHelper;

    private static final Map<RoleConstant, String> ENUM_TO_FRIENDLY_MAPPING = Map.of(
            RoleConstant.STAFF, "phụ trách xưởng",
            RoleConstant.TEACHER, "giảng viên"
    );

    Map<String, String> FRIENDLY_TO_ORDINAL = Map.of(
            "phụ trách xưởng", String.valueOf(RoleConstant.STAFF.ordinal()),
            "giảng viên", String.valueOf(RoleConstant.TEACHER.ordinal()),
            "phụ trách xưởng, giảng viên",
            RoleConstant.STAFF.ordinal() + "," + RoleConstant.TEACHER.ordinal()
    );

    @Override
    public ResponseEntity<?> getDataFromFile(EXUploadRequest request) {
        MultipartFile file = request.getFile();
        if (file.isEmpty()) {
            return RouterHelper.createResponseApi(ApiResponse.error("Vui lòng tải lên file Excel"), HttpStatus.BAD_GATEWAY);
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
        // Parse roles
        List<String> roleFriendlyNames = Optional.ofNullable(item.get("VAI_TRO"))
                .map(s -> Arrays.stream(s.split(",")).map(String::trim).filter(t -> !t.isEmpty()).toList())
                .orElse(Collections.emptyList());

        // Parse facility
        String facilityValue = item.get("CO_SO");
        if (facilityValue == null || facilityValue.trim().isEmpty()) {
            String msg = "Thông tin cơ sở không hợp lệ.";
            excelHelper.saveLogError(ImportLogType.STAFF, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }
        int start = facilityValue.lastIndexOf("(");
        int end = facilityValue.lastIndexOf(")");
        if (start < 0 || end < 0 || start >= end) {
            String msg = "Định dạng cơ sở không hợp lệ. Vui lòng chọn giá trị từ menu sổ xuống.";
            excelHelper.saveLogError(ImportLogType.STAFF, msg + " => " + facilityValue, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }
        String facilityCode = facilityValue.substring(start + 1, end);

        Facility facility = facilityRepository.getFacilityByCode(facilityCode);

        String code = item.get("MA_NHAN_VIEN");
        if (code == null || code.trim().isEmpty()) {
            String msg = "Không được để trống mã nhân viên";
            excelHelper.saveLogError(ImportLogType.STAFF, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }
        String nameStaff = item.get("TEN_NHAN_VIEN");
        if (nameStaff == null || nameStaff.trim().isEmpty()) {
            String msg = "Không được để trống tên nhân viên";
            excelHelper.saveLogError(ImportLogType.STAFF, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }
        String emailFe = item.get("EMAIL_FE");
        if (emailFe == null || emailFe.trim().isEmpty()) {
            String msg = "Không được để trống email fe của nhân viên";
            excelHelper.saveLogError(ImportLogType.STAFF, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }
        String emailFpt = item.get("EMAIL_FPT");
        if (emailFpt == null || emailFpt.trim().isEmpty()) {
            String msg = "Không được để trống email fpt của nhân viên";
            excelHelper.saveLogError(ImportLogType.STAFF, msg, request);
            return RouterHelper.responseError(msg, HttpStatus.BAD_REQUEST);
        }

        ADCreateUpdateStaffRequest req = new ADCreateUpdateStaffRequest();
        req.setFacilityId(facility.getId());
        req.setStaffCode(code);
        req.setName(nameStaff);
        req.setEmailFe(emailFe);
        req.setEmailFpt(emailFpt);

        List<String> roleCodes = roleFriendlyNames.stream()
                .flatMap(name -> {
                    String codeStr = FRIENDLY_TO_ORDINAL
                            .getOrDefault(name.toLowerCase(), name);
                    return Arrays.stream(codeStr.split(","))
                            .map(String::trim);
                })
                .toList();
        req.setRoleCodes(roleCodes);

        ResponseEntity<ApiResponse> result = (ResponseEntity<ApiResponse>) staffService.createStaff(req);
        ApiResponse resp = result.getBody();
        if (resp.getStatus() == RestApiStatus.SUCCESS) {
            excelHelper.saveLogSuccess(ImportLogType.STAFF, resp.getMessage(), request);
        } else {
            excelHelper.saveLogError(ImportLogType.STAFF, resp.getMessage(), request);
        }
        return result;
    }

    @Override
    public ResponseEntity<?> downloadTemplate(EXDataRequest request) {
        String filename = "template-import-staff.xlsx";
        List<String> headers = List.of("Mã Nhân Viên", "Tên Nhân Viên", "Email Fe", "Email Fpt", "Vai Trò", "Cơ Sở");

        List<String> baseRoleList = Arrays.stream(RoleConstant.values())
                .filter(r -> r != RoleConstant.STUDENT && r != RoleConstant.ADMIN)
                .map(r -> ENUM_TO_FRIENDLY_MAPPING.get(r))
                .toList();
        // 2. Khởi tạo mutable list và thêm mục kết hợp
        List<String> roleList = new ArrayList<>(baseRoleList);
        roleList.add("phụ trách xưởng, giảng viên");  // phải khớp đúng key trong FRIENDLY_TO_ORDINAL
        List<String> facilityList = facilityRepository.findAll().stream()
                .map(f -> f.getName() + " (" + f.getCode() + ")")
                .toList();

        try {
            byte[] data = createExcelStream("staff", headers, new ArrayList<>(), roleList, facilityList);
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
                importLogRepository.getListHistory(pageable, ImportLogType.STAFF.ordinal(), sessionHelper.getUserId(), sessionHelper.getFacilityId())
        );
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> historyLogDetail(EXDataRequest request, String id) {
        List<ExImportLogDetailResponse> data = importLogDetailRepository.getAllList(id, sessionHelper.getUserId(), sessionHelper.getFacilityId());
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    /**
     * Tạo file Excel mẫu với dropdown cho cột "Vai Trò" và "Cơ Sở",
     * đồng thời tô màu nền cho hàng tiêu đề.
     *
     * @param sheetName    tên sheet chính
     * @param headers      danh sách header của sheet chính
     * @param data         dữ liệu (nếu có) của sheet chính
     * @param roleList     danh sách tên vai trò (tên hiển thị thân thiện)
     * @param facilityList danh sách cơ sở với định dạng "Tên cơ sở (ID)"
     * @return mảng byte của file Excel
     * @throws IOException nếu có lỗi khi tạo file Excel
     */
    public static byte[] createExcelStream(
            String sheetName,
            List<String> headers,
            List<Map<String, String>> data,
            List<String> roleList,
            List<String> facilityList) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);

        // 1. Style cho header: màu xanh, đậm, wrap text
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setWrapText(true);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 2. Style chung cho wrap text
        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        wrapStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 3. Tạo header và gán styles
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(sheet.getDefaultRowHeightInPoints() * 1.5f); // tăng chiều cao header
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
            sheet.setDefaultColumnStyle(i, wrapStyle);
        }

        // 4. Viết dữ liệu nếu có
        int rowIndex = 1;
        for (Map<String, String> rowData : data) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(rowData.getOrDefault(headers.get(i), ""));
                cell.setCellStyle(wrapStyle);
            }
        }

        // 5. Auto-size các cột chung
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // 6. Đặt width riêng cho 2 cột Email (index 2 và 3): rộng hơn
        sheet.setColumnWidth(2, 40 * 256);
        sheet.setColumnWidth(3, 40 * 256);

        // 7. Tạo sheet DropdownData: vai trò và cơ sở
        Sheet dropdownSheet = workbook.createSheet("DropdownData");
        for (int i = 0; i < roleList.size(); i++) {
            Row row = dropdownSheet.createRow(i);
            row.createCell(0).setCellValue(roleList.get(i));
        }
        for (int i = 0; i < facilityList.size(); i++) {
            Row row = dropdownSheet.getRow(i);
            if (row == null) row = dropdownSheet.createRow(i);
            row.createCell(1).setCellValue(facilityList.get(i));
        }

        // 8. Named ranges
        Name roleName = workbook.createName();
        roleName.setNameName("RoleList");
        roleName.setRefersToFormula("DropdownData!$A$1:$A$" + roleList.size());
        Name facilityName = workbook.createName();
        facilityName.setNameName("FacilityList");
        facilityName.setRefersToFormula("DropdownData!$B$1:$B$" + facilityList.size());

        // 9. Data validation cho cột Vai Trò (index 4) và Cơ Sở (index 5)
        DataValidationHelper dvHelper = sheet.getDataValidationHelper();
        // Vai Trò
        DataValidationConstraint roleConstraint = dvHelper.createFormulaListConstraint("RoleList");
        CellRangeAddressList roleAddr = new CellRangeAddressList(1, 100, 4, 4);
        DataValidation roleValid = dvHelper.createValidation(roleConstraint, roleAddr);
        roleValid.setSuppressDropDownArrow(true);
        sheet.addValidationData(roleValid);
        // Cơ Sở
        DataValidationConstraint facilityConstraint = dvHelper.createFormulaListConstraint("FacilityList");
        CellRangeAddressList facAddr = new CellRangeAddressList(1, 100, 5, 5);
        DataValidation facValid = dvHelper.createValidation(facilityConstraint, facAddr);
        facValid.setSuppressDropDownArrow(true);
        sheet.addValidationData(facValid);

        // 10. Ẩn sheet DropdownData
        workbook.setSheetHidden(workbook.getSheetIndex(dropdownSheet), true);

        // 11. Xuất file
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            workbook.write(baos);
            workbook.close();
            return baos.toByteArray();
        }
    }
}
