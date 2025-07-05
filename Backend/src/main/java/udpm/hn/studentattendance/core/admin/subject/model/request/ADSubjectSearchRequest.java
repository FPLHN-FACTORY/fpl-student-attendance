package udpm.hn.studentattendance.core.admin.subject.model.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
public class ADSubjectSearchRequest extends PageableRequest {

    @Size(max = EntityProperties.LENGTH_NAME, message = "Từ khóa không được quá:" + EntityProperties.LENGTH_NAME)
    private String name;

    private Integer status;

}
