package udpm.hn.studentattendance.core.authentication.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationToken implements Serializable {

    private String accessToken;

    private String refreshToken;

}
