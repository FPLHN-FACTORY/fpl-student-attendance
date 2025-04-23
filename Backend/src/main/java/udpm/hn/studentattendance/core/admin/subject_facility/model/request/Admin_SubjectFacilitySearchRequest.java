package udpm.hn.studentattendance.core.admin.subject_facility.model.request;

import lombok.*;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Admin_SubjectFacilitySearchRequest extends PageableRequest {

    private String name;

    private String facilityId;

    private String subjectId;

    private Integer status;
}
