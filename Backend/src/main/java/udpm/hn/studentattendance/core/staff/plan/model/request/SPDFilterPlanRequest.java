package udpm.hn.studentattendance.core.staff.plan.model.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SPDFilterPlanRequest extends PageableRequest {

    private String idFacility;

    @Size(max = EntityProperties.LENGTH_NAME, message = "Keyword không được vượt quá " + EntityProperties.LENGTH_NAME + " ký tự")
    private String keyword;

    private String level;

    private Integer status;

    private String semester;

    private Integer year;

    private String subject;

}
