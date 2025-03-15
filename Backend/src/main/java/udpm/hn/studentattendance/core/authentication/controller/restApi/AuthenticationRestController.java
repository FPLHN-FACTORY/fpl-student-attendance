package udpm.hn.studentattendance.core.authentication.controller.restApi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.authentication.model.response.AuthenticationInfoUserResponse;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAuthenticationConstant;
import udpm.hn.studentattendance.core.authentication.services.AuthenticationService;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;

@RestController
@RequestMapping(RoutesConstant.API_PREFIX)
@RequiredArgsConstructor
public class AuthenticationRestController {

    private final AuthenticationService authenticationService;

    @GetMapping(RouteAuthenticationConstant.API_GET_ALL_FACILITY)
    public ResponseEntity<ApiResponse> getAllFacility() {
        ApiResponse response = ApiResponse.success("Tải dữ liệu danh sách cơ sở thành công", authenticationService.getAll());
        return RouterHelper.createResponseApi(response, HttpStatus.OK);
    }

    @GetMapping(RouteAuthenticationConstant.API_GET_INFO_USER + "/{token}")
    public ResponseEntity<ApiResponse> getInfoUser(@PathVariable("token") String token) {
        AuthenticationInfoUserResponse userData = authenticationService.getInfoUser(token);

        if (userData == null) {
            ApiResponse error = ApiResponse.error("Token đăng nhập không hợp lệ hoặc đã hết hạn");
            return RouterHelper.createResponseApi(error, HttpStatus.BAD_REQUEST);
        }

        ApiResponse response = ApiResponse.success("Lấy thông tin người dùng thành công", userData);
        return RouterHelper.createResponseApi(response, HttpStatus.OK);
    }

}
