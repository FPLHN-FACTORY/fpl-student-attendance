package udpm.hn.studentattendance.core.staff.project.model.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.query.Page;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
public class USProjectSearchRequest extends PageableRequest {
    @Size(max = EntityProperties.LENGTH_NAME, message = "Từ khóa không được quá:" + EntityProperties.LENGTH_NAME)
    private String name;

    private String levelProjectId;

    private String semesterId;

    private String subjectId;

    private String facilityId;

    private Integer status;

}
