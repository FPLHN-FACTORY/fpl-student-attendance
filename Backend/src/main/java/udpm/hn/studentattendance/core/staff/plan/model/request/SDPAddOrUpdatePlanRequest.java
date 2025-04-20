package udpm.hn.studentattendance.core.staff.plan.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SDPAddOrUpdatePlanRequest {

    private String id;

    private String idProject;

    private List<Long> rangeDate;

    @NotBlank(message = "Vui lòng nhập tên kế hoạch")
    @Size(max = EntityProperties.LENGTH_NAME, message = "Tên kế hoạch không được vượt quá " + EntityProperties.LENGTH_NAME + " ký tự")
    private String name;

    @NotBlank(message = "Vui lòng nhập nội dung kế hoạch")
    private String description;

}
