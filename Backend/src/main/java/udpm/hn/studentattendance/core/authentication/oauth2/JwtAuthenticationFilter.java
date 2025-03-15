package udpm.hn.studentattendance.core.authentication.oauth2;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationRoleRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserAdminRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStaffRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStudentRepository;
import udpm.hn.studentattendance.core.authentication.utils.JwtUtil;
import udpm.hn.studentattendance.entities.Role;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final SessionHelper sessionHelper;

    private final AuthenticationUserAdminRepository authenticationUserAdminRepository;

    private final AuthenticationUserStaffRepository authenticationUserStaffRepository;

    private final AuthenticationUserStudentRepository authenticationUserStudentRepository;

    private final AuthenticationRoleRepository authenticationRoleRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.getEmailFromToken(token);
                Set<String> role = jwtUtil.getRoleFromToken(token);
                String facilityID = jwtUtil.getFacilityFromToken(token);

                boolean isAuthentication = true;

                AuthUser authUser = new AuthUser();

                if (role.contains(RoleConstant.ADMIN.name())) {
                    Optional<UserAdmin> userAdmin = authenticationUserAdminRepository.findByEmail(email);
                    if (userAdmin.isEmpty()) {
                        isAuthentication = false;
                    } else {
                        authUser = sessionHelper.buildAuthUser(userAdmin.get(), Set.of(RoleConstant.ADMIN), facilityID);
                    }
                }
                else if(role.contains(RoleConstant.STAFF.name()) || role.contains(RoleConstant.TEACHER.name())) {
                    Optional<UserStaff> userStaff = authenticationUserStaffRepository.findLogin(email, facilityID);
                    if (userStaff.isEmpty()) {
                        isAuthentication = false;
                    } else {
                        Set<RoleConstant> roles = new HashSet<>();

                        List<Role> lstRole = authenticationRoleRepository.findRolesByUserId(userStaff.get().getId());
                        for(Role r: lstRole) {
                            roles.add(r.getCode());
                        }
                        authUser = sessionHelper.buildAuthUser(userStaff.get(), roles, facilityID);
                    }
                }
                else if (role.contains(RoleConstant.STUDENT.name())) {
                    Optional<UserStudent> userStudent = authenticationUserStudentRepository.findByEmailAndFacility_Id(email, facilityID);
                    if (userStudent.isEmpty()) {
                        isAuthentication = false;
                    } else {
                        authUser = sessionHelper.buildAuthUser(userStudent.get(), Set.of(RoleConstant.STUDENT), facilityID);
                    }
                }

                if (isAuthentication) {


                    sessionHelper.setCurrentUser(authUser);

                    List<GrantedAuthority> authorities = new ArrayList<>();

                    for (RoleConstant r: authUser.getRole()) {
                        authorities.add(new SimpleGrantedAuthority(r.name()));
                    }

                    Authentication auth = new UsernamePasswordAuthenticationToken(null, token, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        chain.doFilter(request, response);
    }

}
