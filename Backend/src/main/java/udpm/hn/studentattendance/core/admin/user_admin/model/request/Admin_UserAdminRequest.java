package udpm.hn.studentattendance.core.admin.user_admin.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class Admin_UserAdminRequest extends PageableRequest {
    private String searchQuery;

    private Integer status;
}
