package udpm.hn.studentattendance.core.authentication.controller.restApi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationStudentRegisterRequest;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationStudentUpdateFaceIDRequest;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationToken;
import udpm.hn.studentattendance.core.authentication.oauth2.AuthUser;
import udpm.hn.studentattendance.core.authentication.utils.JwtUtil;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAuthenticationConstant;
import udpm.hn.studentattendance.core.authentication.services.AuthenticationService;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;
import udpm.hn.studentattendance.utils.AppUtils;

import java.util.Map;

@RestController
@RequestMapping(RoutesConstant.API_PREFIX)
@RequiredArgsConstructor
public class AuthenticationRestController {

    private final AuthenticationService authenticationService;

    @PostMapping(RouteAuthenticationConstant.API_REFRESH_TOKEN)
    public ResponseEntity<?> refreshToken(@RequestBody AuthenticationToken request) {
        return authenticationService.refreshToken(request.getRefreshToken());
    }

    @PostMapping(RouteAuthenticationConstant.API_CONVERT_IMAGE_TO_BASE64)
    public ResponseEntity<?> getAvatar(@RequestBody Map<String, String> requestBody) {
        return authenticationService.getAvater(requestBody.get("url_image"));
    }

    @GetMapping(RouteAuthenticationConstant.API_GET_ALL_FACILITY)
    public ResponseEntity<?> getAllFacility() {
        return authenticationService.getAllFacility();
    }

    @PostMapping(RouteAuthenticationConstant.API_GET_INFO_USER)
    public ResponseEntity<?> getInfoUser(@RequestBody Map<String, String> requestBody) {
        return authenticationService.getInfoUser(requestBody.get("role"));
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
