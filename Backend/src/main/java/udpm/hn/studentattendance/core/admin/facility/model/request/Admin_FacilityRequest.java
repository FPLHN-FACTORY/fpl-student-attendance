package udpm.hn.studentattendance.core.admin.facility.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class Admin_FacilityRequest extends PageableRequest {
    private String id;

    private String code;

    private String name;
}
