package udpm.hn.studentattendance.core.admin.useradmin.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class ADUserAdminRequest extends PageableRequest {
    private String searchQuery;

    private Integer status;
}
