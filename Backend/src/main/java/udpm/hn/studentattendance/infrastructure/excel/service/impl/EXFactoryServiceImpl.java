package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_FactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.repository.Staff_ProjectFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.repository.Staff_StaffFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.service.Staff_FactoryService;
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
import udpm.hn.studentattendance.infrastructure.excel.service.EXFactoryService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EXFactoryServiceImpl implements EXFactoryService {

    private final Staff_FactoryService factoryService;
    private final Staff_ProjectFactoryExtendRepository projectFactoryExtendRepository;
    private final Staff_StaffFactoryExtendRepository staffFactoryExtendRepository;
    private final EXImportLogRepository importLogRepository;
    private final EXImportLogDetailRepository importLogDetailRepository;
    private final SessionHelper sessionHelper;
    private final ExcelHelper excelHelper;

    @Override
    public ResponseEntity<?> getDataFromFile(EXUploadRequest request) {
        MultipartFile file = request.getFile();
        if (file.isEmpty()) {
            return RouterHelper.createResponseApi(ApiResponse.error("Vui lòng tải lên file excel"), HttpStatus.BAD_GATEWAY);
        }

        try {
            List<Map<String, String>> data = ExcelHelper.readFile(file);
            return RouterHelper.responseSuccess("Tải lên file excel thành công", data);
        } catch (IOException e) {
            return RouterHelper.responseError("Lỗi khi xử lý file excel", e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> importItem(EXImportRequest request) {
        Map<String, Object> data = request.getData();
        Map<String, String> item = request.getItem();

        String factoryName = item.get("TEN_NHOM_XUONG");

        String factoryDescription = item.get("MO_TA");

        String projectValue = item.get("DU_AN");
        int projectStartIndex = projectValue.lastIndexOf("(");
        int projectEndIndex = projectValue.lastIndexOf(")");
        if (projectValue == null || projectValue.trim().isEmpty()) {
            return RouterHelper.responseError("Thông tin dự án không được để trống.", HttpStatus.BAD_REQUEST);
        }
        if (projectStartIndex < 0 || projectEndIndex < 0 || projectStartIndex >= projectEndIndex) {
            return RouterHelper.responseError("Định dạng dự án không hợp lệ. Vui lòng chọn giá trị từ dropdown.", HttpStatus.BAD_REQUEST);
        }
        String projectId = projectValue.substring(projectStartIndex + 1, projectEndIndex);

        String lecturerValue = item.get("GIANG_VIEN");
        int lecturerStartIndex = lecturerValue.lastIndexOf("(");
        int lecturerEndIndex = lecturerValue.lastIndexOf(")");
        if (lecturerStartIndex < 0 || lecturerEndIndex < 0 || lecturerStartIndex >= lecturerEndIndex) {
            return RouterHelper.responseError("Định dạng giảng viên không hợp lệ. Vui lòng chọn giá trị từ dropdown.", HttpStatus.BAD_REQUEST);
        }
        String lecturerId = lecturerValue.substring(lecturerStartIndex + 1, lecturerEndIndex);

        Staff_FactoryCreateUpdateRequest createUpdateRequest = new Staff_FactoryCreateUpdateRequest();
        createUpdateRequest.setFactoryName(factoryName);
        createUpdateRequest.setFactoryDescription(factoryDescription);
        createUpdateRequest.setIdProject(projectId);
        createUpdateRequest.setIdUserStaff(lecturerId);
        factoryService.createFactory(createUpdateRequest);

        ResponseEntity<ApiResponse> result = (ResponseEntity<ApiResponse>) factoryService.createFactory(createUpdateRequest);
        ApiResponse response = result.getBody();
        if (response.getStatus() == RestApiStatus.SUCCESS) {
            excelHelper.saveLogSuccess(ImportLogType.FACTORY, response.getMessage(), request);
        } else {
            excelHelper.saveLogError(ImportLogType.FACTORY, response.getMessage(), request);
        }
        return result;
    }


    @Override
    public ResponseEntity<?> downloadTemplate(EXDataRequest request) {
        String filename = "template-import-factory.xlsx";
        List<String> headers = List.of("Tên Nhóm Xưởng", "Mô Tả", "Dự Án", "Giảng Viên");

        List<String> projectByFacility = projectFactoryExtendRepository
                .getAllProject(EntityStatus.ACTIVE, EntityStatus.ACTIVE, EntityStatus.ACTIVE, sessionHelper.getFacilityId())
                .stream()
                .map(project -> project.getName() + " (" + project.getId() + ")")
                .collect(Collectors.toList());

        List<String> staffByFacility = staffFactoryExtendRepository
                .getListUserStaff(EntityStatus.ACTIVE, EntityStatus.ACTIVE, sessionHelper.getFacilityId())
                .stream()
                .map(userStaff -> userStaff.getCode() + " (" + userStaff.getId() + ")")
                .collect(Collectors.toList());

        byte[] data;
        try {
            data = createExcelStream("factory", headers, new ArrayList<>(), projectByFacility, staffByFacility);
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
        PageableObject<ExImportLogResponse> data = PageableObject.of(
                importLogRepository.getListHistory(pageable, ImportLogType.FACTORY.ordinal(), sessionHelper.getUserId(), sessionHelper.getFacilityId())
        );
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> historyLogDetail(EXDataRequest request, String id) {
        List<ExImportLogDetailResponse> data = importLogDetailRepository.getAllList(id, sessionHelper.getUserId(), sessionHelper.getFacilityId());
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    /**
     * Tạo file Excel mẫu với dropdown cho cột "DỰ ÁN" và "GIẢNG VIÊN",
     * đồng thời tô màu nền cho hàng tiêu đề.
     *
     * @param sheetName   tên sheet chính
     * @param headers     danh sách header của sheet chính
     * @param data        dữ liệu (nếu có) của sheet chính
     * @param projectList danh sách dự án với định dạng "Tên dự án (ID)"
     * @param staffList   danh sách giảng viên (giả sử là mã code)
     * @return mảng byte của file Excel
     * @throws IOException nếu có lỗi khi tạo file Excel
     */
    public static byte[] createExcelStream(String sheetName,
                                           List<String> headers,
                                           List<Map<String, String>> data,
                                           List<String> projectList,
                                           List<String> staffList) throws IOException {

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

        // 3. (Nếu có dữ liệu) tạo các dòng dữ liệu (nếu cần)
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

        // Điền danh sách dự án vào cột A (index 0)
        for (int i = 0; i < projectList.size(); i++) {
            Row row = dropdownSheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(projectList.get(i));
        }

        // Điền danh sách giảng viên vào cột B (index 1)
        for (int i = 0; i < staffList.size(); i++) {
            Row row = dropdownSheet.getRow(i);
            if (row == null) {
                row = dropdownSheet.createRow(i);
            }
            Cell cell = row.createCell(1);
            cell.setCellValue(staffList.get(i));
        }

        // 5. Định nghĩa Named Range cho dự án và giảng viên
        Name projectName = workbook.createName();
        projectName.setNameName("ProjectList");
        projectName.setRefersToFormula("DropdownData!$A$1:$A$" + projectList.size());

        Name staffName = workbook.createName();
        staffName.setNameName("StaffList");
        staffName.setRefersToFormula("DropdownData!$B$1:$B$" + staffList.size());

        // 6. Thiết lập Data Validation cho sheet chính
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();

        // Data Validation cho cột "DỰ ÁN" (giả sử cột thứ 3, index 2)
        DataValidationConstraint projectConstraint = validationHelper.createFormulaListConstraint("ProjectList");
        CellRangeAddressList projectAddressList = new CellRangeAddressList(1, 100, 2, 2);
        DataValidation projectValidation = validationHelper.createValidation(projectConstraint, projectAddressList);
        projectValidation.setSuppressDropDownArrow(true);
        sheet.addValidationData(projectValidation);

        // Data Validation cho cột "GIẢNG VIÊN" (giả sử cột thứ 4, index 3)
        DataValidationConstraint staffConstraint = validationHelper.createFormulaListConstraint("StaffList");
        CellRangeAddressList staffAddressList = new CellRangeAddressList(1, 100, 3, 3);
        DataValidation staffValidation = validationHelper.createValidation(staffConstraint, staffAddressList);
        staffValidation.setSuppressDropDownArrow(true);
        sheet.addValidationData(staffValidation);

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
