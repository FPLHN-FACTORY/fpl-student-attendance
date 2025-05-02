package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddOrUpdatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDProjectResponse;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDFacilityShiftRepository;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanDateService;
import udpm.hn.studentattendance.entities.PlanFactory;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.ImportLogType;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogDetailResponse;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogResponse;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogDetailRepository;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogRepository;
import udpm.hn.studentattendance.infrastructure.excel.service.EXPlanDateService;
import udpm.hn.studentattendance.utils.DateTimeUtils;
import udpm.hn.studentattendance.utils.ExcelUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class EXPlanDateServiceImpl implements EXPlanDateService {

    private final SPDPlanDateService spdPlanDateService;

    private final SPDFacilityShiftRepository spdFacilityShiftRepository;

    private final EXImportLogRepository importLogRepository;

    private final EXImportLogDetailRepository importLogDetailRepository;

    private final ExcelHelper excelHelper;

    private final SessionHelper sessionHelper;

    @Override
    public ResponseEntity<ApiResponse> getDataFromFile(EXUploadRequest request) {

        MultipartFile file = request.getFile();

        if (file.isEmpty()) {
            return RouterHelper.createResponseApi(ApiResponse.error("Vui lòng tải lên file Excel"), HttpStatus.BAD_GATEWAY);
        }

        try {
            List<Map<String, String>> data = ExcelHelper.readFile(file);
            return RouterHelper.responseSuccess("Tải lên excel thành công", data);
        } catch (Exception e) {
            return RouterHelper.responseError("Lỗi khi xử lý excel", e.getMessage());
        }
    }

    @Override
    public ResponseEntity<ApiResponse> importItem(EXImportRequest request) {
        Map<String, Object> data = request.getData();
        Map<String, String> item = request.getItem();


        String idPlanFactory = (String) data.get("idPlanFactory");

        ResponseEntity<ApiResponse> planFactory = (ResponseEntity<ApiResponse>) spdPlanDateService.getDetail(idPlanFactory);
        if (planFactory.getBody().getData() == null) {
            return error("Không tìm thấy nhóm xưởng", null, request);
        }

        SPDAddOrUpdatePlanDateRequest addOrUpdatePlanDateRequest = new SPDAddOrUpdatePlanDateRequest();
        addOrUpdatePlanDateRequest.setIdPlanFactory(idPlanFactory);

        String ngayDienRa = item.get("NGAY_DIEN_RA");
        String caBatDau = item.get("CA_BAT_DAU");
        String caKetThuc = item.get("CA_KET_THUC");
        String hinhThucHoc = item.get("HINH_THUC_HOC");
        String diemDanhMuonToiDa = item.get("DIEM_DANH_MUON_TOI_DA");
        String noiDungBuoiHoc = item.get("NOI_DUNG_BUOI_HOC");
        String linkHocOnline = item.get("LINK_HOC_ONLINE");
        String checkIp = item.get("CHECK_IP");
        String checkDiaDiem = item.get("CHECK_DIA_DIEM");

        try {
            addOrUpdatePlanDateRequest.setStartDate(DateTimeUtils.convertStringToTimeMillis(ngayDienRa, "dd/MM/yyyy"));
        } catch (Exception e) {
            return error("Ngày diễn ra không hợp lệ (dd/MM/yyyy)", ngayDienRa, request);
        }

        try {
            addOrUpdatePlanDateRequest.setType(ShiftType.valueOf(hinhThucHoc).ordinal());
        } catch (Exception e) {
            return error("Hình thức học không hợp lệ (ONLINE/OFFLINE)", hinhThucHoc, request);
        }

        try {
            int startShift = Integer.parseInt(caBatDau);
            int endShift = Integer.parseInt(caKetThuc);
            if (endShift < startShift) {
                return error("Ca kết thúc phải lớn hơn ca bắt đầu", caBatDau + " - " + caKetThuc, request);
            }

            addOrUpdatePlanDateRequest.setShift(IntStream.rangeClosed(startShift, endShift)
                    .boxed()
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            return error("Ca học không hợp lệ (1, 2, 3, ...)", caBatDau + " - " + caKetThuc, request);
        }

        try {
            addOrUpdatePlanDateRequest.setLateArrival((int) Double.parseDouble(diemDanhMuonToiDa));
        } catch (Exception e) {
            return error("Thời gian điểm danh muộn tối đa không hợp lệ (14, 21, ...)", diemDanhMuonToiDa, request);
        }

        try {
            if(!StringUtils.hasText(checkIp)) {
                throw new RuntimeException();
            }
            boolean isCheckIp = Boolean.parseBoolean(checkIp);
            addOrUpdatePlanDateRequest.setRequiredIp(isCheckIp ? StatusType.ENABLE.getKey() : StatusType.DISABLE.getKey());
        } catch (Exception e) {
            return error("Check IP không hợp lệ (TRUE / FALSE)", checkIp, request);
        }

        try {
            if(!StringUtils.hasText(checkDiaDiem)) {
                throw new RuntimeException();
            }
            boolean isCheckLocation = Boolean.parseBoolean(checkDiaDiem);
            addOrUpdatePlanDateRequest.setRequiredIp(isCheckLocation ? StatusType.ENABLE.getKey() : StatusType.DISABLE.getKey());
        } catch (Exception e) {
            return error("Check địa điểm không hợp lệ (TRUE / FALSE)", checkDiaDiem, request);
        }

        addOrUpdatePlanDateRequest.setDescription(noiDungBuoiHoc);
        addOrUpdatePlanDateRequest.setLink(linkHocOnline);

        ResponseEntity<ApiResponse> result = (ResponseEntity<ApiResponse>) spdPlanDateService.addPlanDate(addOrUpdatePlanDateRequest);
        ApiResponse response = result.getBody();

        if (response.getStatus() == RestApiStatus.SUCCESS) {
            excelHelper.saveLogSuccess(ImportLogType.PLAN_DATE, response.getMessage(), request);
        } else {
            excelHelper.saveLogError(ImportLogType.PLAN_DATE, response.getMessage(), request);
        }

        return result;
    }

    @Override
    public ResponseEntity<byte[]> downloadTemplate(EXDataRequest request) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream data = new ByteArrayOutputStream()) {
            String filename = "template-import-plan-date.xlsx";
            List<String> headers = List.of("Ngày diễn ra", "Hình thức học", "Ca bắt đầu", "Ca kết thúc", "Điểm danh muộn tối đa", "Nội dung buổi học", "Link học online", "Check IP", "Check địa điểm");

            int firstRow = 1;
            int lastRow = 500;

            List<String> lstShiftType = Arrays.stream(ShiftType.values())
                    .map(Enum::name)
                    .toList();

            List<Integer> lstShift = spdFacilityShiftRepository.getAllShift(sessionHelper.getFacilityId());

            Sheet templateSheet = ExcelUtils.createTemplate(workbook, "Data Import", headers, new ArrayList<>());
            ExcelUtils.addDateValidation(templateSheet, firstRow, lastRow, 0, "dd/MM/yyyy", "01/01/1900", "31/12/9999");
            ExcelUtils.addListValidation(templateSheet, firstRow, lastRow, 1, lstShiftType);
            ExcelUtils.addListValidation(templateSheet, firstRow, lastRow, 2, lstShift);
            ExcelUtils.addListValidation(templateSheet, firstRow, lastRow, 3, lstShift);
            ExcelUtils.addListValidation(templateSheet, firstRow, lastRow, 7, List.of(true, false));
            ExcelUtils.addListValidation(templateSheet, firstRow, lastRow, 8, List.of(true, false));
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
        PageableObject<ExImportLogResponse> data = PageableObject.of(importLogRepository.getListHistory(pageable, ImportLogType.PLAN_DATE.ordinal(), sessionHelper.getUserId(), sessionHelper.getFacilityId()));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> historyLogDetail(EXDataRequest request, String id) {
        List<ExImportLogDetailResponse> data = importLogDetailRepository.getAllList(id, sessionHelper.getUserId(), sessionHelper.getFacilityId());
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    private ResponseEntity<ApiResponse> error(String message, Object data, EXImportRequest request) {
        if (data != null) {
            message += " => " + data;
        }
        excelHelper.saveLogError(ImportLogType.PLAN_DATE, message, request);
        return RouterHelper.responseError(message);
    }

}
