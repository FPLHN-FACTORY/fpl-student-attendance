package udpm.hn.studentattendance.core.authentication.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationStudentRegisterRequest {

    @Size(max = EntityProperties.LENGTH_CODE)
    @NotBlank(message = "Mã số sinh viên không được bỏ trống")
    private String code;

    @NotBlank(message = "Cơ sở không được bỏ trống")
    private String idFacility;

    @Size(max = EntityProperties.LENGTH_NAME)
    @NotBlank(message = "Họ và tên sinh viên không được bỏ trống")
    private String name;

    @NotBlank(message = "Thông tin khuôn mặt không được bỏ trống")
    private String faceEmbedding;

}
