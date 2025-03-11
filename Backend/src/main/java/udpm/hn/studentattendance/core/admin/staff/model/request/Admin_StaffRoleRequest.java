package udpm.hn.studentattendance.core.admin.staff.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class Admin_StaffRoleRequest extends PageableRequest {
    private String staffId;

    private String idFacility;

    private String roleCode;
}
