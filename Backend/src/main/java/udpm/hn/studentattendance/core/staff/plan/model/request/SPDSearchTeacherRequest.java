package udpm.hn.studentattendance.core.staff.plan.model.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SPDSearchTeacherRequest {

    @Size(max = EntityProperties.LENGTH_NAME, message = "Từ khoá tìm kiếm không được vượt quá " + EntityProperties.LENGTH_NAME + " ký tự")
    private String keyword;

}
