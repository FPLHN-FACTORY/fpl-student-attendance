package udpm.hn.studentattendance.core.admin.user_staff.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Getter
@Setter
public class Admin_StaffRequest extends PageableRequest {
    private String searchQuery;

    private String idFacility;

    private EntityStatus status;
}
