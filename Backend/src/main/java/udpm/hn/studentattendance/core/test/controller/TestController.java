package udpm.hn.studentattendance.core.test.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;

@RestController
public class TestController {
    @GetMapping("/test")
    public ResponseEntity<ApiResponse> testApi() {
        return RouterHelper.createResponseApi(ApiResponse.success("Lấy api thành công"), HttpStatus.OK);
    }
}
