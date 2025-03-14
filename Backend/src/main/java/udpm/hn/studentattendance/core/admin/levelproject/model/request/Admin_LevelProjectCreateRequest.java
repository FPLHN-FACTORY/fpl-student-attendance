package udpm.hn.studentattendance.core.admin.levelproject.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Admin_LevelProjectCreateRequest {

    @NotBlank(message = "Không được bỏ trống")
    private String name;

    private String description;

    private String code;

}
