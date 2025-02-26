package udpm.hn.studentattendance.core.staff.projectmanagement.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProjectCreateRequest {

    @NotBlank(message = "Không được bỏ trống")
    private String name;

    private Long createTime;

    private Long updateTime;

    private String description;

    private String levelProjectId;

    private String semesterId;

    private String subjectFacilityId;

    private String userStaffId;
}
