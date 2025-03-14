package udpm.hn.studentattendance.core.admin.subject.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Admin_SubjectUpdateRequest {

    @NotBlank(message = "Không được bỏ trống")
    private String name;

    private String code;

    private String status;
}
