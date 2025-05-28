package udpm.hn.studentattendance.core.authentication.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationStudentUpdateFaceIDRequest {

    @NotBlank(message = "Thông tin khuôn mặt không được bỏ trống")
    private String faceEmbedding;

}
