package udpm.hn.studentattendance.core.admin.useradmin.model.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
@ToString
public class ADUserAdminRequest extends PageableRequest {
    @Size(max = EntityProperties.LENGTH_NAME, message = "Từ khóa không được quá:" + EntityProperties.LENGTH_NAME)
    private String searchQuery;

    private Integer status;

}
