package udpm.hn.studentattendance.core.authentication.services;

import org.springframework.web.servlet.view.RedirectView;
import udpm.hn.studentattendance.core.authentication.oauth2.AuthUser;
import udpm.hn.studentattendance.entities.Facility;

import java.io.IOException;
import java.util.List;

public interface AuthenticationService {

    RedirectView authorSwitch(String role, String redirectUri, String facilityId) throws IOException;

    List<Facility> getAll();

    AuthUser getInfoUser(String role);

}
