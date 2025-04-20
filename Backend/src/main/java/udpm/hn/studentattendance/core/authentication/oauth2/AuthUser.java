package udpm.hn.studentattendance.core.authentication.oauth2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AuthUser {

    protected String id;

    protected String name;

    protected Set<RoleConstant> role;

    protected String idFacility;

    protected String code;

    protected String picture;

    protected String email;

    protected String emailFe;

    protected String emailFpt;

}
