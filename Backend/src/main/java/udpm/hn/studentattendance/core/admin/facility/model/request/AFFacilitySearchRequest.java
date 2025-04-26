package udpm.hn.studentattendance.core.admin.facility.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Getter
@Setter
public class AFFacilitySearchRequest extends PageableRequest {
    private String name;

    private EntityStatus status;

}
