package udpm.hn.studentattendance.core.admin.useradmin.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
public class ADUserAdminCreateOrUpdateRequest {

    @NotBlank(message = "Mã ban đào tạo không được bỏ trống")
    @Size(max = EntityProperties.LENGTH_CODE, message = "Mã ban đào tạo chỉ được tối đa " + EntityProperties.LENGTH_CODE + " ký tự")
    private String staffCode;

    @NotBlank(message = "Không được để trống email ban đào tạo")
    @Size(max = EntityProperties.LENGTH_NAME, message = "Email chỉ được tối đa " + EntityProperties.LENGTH_NAME + " ký tự")
    private String email;

    @NotBlank(message = "Không được để trống tên ban đào tạo")
    @Size(max = EntityProperties.LENGTH_NAME, message = "Tên ban đào tạo chỉ được tối đa " + EntityProperties.LENGTH_NAME + " ký tự")
    private String staffName;

}
