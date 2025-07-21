package udpm.hn.studentattendance.core.admin.levelproject.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ADLevelProjectCreateRequest {

    @NotBlank(message = "Tên cấp độ dự án không được bỏ trống")
    @Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Tên cấp độ dự án phải có ít nhất 2 ký tự và không được vượt quá "
            + EntityProperties.LENGTH_NAME + " ký tự")
    private String name;

    private String description;

}
