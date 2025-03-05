package udpm.hn.studentattendance.core.admin.adminsubjectmanagement.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AdminSubjectCreateRequest {

    @NotBlank(message = "Không được bỏ trống")
    private String name;

    private String code;

}
