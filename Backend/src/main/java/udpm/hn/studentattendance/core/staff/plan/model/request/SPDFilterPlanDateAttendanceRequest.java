package udpm.hn.studentattendance.core.staff.plan.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SPDFilterPlanDateAttendanceRequest extends PageableRequest {

    private String keyword;

    private Integer status;

    private String idPlanDate;

    private String idFacility;

    private String idUserStudent;

    @Override
    public String toString() {
        return "page=" + getPage() +
                "_size=" + getSize() +
                "_orderBy=" + getOrderBy() +
                "_sortBy=" + getSortBy() +
                "_q=" + (getQ() != null ? getQ() : "") +
                "_keyword=" + (keyword != null ? keyword : "") +
                "_status=" + (status != null ? status : "") +
                "_idPlanDate=" + (idPlanDate != null ? idPlanDate : "") +
                "_idFacility=" + (idFacility != null ? idFacility : "") +
                "_idUserStudent=" + (idUserStudent != null ? idUserStudent : "");
    }
}
