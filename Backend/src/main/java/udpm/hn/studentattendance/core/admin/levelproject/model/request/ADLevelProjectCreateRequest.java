package udpm.hn.studentattendance.core.admin.levelproject.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ADLevelProjectCreateRequest {

    @NotBlank(message = "Không được bỏ trống")
    @NotEmpty(message = "Khoong bo trong")
    private String name;

    private String description;

    private String code;

}
