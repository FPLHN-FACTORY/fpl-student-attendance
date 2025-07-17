package udpm.hn.studentattendance.core.staff.studentfactory.model.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
public class USUserStudentRequest extends PageableRequest {

    @Size(max = EntityProperties.LENGTH_NAME, message = "Keyword không được vượt quá " + EntityProperties.LENGTH_NAME + " ký tự")
    private String searchQuery;

    private String factoryId;
}
