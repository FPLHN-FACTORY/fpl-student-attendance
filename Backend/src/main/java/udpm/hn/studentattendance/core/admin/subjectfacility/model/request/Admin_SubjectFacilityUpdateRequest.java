package udpm.hn.studentattendance.core.admin.subjectfacility.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Admin_SubjectFacilityUpdateRequest {

    private String facilityId;

    private String subjectId;

    private String status;
}
