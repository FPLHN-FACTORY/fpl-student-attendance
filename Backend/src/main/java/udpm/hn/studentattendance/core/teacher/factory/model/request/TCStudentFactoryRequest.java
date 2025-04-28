package udpm.hn.studentattendance.core.teacher.factory.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class TCStudentFactoryRequest extends PageableRequest {
    private String studentId;

    private String factoryId;

    private String studentFactoryId;

    private String searchQuery;

    private Integer status;
}
