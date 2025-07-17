package udpm.hn.studentattendance.core.staff.studentfactory.model.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
public class USStudentFactoryRequest extends PageableRequest {
    private String studentId;

    private String factoryId;

    private String studentFactoryId;

    @Size(max = EntityProperties.LENGTH_NAME, message = "Tìm kiếm " + EntityProperties.LENGTH_NAME + " ký tự")
    private String searchQuery;

    private Integer status;
}
