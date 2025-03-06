package udpm.hn.studentattendance.core.admin.subject.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class AdminSubjectSearchRequest  extends PageableRequest {

    private String name;

    private Integer status;

}
