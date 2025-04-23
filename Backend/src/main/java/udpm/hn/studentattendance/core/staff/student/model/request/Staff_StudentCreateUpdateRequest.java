package udpm.hn.studentattendance.core.staff.student.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class Staff_StudentCreateUpdateRequest {

    private String id;

    @Length(max = 50, message = "Mã sinh viên phải ít hơn 50 ký tự")
    @Pattern(regexp = "^[^\\s]+$", message = "Mã sinh viên không được chứa khoảng trắng")
    @NotBlank
    private String code;

    @Length(max = 255, message = "Tên phải ít hơn 255 ký tự")
    @NotBlank
    private String name;

    @Length(max = 100, message = "Tài khoản email phải ít hơn 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@gmail\\.com$", message = "Email phải có định dạng @gmail.com")
    @NotBlank
    private String email;
}
