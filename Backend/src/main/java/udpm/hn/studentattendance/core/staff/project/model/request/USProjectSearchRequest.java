package udpm.hn.studentattendance.core.staff.project.model.request;

import lombok.*;
import org.hibernate.query.Page;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class USProjectSearchRequest extends PageableRequest {

    private String name;

    private String levelProjectId;

    private String semesterId;

    private String subjectId;

    private String facilityId;

    private Integer status;

}
