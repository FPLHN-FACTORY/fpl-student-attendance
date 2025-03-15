package udpm.hn.studentattendance.core.admin.role.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class Admin_RoleRequest extends PageableRequest {
    private String roleCode;

    private String idFacility;
}
