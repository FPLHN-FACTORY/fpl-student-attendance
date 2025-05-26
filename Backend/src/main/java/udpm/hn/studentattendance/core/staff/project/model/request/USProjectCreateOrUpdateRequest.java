package udpm.hn.studentattendance.core.staff.project.model.request;

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
public class USProjectCreateOrUpdateRequest {

    @NotBlank(message = "Tên dự án không được bỏ trống")
    @Size(max = EntityProperties.LENGTH_NAME, message = "Tên dự án chỉ được tối đa " + EntityProperties.LENGTH_NAME + " ký tự")
    private String name;

    private String description;

    private String levelProjectId;

    private String semesterId;

    private String subjectFacilityId;

}
