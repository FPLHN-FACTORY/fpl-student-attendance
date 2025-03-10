package udpm.hn.studentattendance.core.staff.factory.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Getter
@Setter
public class FactoryRequest extends PageableRequest {
    private String idProject;
    private String idStaff;
    private String idFacility;
    private String searchQuery;        // Tìm kiếm chung cho bảng factory
//    private Long searchPlanStartDate;
//    private Integer searchPlanShift;

    private EntityStatus status;

}
