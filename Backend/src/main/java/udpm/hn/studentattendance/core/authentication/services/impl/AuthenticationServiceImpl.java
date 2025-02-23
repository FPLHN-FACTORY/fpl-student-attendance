package udpm.hn.studentattendance.core.authentication.services.impl;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.RedirectView;
import udpm.hn.studentattendance.core.authentication.model.response.AuthenticationInfoUserResponse;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationFacilityRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserAdminRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStaffRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStudentRepository;
import udpm.hn.studentattendance.core.authentication.router.RouteAuthentication;
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

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final HttpSession httpSession;

    private final JwtUtil jwtUtil;

    private final AuthenticationFacilityRepository authenticationFacilityRepository;

    private final AuthenticationUserAdminRepository authenticationUserAdminRepository;

    private final AuthenticationUserStaffRepository authenticationUserStaffRepository;

    private final AuthenticationUserStudentRepository authenticationUserStudentRepository;

    @Override
    public RedirectView authorSwitch(String role, String redirectUri, String facilityId) throws IOException {
        httpSession.setAttribute(SessionConstant.LOGIN_ROLE, role);
        httpSession.setAttribute(SessionConstant.LOGIN_REDIRECT, redirectUri);
        httpSession.setAttribute(SessionConstant.LOGIN_FACILITY, facilityId);

        return new RedirectView(RouteAuthentication.REDIRECT_GOOGLE_AUTHORIZATION);
    }

    @Override
    public List<Facility> getAll() {
        return authenticationFacilityRepository.findAll();
    }

    @Override
    public AuthenticationInfoUserResponse getInfoUser(String token) {
        try {
            String email = jwtUtil.getEmailFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            String facilityID = jwtUtil.getFacilityFromToken(token);

            AuthenticationInfoUserResponse dataUser = new AuthenticationInfoUserResponse();

            RoleConstant roleName = RoleConstant.valueOf(role.toUpperCase());
            switch (roleName) {

                case ADMIN:
                    Optional<UserAdmin> userAdmin = authenticationUserAdminRepository.findByEmail(email);
                    if (userAdmin.isPresent()) {
                        dataUser.setId(userAdmin.get().getId());
                        dataUser.setCode(userAdmin.get().getCode());
                        dataUser.setName(userAdmin.get().getName());
                        dataUser.setEmail(userAdmin.get().getEmail());
                        dataUser.setPicture(userAdmin.get().getImage());
                        dataUser.setRole(roleName.name());
                        dataUser.setIdFacility(facilityID);
                        return dataUser;
                    }
                    break;

                case STAFF:
                    Optional<UserStaff> userStaff = authenticationUserStaffRepository.findLoginStaff(email, roleName, facilityID);
                    if (userStaff.isPresent()) {
                        dataUser.setId(userStaff.get().getId());
                        dataUser.setCode(userStaff.get().getCode());
                        dataUser.setName(userStaff.get().getName());
                        dataUser.setEmail(userStaff.get().getEmailFpt());
                        dataUser.setPicture(userStaff.get().getImage());
                        dataUser.setRole(roleName.name());
                        dataUser.setIdFacility(facilityID);
                        return dataUser;
                    }
                    break;

                case STUDENT:
                    Optional<UserStudent> userStudent = authenticationUserStudentRepository.findByEmailAndFacility_Id(email, facilityID);
                    if (userStudent.isPresent()) {
                        dataUser.setId(userStudent.get().getId());
                        dataUser.setCode(userStudent.get().getCode());
                        dataUser.setName(userStudent.get().getName());
                        dataUser.setEmail(userStudent.get().getEmail());
                        dataUser.setPicture(userStudent.get().getImage());
                        dataUser.setRole(roleName.name());
                        dataUser.setIdFacility(facilityID);
                        return dataUser;
                    }
                    break;

            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

}
