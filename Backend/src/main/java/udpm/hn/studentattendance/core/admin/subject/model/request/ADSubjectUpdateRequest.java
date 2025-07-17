package udpm.hn.studentattendance.core.admin.subject.model.request;

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
public class ADSubjectUpdateRequest {

        @NotBlank(message = "Tên bộ môn không được bỏ trống")
        @Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Tên bộ môn không được vượt quá "
                        + EntityProperties.LENGTH_NAME
                        + " ký tự")
        private String name;

        @NotBlank(message = "Mã bộ môn không được bỏ trống")
        @Size(max = EntityProperties.LENGTH_CODE, message = "Mã bộ môn không được vượt quá "
                        + EntityProperties.LENGTH_CODE
                        + " ký tự")
        private String code;

}
