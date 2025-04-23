package udpm.hn.studentattendance.core.staff.factory.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Getter
@Setter
public class Staff_FactoryRequest extends PageableRequest {
    private String idProject;

    private String idStaff;

    private String idFacility;

    private String factoryName;    // dùng thay cho searchQuery

    private String idSemester;

    private EntityStatus status;

}
