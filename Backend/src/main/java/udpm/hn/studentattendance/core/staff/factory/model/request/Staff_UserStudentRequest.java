package udpm.hn.studentattendance.core.staff.factory.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class Staff_UserStudentRequest extends PageableRequest {

    private String searchQuery;

    private String factoryId;
}
