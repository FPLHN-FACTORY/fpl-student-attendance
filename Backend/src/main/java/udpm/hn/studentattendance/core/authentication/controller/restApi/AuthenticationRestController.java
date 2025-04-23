package udpm.hn.studentattendance.core.authentication.controller.restApi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationStudentRegisterRequest;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationStudentUpdateFaceIDRequest;
import udpm.hn.studentattendance.core.authentication.oauth2.AuthUser;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAuthenticationConstant;
import udpm.hn.studentattendance.core.authentication.services.AuthenticationService;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;

import java.util.Map;

@RestController
@RequestMapping(RoutesConstant.API_PREFIX)
@RequiredArgsConstructor
public class AuthenticationRestController {

    private final AuthenticationService authenticationService;

    @GetMapping(RouteAuthenticationConstant.API_GET_ALL_FACILITY)
    public ResponseEntity<ApiResponse> getAllFacility() {
        ApiResponse response = ApiResponse.success("Tải dữ liệu danh sách cơ sở thành công",
                authenticationService.getAllFacility());
        return RouterHelper.createResponseApi(response, HttpStatus.OK);
    }

    @PostMapping(RouteAuthenticationConstant.API_GET_INFO_USER)
    public ResponseEntity<ApiResponse> getInfoUser(@RequestBody Map<String, String> requestBody) {
        String role = requestBody.get("role");
        AuthUser userData = authenticationService.getInfoUser(role);

        if (userData == null) {
            ApiResponse error = ApiResponse.error("Token đăng nhập không hợp lệ hoặc đã hết hạn");
            return RouterHelper.createResponseApi(error, HttpStatus.BAD_REQUEST);
        }

        ApiResponse response = ApiResponse.success("Lấy thông tin người dùng thành công", userData);
        return RouterHelper.createResponseApi(response, HttpStatus.OK);
    }

    @PutMapping(RouteAuthenticationConstant.API_STUDENT_REGISTER)
    public ResponseEntity<?> studentRegister(@RequestBody AuthenticationStudentRegisterRequest requestBody) {
        return authenticationService.studentRegister(requestBody);
    }

    @PutMapping(RouteAuthenticationConstant.API_STUDENT_UPDATE_FACE_ID)
    public ResponseEntity<?> studentUpdateFaceID(@RequestBody AuthenticationStudentUpdateFaceIDRequest requestBody) {
        return authenticationService.studentUpdateFaceID(requestBody);
    }

    @GetMapping(RouteAuthenticationConstant.API_STUDENT_INFO)
    public ResponseEntity<?> studentInfo() {
        return authenticationService.studentInfo();
    }
}
