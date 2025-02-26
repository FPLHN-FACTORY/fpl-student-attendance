package udpm.hn.studentattendance.core.admin.staff.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdStaffRoleRequest {
    private String staffId;

    private String idFacility;

    private String roleName;
}
