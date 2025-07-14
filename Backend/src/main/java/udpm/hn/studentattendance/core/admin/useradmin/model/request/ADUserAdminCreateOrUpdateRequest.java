package udpm.hn.studentattendance.core.admin.useradmin.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
public class ADUserAdminCreateOrUpdateRequest {

    @NotBlank(message = "Mã admin tạo không được bỏ trống")
    @Size(max = EntityProperties.LENGTH_CODE, message = "Mã admin chỉ được tối đa " + EntityProperties.LENGTH_CODE + " ký tự")
    private String staffCode;

    @NotBlank(message = "Không được để trống email admin")
    @Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Email chỉ được tối đa " + EntityProperties.LENGTH_NAME + " ký tự")
    private String email;

    @NotBlank(message = "Không được để trống tên admin")
    @Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Tên admin chỉ được tối đa " + EntityProperties.LENGTH_NAME + " ký tự")
    private String staffName;

}
