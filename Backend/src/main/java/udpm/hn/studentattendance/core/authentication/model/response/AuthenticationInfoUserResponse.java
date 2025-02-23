package udpm.hn.studentattendance.core.authentication.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationInfoUserResponse {

    private String id;

    private String name;

    private String code;

    private String email;

    private String picture;

    private String role;

    private String idFacility;

}
