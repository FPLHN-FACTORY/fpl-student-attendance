package udpm.hn.studentattendance.core.admin.subjectfacility.model.request;

import lombok.*;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AdminSubjectFacilitySearchRequest extends PageableRequest {

    private String name;

    private String facilityId;

    private String subjectId;

    private Integer status;
}
