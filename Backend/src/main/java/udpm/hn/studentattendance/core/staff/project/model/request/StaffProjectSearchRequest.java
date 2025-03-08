package udpm.hn.studentattendance.core.staff.project.model.request;

import lombok.*;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StaffProjectSearchRequest extends PageableRequest {

    private String name;

    private String levelProjectId;

    private String semesterId;

    private String subjectId;

    private String facilityId;

    private Integer status;

}
