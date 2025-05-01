package udpm.hn.studentattendance.core.admin.userstaff.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Getter
@Setter
public class ADStaffRequest extends PageableRequest {
    private String searchQuery;

    private String idFacility;

    private EntityStatus status;
}
