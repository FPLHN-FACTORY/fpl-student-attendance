package udpm.hn.studentattendance.core.staff.factory.model.request.student_factory;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class Staff_UserStudentRequest extends PageableRequest {

    private String searchQuery;

    private String factoryId;
}
