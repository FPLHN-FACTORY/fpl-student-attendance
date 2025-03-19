package udpm.hn.studentattendance.core.staff.plan.model.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SPDFilterPlanRequest extends PageableRequest {

    private String idFacility;

    @Size(max = 255, message = "Keyword không được vượt quá 255 ký tự")
    private String keyword;

    private String level;

    private Integer status;

    private String semester;

    private Integer year;

    private String subject;

}
