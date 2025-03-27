package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddOrUpdatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDProjectResponse;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanDateService;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.ImportLogType;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogDetailResponse;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogResponse;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogDetailRepository;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogRepository;
import udpm.hn.studentattendance.infrastructure.excel.service.EXPlanDateService;
import udpm.hn.studentattendance.utils.DateTimeUtils;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EXPlanDateServiceImpl implements EXPlanDateService {

    private final SPDPlanDateService spdPlanDateService;

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

        SPDAddOrUpdatePlanDateRequest addOrUpdatePlanDateRequest = new SPDAddOrUpdatePlanDateRequest();
        addOrUpdatePlanDateRequest.setIdPlanFactory(idPlanFactory);
        addOrUpdatePlanDateRequest.setStartDate(DateTimeUtils.convertStringToTimeMillis(item.get("NGAY_DIEN_RA")));
        addOrUpdatePlanDateRequest.setShift((int) Double.parseDouble(item.get("CA_HOC")));
        addOrUpdatePlanDateRequest.setType(ShiftType.valueOf(item.get("HINH_THUC_HOC")).ordinal());
        addOrUpdatePlanDateRequest.setDescription(item.get("NOI_DUNG_BUOI_HOC"));
        addOrUpdatePlanDateRequest.setLateArrival((int) Double.parseDouble(item.get("DIEM_DANH_MUON_TOI_DA")));

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
        String filename = "template-import-plan-date.xlsx";

        List<String> headers = List.of("Ngày diễn ra", "Hình thức học", "Ca học", "Điểm danh muộn tối đa", "Nội dung buổi học");
        byte[] data = ExcelHelper.createExcelStream("plan-date", headers, new ArrayList<>());
        if (data == null) {
            return null;
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
        PageableObject<ExImportLogResponse> data = PageableObject.of(importLogRepository.getListHistory(pageable, ImportLogType.PLAN_DATE.ordinal(), sessionHelper.getUserId(), sessionHelper.getFacilityId()));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> historyLogDetail(EXDataRequest request, String id) {
        List<ExImportLogDetailResponse> data = importLogDetailRepository.getAllList(id, sessionHelper.getUserId(), sessionHelper.getFacilityId());
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

}
