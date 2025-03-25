package udpm.hn.studentattendance.core.authentication.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.view.RedirectView;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationStudentRegisterRequest;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationStudentUpdateFaceIDRequest;
import udpm.hn.studentattendance.core.authentication.oauth2.AuthUser;
import udpm.hn.studentattendance.entities.Facility;

import java.io.IOException;
import java.util.List;

public interface AuthenticationService {

    RedirectView authorSwitch(String role, String redirectUri, String facilityId) throws IOException;

    List<Facility> getAllFacility();

    AuthUser getInfoUser(String role);

    ResponseEntity<?> studentRegister(AuthenticationStudentRegisterRequest request);

    ResponseEntity<?> studentInfo();

    ResponseEntity<?> studentUpdateFaceID(AuthenticationStudentUpdateFaceIDRequest requestBody);

}
