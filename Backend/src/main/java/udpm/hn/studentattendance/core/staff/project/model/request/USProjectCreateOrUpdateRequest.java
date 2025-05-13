package udpm.hn.studentattendance.core.staff.project.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class USProjectCreateOrUpdateRequest {

    @NotBlank(message = "Không được bỏ trống")
    private String name;

    private String description;

    private String levelProjectId;

    private String semesterId;

    private String subjectFacilityId;


}
