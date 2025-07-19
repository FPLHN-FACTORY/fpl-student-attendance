package udpm.hn.studentattendance.core.authentication.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.view.RedirectView;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationStudentRegisterRequest;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationStudentUpdateFaceIDRequest;
import udpm.hn.studentattendance.core.authentication.oauth2.AuthUser;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface AuthenticationService {

    RedirectView authorSwitch(String role, String redirectUri, String facilityId) throws IOException;

    ResponseEntity<?> getAllFacility();

    ResponseEntity<?> getInfoUser(String role);

    ResponseEntity<?> studentRegister(AuthenticationStudentRegisterRequest request);

    ResponseEntity<?> studentInfo();

    ResponseEntity<?> studentUpdateFaceID(AuthenticationStudentUpdateFaceIDRequest requestBody);

    ResponseEntity<?> refreshToken(String refreshToken);

    ResponseEntity<?> getAvatar(String urlImage);

    ResponseEntity<?> getAllSemester();

    ResponseEntity<?> getSettings();

    ResponseEntity<?> saveSettings(Map<SettingKeys, String> settings);

}
