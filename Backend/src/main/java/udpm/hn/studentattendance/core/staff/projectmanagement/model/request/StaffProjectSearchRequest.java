package udpm.hn.studentattendance.core.staff.projectmanagement.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StaffProjectSearchRequest extends PageableRequest {

    private String name;

    private String levelProjectId;

    private String semesterId;

    private String subjectFacilityId;

    private Integer status;
}
