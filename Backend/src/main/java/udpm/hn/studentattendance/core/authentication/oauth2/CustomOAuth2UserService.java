package udpm.hn.studentattendance.core.authentication.oauth2;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationRoleRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserAdminRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStaffRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStudentRepository;
import udpm.hn.studentattendance.entities.Role;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.helpers.SettingHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.constants.SessionConstant;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final HttpSession httpSession;

    private final AuthenticationUserAdminRepository authenticationUserAdminRepository;

    private final AuthenticationUserStaffRepository authenticationUserStaffRepository;

    private final AuthenticationUserStudentRepository authenticationUserStudentRepository;

    private final AuthenticationRoleRepository authenticationRoleRepository;

    private final SettingHelper settingHelper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(user);

        String role = (String) httpSession.getAttribute(SessionConstant.LOGIN_ROLE);
        String facilityID = (String) httpSession.getAttribute(SessionConstant.LOGIN_FACILITY);

        customOAuth2User.setIdFacility(facilityID);

        RoleConstant roleCode;
        try {
            roleCode = RoleConstant.valueOf(role.toUpperCase());
        } catch (Exception e) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_role", "Role đăng nhập không hợp lệ: " + role, null));
        }

        Set<RoleConstant> roles = new HashSet<>();

        switch (roleCode) {

            case ADMIN:
                Optional<UserAdmin> userAdmin = authenticationUserAdminRepository
                        .findByEmail(customOAuth2User.getEmail());
                if (userAdmin.isEmpty()) {
                    throw new OAuth2AuthenticationException(
                            new OAuth2Error("login_failed", "Đăng nhập ban đào tạo thất bại", null));
                }
                customOAuth2User.setId(userAdmin.get().getId());
                customOAuth2User.setCode(userAdmin.get().getCode());
                roles.add(roleCode);
                break;

            case TEACHER:
            case STAFF:
                Optional<UserStaff> userStaff = authenticationUserStaffRepository
                        .findLoginStaff(customOAuth2User.getEmail(), roleCode, facilityID);
                if (userStaff.isEmpty()) {
                    throw new OAuth2AuthenticationException(
                            new OAuth2Error("login_failed", "Đăng nhập " + (roleCode == RoleConstant.STAFF ? "phụ trách xưởng" : "giảng viên") + " thất bại", null));
                }
                customOAuth2User.setId(userStaff.get().getId());
                customOAuth2User.setCode(userStaff.get().getCode());
                customOAuth2User.setEmailFe(userStaff.get().getEmailFe());
                customOAuth2User.setEmailFpt(userStaff.get().getEmailFpt());

                List<Role> lstRole = authenticationRoleRepository.findRolesByUserId(userStaff.get().getId());
                for (Role r : lstRole) {
                    roles.add(r.getCode());
                }

                break;

            case STUDENT:
                UserStudent userStudent = authenticationUserStudentRepository.findByEmail(customOAuth2User.getEmail())
                        .orElse(null);
                if (userStudent == null) {
                    if (!settingHelper.getSetting(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STUDENT, Boolean.class)) {
                        if (!ValidateHelper.isValidEmailFPT(customOAuth2User.getEmail())) {
                            throw new OAuth2AuthenticationException(
                                    new OAuth2Error("login_failed", "Hiện tại chỉ hỗ trợ đăng ký tài khoản mới bằng email FPT", null));
                        }
                    }
                    UserStudent newUserStudent = new UserStudent();
                    newUserStudent.setEmail(customOAuth2User.getEmail());
                    newUserStudent.setCode(customOAuth2User.getCode());
                    newUserStudent.setName(customOAuth2User.getName());
                    newUserStudent.setImage(customOAuth2User.getPicture());
                    userStudent = authenticationUserStudentRepository.save(newUserStudent);
                }
                if (userStudent.getFacility() != null
                        && !facilityID.equalsIgnoreCase(userStudent.getFacility().getId())) {
                    throw new OAuth2AuthenticationException(
                            new OAuth2Error("login_failed", "Đăng nhập sinh viên thất bại", null));
                }
                customOAuth2User.setId(userStudent.getId());
                customOAuth2User.setCode(userStudent.getCode());
                customOAuth2User
                        .setIdFacility(userStudent.getFacility() == null ? null : userStudent.getFacility().getId());
                roles.add(roleCode);
                break;

            default:
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("invalid_role", "Không tìm thấy role: " + role, null));
        }

        customOAuth2User.setRole(roles);

        return customOAuth2User;
    }

}
