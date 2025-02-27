package udpm.hn.studentattendance.core.admin.role.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdCreateUpdateRoleRequest {
    private String roleId;

    @NotBlank(message = "Mã vai trò không được để trống")
    @Size(max = 100, message = "Mã vai trò phải dưới 100 ký tự")
    private String roleCode;

    @NotBlank(message = "Cơ sở không được trống")
    private String idFacility;
}
