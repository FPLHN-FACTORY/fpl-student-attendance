package udpm.hn.studentattendance.core.authentication.oauth2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {

    private String id;

    private String name;

    private Set<RoleConstant> role;

    private String idFacility;

    private String code;

    private String picture;

    private String email;

    private String emailFe;

    private String emailFpt;

}
