package udpm.hn.studentattendance.core.staff.factory.model.request.userstudentfactory;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class USStudentFactoryRequest extends PageableRequest {
    private String studentId;

    private String factoryId;

    private String studentFactoryId;

    private String searchQuery;

    private Integer status;
}
