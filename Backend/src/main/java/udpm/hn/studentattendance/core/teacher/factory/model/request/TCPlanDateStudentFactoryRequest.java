package udpm.hn.studentattendance.core.teacher.factory.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class TCPlanDateStudentFactoryRequest extends PageableRequest {

    private String idFactory;

    private String idUserStudent;

}
