package udpm.hn.studentattendance.core.staff.studentfactory.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
public class USStudentFactoryAddRequest {

    @NotBlank(message = "Không được để trống mã sinh viên")
    @Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Mã sih viên " + EntityProperties.LENGTH_NAME
            + " ký tự")
    private String studentCode;

    private String factoryId;
}
