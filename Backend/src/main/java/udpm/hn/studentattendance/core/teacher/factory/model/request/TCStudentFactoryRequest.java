package udpm.hn.studentattendance.core.teacher.factory.model.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
public class TCStudentFactoryRequest extends PageableRequest {
    private String studentId;

    private String factoryId;

    private String studentFactoryId;

    @Size(max = EntityProperties.LENGTH_NAME, message = "Từ khóa không được quá:" + EntityProperties.LENGTH_NAME)
    private String searchQuery;

    private Integer status;
}
