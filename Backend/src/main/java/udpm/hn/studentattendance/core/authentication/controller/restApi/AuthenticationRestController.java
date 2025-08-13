package udpm.hn.studentattendance.core.authentication.controller.restApi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationStudentRegisterRequest;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationToken;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAuthenticationConstant;
import udpm.hn.studentattendance.core.authentication.services.AuthenticationService;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;

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
        return authenticationService.getAvatar(requestBody.get("url_image"));
    }

    @GetMapping(RouteAuthenticationConstant.API_SETTINGS)
    public ResponseEntity<?> getSettings() {
        return authenticationService.getSettings();
    }

    @PutMapping(RouteAdminConstant.URL_API_SETTINGS)
    public ResponseEntity<?> saveSettings(@RequestBody Map<SettingKeys, String> requestBody) {
        return authenticationService.saveSettings(requestBody);
    }

    @GetMapping(RouteAuthenticationConstant.API_GET_ALL_FACILITY)
    public ResponseEntity<?> getAllFacility() {
        return authenticationService.getAllFacility();
    }

    @GetMapping(RouteAuthenticationConstant.API_GET_ALL_SEMESTER)
    public ResponseEntity<?> getAllSemester() {
        return authenticationService.getAllSemester();
    }

    @PostMapping(RouteAuthenticationConstant.API_GET_INFO_USER)
    public ResponseEntity<?> getInfoUser(@RequestBody Map<String, String> requestBody) {
        return authenticationService.getInfoUser(requestBody.get("role"));
    }

    @PutMapping(RouteAuthenticationConstant.API_STUDENT_REGISTER)
    public ResponseEntity<?> studentRegister(
            @ModelAttribute AuthenticationStudentRegisterRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return authenticationService.studentRegister(request, image);
    }

    @PutMapping(RouteAuthenticationConstant.API_STUDENT_UPDATE_FACE_ID)
    public ResponseEntity<?> studentUpdateFaceID(@RequestPart("image") MultipartFile image) {
        return authenticationService.studentUpdateFaceID(image);
    }

    @GetMapping(RouteAuthenticationConstant.API_STUDENT_INFO)
    public ResponseEntity<?> studentInfo() {
        return authenticationService.studentInfo();
    }

}
