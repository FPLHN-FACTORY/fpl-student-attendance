package udpm.hn.studentattendance.core.authentication.services.impl;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.RedirectView;
import udpm.hn.studentattendance.core.authentication.oauth2.AuthUser;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationFacilityRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationRoleRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserAdminRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStaffRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStudentRepository;
import udpm.hn.studentattendance.core.authentication.utils.JwtUtil;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAuthenticationConstant;
import udpm.hn.studentattendance.core.authentication.services.AuthenticationService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.infrastructure.constants.SessionConstant;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final HttpSession httpSession;

    private final SessionHelper sessionHelper;

    private final AuthenticationFacilityRepository authenticationFacilityRepository;

    private final AuthenticationUserAdminRepository authenticationUserAdminRepository;

    private final AuthenticationUserStaffRepository authenticationUserStaffRepository;

    private final AuthenticationUserStudentRepository authenticationUserStudentRepository;

    @Override
    public RedirectView authorSwitch(String role, String redirectUri, String facilityId) {
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
    public AuthUser getInfoUser(String role) {
        if (role == null) {
            return null;
        }

        role = role.trim();
        String facilityID = sessionHelper.getFacilityId();
        AuthUser userData = null;

        if (role.equalsIgnoreCase(RoleConstant.ADMIN.name())) {
            Optional<UserAdmin> userAdmin = authenticationUserAdminRepository.findByEmail(sessionHelper.getUserEmail());
            if (userAdmin.isEmpty()) {
                return null;
            }
            userData = sessionHelper.buildAuthUser(userAdmin.get(), sessionHelper.getUserRole(), facilityID);
        } else if(role.equalsIgnoreCase(RoleConstant.STAFF.name()) || role.equalsIgnoreCase(RoleConstant.TEACHER.name())) {
            Optional<UserStaff> userStaff = authenticationUserStaffRepository.findLogin(sessionHelper.getUserEmailFpt(), facilityID);
            if (userStaff.isEmpty()) {
                return null;
            }
            userData = sessionHelper.buildAuthUser(userStaff.get(), sessionHelper.getUserRole(), facilityID);
        } else if (role.equalsIgnoreCase(RoleConstant.STUDENT.name())) {
            Optional<UserStudent> userStudent = facilityID != null && !facilityID.equalsIgnoreCase("null") ? authenticationUserStudentRepository.findByEmailAndFacility_Id(sessionHelper.getUserEmail(), facilityID) : authenticationUserStudentRepository.findByEmail(sessionHelper.getUserEmail());
            if (userStudent.isEmpty()) {
                return null;
            }
            userData = sessionHelper.buildAuthUser(userStudent.get(), sessionHelper.getUserRole(), facilityID);
        }

        return userData;
    }

}
