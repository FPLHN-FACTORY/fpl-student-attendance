package udpm.hn.studentattendance.core.staff.factory.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class USFactoryCreateUpdateRequest {
    private String id;
    @NotBlank(message = "Tên không được để trống")
    @Size(max = 255, message = "Tên không được vượt quá 255 ký tự")
    private String factoryName;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String factoryDescription;

    @NotBlank(message = "Không được để trống dự án")
    private String idProject;

    @NotBlank(message = "Không được để trống nhân viên")
    private String idUserStaff;

}
