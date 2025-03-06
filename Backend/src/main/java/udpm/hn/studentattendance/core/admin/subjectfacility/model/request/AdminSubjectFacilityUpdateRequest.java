package udpm.hn.studentattendance.core.admin.subjectfacility.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminSubjectFacilityUpdateRequest {

    private String facilityId;

    private String subjectId;

    private String status;
}
