package udpm.hn.studentattendance.core.authentication.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationUserRequest {

    private String id;

    private String name;

    private String code;

    private String email;

    private String picture;

    private List<String> roles;

}
