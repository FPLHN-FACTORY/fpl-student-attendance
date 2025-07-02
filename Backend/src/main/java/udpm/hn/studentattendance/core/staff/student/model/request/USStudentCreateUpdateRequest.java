package udpm.hn.studentattendance.core.staff.student.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
public class USStudentCreateUpdateRequest {

    private String id;

    @NotBlank(message = "Không được để trống mã sinh viên")
    @Size(max = EntityProperties.LENGTH_CODE, message = "Mã sinh viên không được vượt quá " + EntityProperties.LENGTH_CODE + " ký tự")
    private String code;

    @NotBlank(message = "Không được để trống mã sinh viên")
    @Size(max = EntityProperties.LENGTH_NAME, message = "Tên sinh viên không được vượt quá " + EntityProperties.LENGTH_NAME + " ký tự")
    private String name;

    @NotBlank(message = "Không được để trống email sinh viên")
    @Size(max = EntityProperties.LENGTH_NAME, message = "Email sinh viên không được vượt quá " + EntityProperties.LENGTH_NAME + " ký tự")
    private String email;

}
