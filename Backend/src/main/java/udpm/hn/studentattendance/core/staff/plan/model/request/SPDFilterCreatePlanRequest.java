package udpm.hn.studentattendance.core.staff.plan.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SPDFilterCreatePlanRequest {

    private String idFacility;

    private String idProject;

    private String level;

    private String semester;

    private Integer year;

    private String subject;

}
