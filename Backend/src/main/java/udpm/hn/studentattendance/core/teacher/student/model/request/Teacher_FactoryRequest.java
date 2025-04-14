package udpm.hn.studentattendance.core.teacher.student.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class Teacher_FactoryRequest extends PageableRequest {
    private String factoryId;

    private String userStaffId;

    private String factoryName;

    private String projectId;

    private String factoryStatus;

}