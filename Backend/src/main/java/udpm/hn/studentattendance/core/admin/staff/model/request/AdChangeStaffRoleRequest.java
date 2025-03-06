package udpm.hn.studentattendance.core.admin.staff.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class AdChangeStaffRoleRequest {
    @NotBlank(message = "Id staff không được để trống")
    @Length(max = 255, message = "Mã nhân viên phải ngắn hơn 255 ký tự")
    @Pattern(regexp = "^[^\\s]+$", message = "Mã nhân viên không được chứa khoảng trắng")
    private String idStaff;

    @NotBlank(message = "Id role không được để trống")
    @Length(max = 255, message = "Mã vai trò phải ngắn hơn 255 ký tự")
    @Pattern(regexp = "^[^\\s]+$", message = "Mã vai trò không được chứa khoảng trắng")
    private String idRole;

    private String facilityId;
}
