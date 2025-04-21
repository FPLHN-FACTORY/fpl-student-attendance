package udpm.hn.studentattendance.core.admin.level_project.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class Admin_LevelProjectSearchRequest extends PageableRequest {

    private String name;

    private Integer status;
}
