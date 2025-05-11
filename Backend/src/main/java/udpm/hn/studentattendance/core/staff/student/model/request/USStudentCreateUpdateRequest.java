package udpm.hn.studentattendance.core.staff.student.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
public class USStudentCreateUpdateRequest {

    private String id;

    @Length(max = EntityProperties.LENGTH_CODE, message = "Mã sinh viên phải ít hơn " + EntityProperties.LENGTH_CODE + " ký tự")
    @NotBlank(message = "Mã sinh viên không được bỏ trống")
    private String code;

    @Length(max = EntityProperties.LENGTH_NAME, message = "Tên phải ít hơn " + EntityProperties.LENGTH_NAME + " ký tự")
    @NotBlank(message = "Tên sinh viên không đuợc bỏ trống")
    private String name;

    @Length(max = EntityProperties.LENGTH_NAME, message = "Tài khoản email phải ít hơn " + EntityProperties.LENGTH_NAME + " ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@gmail\\.com$", message = "Email phải có định dạng @gmail.com")
    @NotBlank(message = "Email không được bỏ trống")
    private String email;

}
