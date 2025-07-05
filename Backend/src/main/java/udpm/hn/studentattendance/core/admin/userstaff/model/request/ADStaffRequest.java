package udpm.hn.studentattendance.core.admin.userstaff.model.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Getter
@Setter
public class ADStaffRequest extends PageableRequest {
    @Size(max = EntityProperties.LENGTH_NAME, message = "Từ khóa không được quá:" + EntityProperties.LENGTH_NAME)
    private String searchQuery;

    private String idFacility;

    private EntityStatus status;

    private Integer roleCodeFilter;
}
