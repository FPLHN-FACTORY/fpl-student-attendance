package udpm.hn.studentattendance.infrastructure.excel.service.impl;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.helpers.ExcelHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXUploadRequest;
import udpm.hn.studentattendance.infrastructure.excel.service.EXPlanDateService;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EXPlanDateServiceImpl implements EXPlanDateService {

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
        return null;
    }

    @Override
    public ResponseEntity<byte[]> downloadTemplate() {
        String filename = "template-import-plan-date.xlsx";

        List<String> headers = List.of("STT", "Ngày học diễn ra", "Ca học", "Điểm danh muộn tối đa", "Nội dung buổi học");
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

}
