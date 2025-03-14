package udpm.hn.studentattendance.core.staff.plan.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SPDFilterCreatePlanDateRequest {

    private String idFacility;

    private String idFactory;

    private String level;

    private String semester;

    private Integer year;

    private String subject;

}
