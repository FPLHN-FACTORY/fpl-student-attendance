package udpm.hn.studentattendance.core.admin.userstaff.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

import java.util.List;

@Getter
@Setter
public class ADCreateUpdateStaffRequest {

    @NotBlank(message = "Tên không được để trống")
    @Length(max = EntityProperties.LENGTH_NAME, message = "Tên chỉ được tối đa " + EntityProperties.LENGTH_NAME + " ký tự")
    private String name;

    @NotBlank(message = "Mã nhân sự không được để trống")
    @Length(max = EntityProperties.LENGTH_CODE, message = "Mã nhân viên chỉ được tối đa " + EntityProperties.LENGTH_CODE + " ký tự")
    private String staffCode;

    @NotBlank(message = "Tài khoản FE không được để trống")
    @Length(max = EntityProperties.LENGTH_NAME, message = "Tài khoản FE chỉ được tối đa " + EntityProperties.LENGTH_NAME + " ký tự")
    private String emailFe;

    @NotBlank(message = "Tài khoản FPT không được để trống")
    @Length(max = EntityProperties.LENGTH_NAME, message = "Tài khoản FPT chỉ được tối đa " + EntityProperties.LENGTH_NAME + " ký tự")
    private String emailFpt;

    private String facilityId;

    private List<String> roleCodes;

}
