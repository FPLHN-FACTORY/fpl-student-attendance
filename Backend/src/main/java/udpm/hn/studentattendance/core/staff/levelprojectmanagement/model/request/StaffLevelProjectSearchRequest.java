package udpm.hn.studentattendance.core.staff.levelprojectmanagement.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Getter
@Setter
public class StaffLevelProjectSearchRequest extends PageableRequest {

    private String name;

    private Integer status;
}
