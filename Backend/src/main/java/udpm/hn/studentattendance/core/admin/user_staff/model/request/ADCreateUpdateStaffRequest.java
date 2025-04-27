package udpm.hn.studentattendance.core.admin.user_staff.model.request;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Getter
@Setter
public class ADCreateUpdateStaffRequest {

    // @NotEmpty(message = "Tên không được để trống")
    @Length(max = 255, message = "Tên phải ít hơn 255 ký tự")
    private String name;

    // @NotEmpty(message = "Mã nhân viên không được để trống")
    @Length(max = 50, message = "Mã nhân viên phải ít hơn 50 ký tự")
    @Pattern(regexp = "^[^\\s]+$", message = "Mã nhân viên không được chứa khoảng trắng")
    private String staffCode;

    // @NotEmpty(message = "Tài khoản FE không được để trống")
    @Length(max = 100, message = "Tài khoản FE phải ít hơn 100 ký tự")
    // @Pattern(regexp = "^[A-Za-z0-9._%+-]+@fe\\.edu\\.vn$", message = "Không chứa
    // khoảng trắng và kết thúc bằng @fe.edu.vn")
    private String emailFe;

    // @NotBlank(message = "Tài khoản FPT không được để trống")
    @Length(max = 100, message = "Tài khoản FPT phải ít hơn 100 ký tự")
    // @Pattern(regexp = "^[A-Za-z0-9._%+-]+@fpt\\.edu\\.vn$", message = "Không chứa
    // khoảng trắng và kết thúc bằng @fpt.edu.vn")
    private String emailFpt;

    private String facilityId;

    private List<String> roleCodes;
}
