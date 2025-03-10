package udpm.hn.studentattendance.core.authentication.services.impl;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.RedirectView;
import udpm.hn.studentattendance.core.authentication.model.response.AuthenticationInfoUserResponse;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationFacilityRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserAdminRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStaffRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAuthenticationConstant;
import udpm.hn.studentattendance.core.authentication.services.AuthenticationService;
import udpm.hn.studentattendance.core.authentication.utils.JwtUtil;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.constants.SessionConstant;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final HttpSession httpSession;

    private final AuthenticationFacilityRepository authenticationFacilityRepository;

    @Override
    public RedirectView authorSwitch(String role, String redirectUri, String facilityId) throws IOException {
        httpSession.setAttribute(SessionConstant.LOGIN_ROLE, role);
        httpSession.setAttribute(SessionConstant.LOGIN_REDIRECT, redirectUri);
        httpSession.setAttribute(SessionConstant.LOGIN_FACILITY, facilityId);

        return new RedirectView(RouteAuthenticationConstant.REDIRECT_GOOGLE_AUTHORIZATION);
    }

    @Override
    public List<Facility> getAll() {
        return authenticationFacilityRepository.getFacilitiesByStatus(EntityStatus.ACTIVE);
    }

    @Override
    public AuthenticationInfoUserResponse getInfoUser(String token) {
        return null;
    }

}
