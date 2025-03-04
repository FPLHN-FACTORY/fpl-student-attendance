package udpm.hn.studentattendance.core.staff.levelprojectmanagement.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Getter
@Setter
@AllArgsConstructor
public class LevelProjectCreateRequest {

    @NotBlank(message = "Không được bỏ trống")
    private String name;

    private String description;

    private String code;

}
