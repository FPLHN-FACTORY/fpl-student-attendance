package udpm.hn.studentattendance.helpers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.core.authentication.oauth2.AuthUser;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.constants.SessionConstant;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class SessionHelper {

    private final HttpSession httpSession;

    private static AuthUser authUser;

    private static RoleConstant loginRole;

    public AuthUser getCurrentUser() {
        return (AuthUser) httpSession.getAttribute(SessionConstant.AUTH_USER);
    }

    public void setCurrentUser(AuthUser user) {
        httpSession.setAttribute(SessionConstant.AUTH_USER, user);
        authUser = user;
    }

    public AuthUser buildAuthUser(UserAdmin user, Set<RoleConstant> role, String idFacility) {
        AuthUser authUser = new AuthUser();
        authUser.setId(user.getId());
        authUser.setCode(user.getCode());
        authUser.setName(user.getName());
        authUser.setEmail(user.getEmail());
        authUser.setRole(role);
        authUser.setIdFacility(idFacility);
        authUser.setPicture(user.getImage());
        return authUser;
    }

    public AuthUser buildAuthUser(UserStaff user, Set<RoleConstant> role, String idFacility) {
        AuthUser authUser = new AuthUser();
        authUser.setId(user.getId());
        authUser.setCode(user.getCode());
        authUser.setName(user.getName());
        authUser.setEmail(user.getEmailFpt());
        authUser.setEmailFe(user.getEmailFe());
        authUser.setEmailFpt(user.getEmailFpt());
        authUser.setRole(role);
        authUser.setIdFacility(idFacility);
        authUser.setPicture(user.getImage());
        return authUser;
    }

    public AuthUser buildAuthUser(UserStudent user, Set<RoleConstant> role, String idFacility) {
        AuthUser authUser = new AuthUser();
        authUser.setId(user.getId());
        authUser.setCode(user.getCode());
        authUser.setName(user.getName());
        authUser.setEmail(user.getEmail());
        authUser.setRole(role);
        authUser.setIdFacility(idFacility);
        authUser.setPicture(user.getImage());
        return authUser;
    }

    public String getUserId() {
        return authUser.getId();
    }

    public String getUserName() {
        return authUser.getName();
    }

    public Set<RoleConstant> getUserRole() {
        return authUser.getRole();
    }

    public String getFacilityId() {
        return authUser.getIdFacility();
    }

    public String getUserEmail() {
        return authUser.getEmail();
    }

    public String getUserEmailFe() {
        return authUser.getEmailFe();
    }

    public String getUserEmailFpt() {
        return authUser.getEmailFpt();
    }

    public String getUserPicture() {
        return authUser.getPicture();
    }

    public String getUserCode() {
        return authUser.getCode();
    }

    public RoleConstant getLoginRole() {
        return loginRole;
    }

    public void setLoginRole(RoleConstant role) {
        loginRole = role;
    }

}
