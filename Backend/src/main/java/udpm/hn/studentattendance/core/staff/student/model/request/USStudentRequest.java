package udpm.hn.studentattendance.core.staff.student.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Getter
@Setter
public class USStudentRequest extends PageableRequest {
    private String studentId;

    private String searchQuery;

    private EntityStatus studentStatus;

}
