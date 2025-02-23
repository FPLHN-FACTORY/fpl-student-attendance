package udpm.hn.studentattendance.core.authentication.oauth2;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserAdminRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStaffRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStudentRepository;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.constants.SessionConstant;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final HttpSession httpSession;

    private final AuthenticationUserAdminRepository authenticationUserAdminRepository;

    private final AuthenticationUserStaffRepository authenticationUserStaffRepository;

    private final AuthenticationUserStudentRepository authenticationUserStudentRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(user);

        String role = (String) httpSession.getAttribute(SessionConstant.LOGIN_ROLE);
        String facilityID = (String) httpSession.getAttribute(SessionConstant.LOGIN_FACILITY);

        RoleConstant roleCode;
        try {
             roleCode = RoleConstant.valueOf(role.toUpperCase());
        } catch (Exception e) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_role", "Role đăng nhập không hợp lệ: " + role, null));
        }

        switch (roleCode) {

            case ADMIN:
                Optional<UserAdmin> userAdmin = authenticationUserAdminRepository.findByEmail(customOAuth2User.getEmail());
                if (userAdmin.isEmpty()) {
                    throw new OAuth2AuthenticationException(new OAuth2Error("login_failed", "Đăng nhập ban đào tạo thất bại", null));
                }
                customOAuth2User.setId(userAdmin.get().getId());
                customOAuth2User.setCode(userAdmin.get().getCode());
                break;

            case STAFF:
                Optional<UserStaff> userStaff = authenticationUserStaffRepository.findLoginStaff(customOAuth2User.getEmail(), roleCode, facilityID);
                if (userStaff.isEmpty()) {
                    throw new OAuth2AuthenticationException(new OAuth2Error("login_failed", "Đăng nhập giảng viên thất bại", null));
                }
                customOAuth2User.setId(userStaff.get().getId());
                customOAuth2User.setCode(userStaff.get().getCode());
                customOAuth2User.setEmailFe(userStaff.get().getEmailFe());
                customOAuth2User.setEmailFPT(userStaff.get().getEmailFpt());
                break;

            case STUDENT:
                Optional<UserStudent> userStudent = authenticationUserStudentRepository.findByEmailAndFacility_Id(customOAuth2User.getEmail(), facilityID);
                if (userStudent.isEmpty()) {
                    throw new OAuth2AuthenticationException(new OAuth2Error("login_failed", "Đăng nhập sinh viên thất bại", null));
                }
                customOAuth2User.setId(userStudent.get().getId());
                customOAuth2User.setCode(userStudent.get().getCode());
                break;

            default:
                throw new OAuth2AuthenticationException(new OAuth2Error("invalid_role", "Không tìm thấy role: " + role, null));
        }

        customOAuth2User.setRole(role);
        customOAuth2User.setIdFacility(facilityID);
        return customOAuth2User;
    }


}
