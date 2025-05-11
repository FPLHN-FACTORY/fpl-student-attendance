package udpm.hn.studentattendance.core.staff.plan.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SPDFilterPlanDateAttendanceRequest extends PageableRequest {

    private String keyword;

    private Integer status;

    private String idPlanDate;

    private String idFacility;

    private String idUserStudent;

}
