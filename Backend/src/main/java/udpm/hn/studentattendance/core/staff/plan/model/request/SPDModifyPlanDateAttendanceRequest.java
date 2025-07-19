package udpm.hn.studentattendance.core.staff.plan.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SPDModifyPlanDateAttendanceRequest {

    private String idPlanDate;

    private String idFacility;

    private String idUserStudent;

    private Integer status;

}
