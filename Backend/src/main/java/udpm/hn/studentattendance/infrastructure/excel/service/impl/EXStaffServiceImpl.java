package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.admin.user_staff.model.request.Admin_CreateUpdateStaffRequest;
import udpm.hn.studentattendance.core.admin.user_staff.repository.Admin_StaffFacilityRepository;
import udpm.hn.studentattendance.core.admin.user_staff.service.Admin_StaffService;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EXStaffServiceImpl implements EXStaffService {

    private final Admin_StaffService staffService;
    private final EXImportLogRepository importLogRepository;
    private final EXImportLogDetailRepository importLogDetailRepository;
    private final SessionHelper sessionHelper;
    private final Admin_StaffFacilityRepository facilityRepository;
    private final ExcelHelper excelHelper;

    private static final Map<RoleConstant, String> ENUM_TO_FRIENDLY_MAPPING = Map.of(
            RoleConstant.ADMIN, "ban đào tạo",
            RoleConstant.STAFF, "phụ trách xưởng",
            RoleConstant.TEACHER, "giảng viên"
    );

    private static final Map<String, String> FRIENDLY_TO_ENUM_MAPPING = Map.of(
            "ban đào tạo", "ADMIN",
            "phụ trách xưởng", "STAFF",
            "giảng viên", "TEACHER"
    );

    @Override
    public ResponseEntity<?> getDataFromFile(EXUploadRequest request) {
        MultipartFile file = request.getFile();
        if (file.isEmpty()) {
            return RouterHelper.responseError("Vui lòng tải lên file Excel", HttpStatus.BAD_GATEWAY);
        }
        try {
            List<Map<String, String>> data = ExcelHelper.readFile(file);
            return RouterHelper.responseSuccess("Tải lên excel thành công", data);
        } catch (IOException e) {
            return RouterHelper.responseError("Lỗi khi xử lý excel", e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> importItem(EXImportRequest request) {
        Map<String, Object> data = request.getData();
        Map<String, String> item = request.getItem();

        List<String> roleFriendlyNames = new ArrayList<>();
        String vaiTro = item.get("VAI_TRO");
        if (vaiTro != null && !vaiTro.trim().isEmpty()) {
            roleFriendlyNames = Arrays.stream(vaiTro.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        String facilityValue = item.get("CO_SO");
        if (facilityValue == null || facilityValue.trim().isEmpty()) {
            return RouterHelper.responseError("Thông tin cơ sở không hợp lệ.", HttpStatus.BAD_REQUEST);
        }
        // Tách lấy id từ chuỗi theo định dạng "Tên cơ sở (ID)"
        int startIndex = facilityValue.lastIndexOf("(");
        int endIndex = facilityValue.lastIndexOf(")");
        if (startIndex < 0 || endIndex < 0 || startIndex >= endIndex) {
            return RouterHelper.responseError("Định dạng cơ sở không hợp lệ. Vui lòng chọn giá trị từ menu sổ xuống.", HttpStatus.BAD_REQUEST);
        }
        String facilityId = facilityValue.substring(startIndex + 1, endIndex);

        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Facility với id: " + facilityId));

        Admin_CreateUpdateStaffRequest createUpdateStaffRequest = new Admin_CreateUpdateStaffRequest();
        createUpdateStaffRequest.setFacilityId(facility.getId());
        createUpdateStaffRequest.setStaffCode(item.get("MA_NHAN_VIEN"));
        createUpdateStaffRequest.setEmailFe(item.get("EMAIL_FE"));
        createUpdateStaffRequest.setEmailFpt(item.get("EMAIL_FPT"));
        createUpdateStaffRequest.setName(item.get("TEN_NHAN_VIEN"));

        List<String> roleCodes = roleFriendlyNames.stream()
                .map(name -> FRIENDLY_TO_ENUM_MAPPING.getOrDefault(name.toLowerCase(), name))
                .collect(Collectors.toList());
        createUpdateStaffRequest.setRoleCodes(roleCodes);

        ResponseEntity<ApiResponse> result = (ResponseEntity<ApiResponse>) staffService.createStaff(createUpdateStaffRequest);
        ApiResponse response = result.getBody();
        if (response.getStatus() == RestApiStatus.SUCCESS) {
            excelHelper.saveLogSuccess(ImportLogType.STAFF, response.getMessage(), request);
        } else {
            excelHelper.saveLogError(ImportLogType.STAFF, response.getMessage(), request);
        }
        return result;
    }

    @Override
    public ResponseEntity<?> downloadTemplate(EXDataRequest request) {
        String filename = "template-import-staff.xlsx";

        List<String> headers = List.of("Mã Nhân Viên", "Tên Nhân Viên", "Email Fe", "Email Fpt", "Vai Trò", "Cơ Sở");

        List<String> roleList = Arrays.stream(RoleConstant.values())
                .filter(role -> role != RoleConstant.STUDENT)
                .map(role -> ENUM_TO_FRIENDLY_MAPPING.get(role))
                .collect(Collectors.toList());

        List<String> facilityList = facilityRepository.findAll()
                .stream()
                .map(facility -> facility.getName() + " (" + facility.getId() + ")")
                .collect(Collectors.toList());

        byte[] data;
        try {
            data = createExcelStream("staff", headers, new ArrayList<>(), roleList, facilityList);
        } catch (IOException e) {
            return RouterHelper.responseError("Không thể tạo file mẫu", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        HttpHeaders headersHttp = new HttpHeaders();
        headersHttp.setContentDisposition(ContentDisposition.builder("attachment").filename(filename).build());
        headersHttp.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        headersHttp.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return new ResponseEntity<>(data, headersHttp, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> historyLog(EXDataRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<ExImportLogResponse> data = PageableObject.of(importLogRepository.getListHistory(pageable, ImportLogType.STAFF.ordinal(), sessionHelper.getUserId(), sessionHelper.getFacilityId()));
        System.out.println(sessionHelper.getFacilityId());
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
    public static byte[] createExcelStream(String sheetName,
                                           List<String> headers,
                                           List<Map<String, String>> data,
                                           List<String> roleList,
                                           List<String> facilityList) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);

        // 1. Tạo CellStyle cho header (màu xanh nhạt)
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Tạo font đậm cho header
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // 2. Tạo hàng tiêu đề
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }

        // 3. (Nếu có dữ liệu) tạo các dòng dữ liệu ở đây...
        // Ví dụ:
        /*
        int rowIndex = 1;
        for (Map<String, String> rowData : data) {
            Row row = sheet.createRow(rowIndex++);
            // Điền dữ liệu từng cột...
        }
        */

        // 4. Tạo sheet phụ để chứa danh sách dropdown
        Sheet dropdownSheet = workbook.createSheet("DropdownData");

        // Điền danh sách vai trò vào cột A (index 0)
        for (int i = 0; i < roleList.size(); i++) {
            Row row = dropdownSheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(roleList.get(i));
        }

        // Điền danh sách cơ sở vào cột B (index 1)
        for (int i = 0; i < facilityList.size(); i++) {
            Row row = dropdownSheet.getRow(i);
            if (row == null) {
                row = dropdownSheet.createRow(i);
            }
            Cell cell = row.createCell(1);
            cell.setCellValue(facilityList.get(i));
        }

        // 5. Định nghĩa Named Range cho vai trò và cơ sở
        Name roleName = workbook.createName();
        roleName.setNameName("RoleList");
        roleName.setRefersToFormula("DropdownData!$A$1:$A$" + roleList.size());

        Name facilityName = workbook.createName();
        facilityName.setNameName("FacilityList");
        facilityName.setRefersToFormula("DropdownData!$B$1:$B$" + facilityList.size());

        // 6. Thiết lập Data Validation cho sheet chính
        // Giả sử cột "Vai Trò" là cột thứ 5 (index 4) và "Cơ Sở" là cột thứ 6 (index 5)
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();

        // Data Validation cho cột "Vai Trò"
        DataValidationConstraint roleConstraint = validationHelper.createFormulaListConstraint("RoleList");
        CellRangeAddressList roleAddressList = new CellRangeAddressList(1, 100, 4, 4);
        DataValidation roleValidation = validationHelper.createValidation(roleConstraint, roleAddressList);
        roleValidation.setSuppressDropDownArrow(true);
        sheet.addValidationData(roleValidation);

        // Data Validation cho cột "Cơ Sở"
        DataValidationConstraint facilityConstraint = validationHelper.createFormulaListConstraint("FacilityList");
        CellRangeAddressList facilityAddressList = new CellRangeAddressList(1, 100, 5, 5);
        DataValidation facilityValidation = validationHelper.createValidation(facilityConstraint, facilityAddressList);
        facilityValidation.setSuppressDropDownArrow(true);
        sheet.addValidationData(facilityValidation);

        // 7. Ẩn sheet DropdownData
        int dropdownSheetIndex = workbook.getSheetIndex(dropdownSheet);
        workbook.setSheetHidden(dropdownSheetIndex, true);

        // 8. Xuất Workbook thành mảng byte
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }
}
