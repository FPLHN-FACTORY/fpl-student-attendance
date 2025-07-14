package udpm.hn.studentattendance.core.staff.plan.model.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SPDFilterPlanFactoryRequest extends PageableRequest {

    private String idFacility;

    private String idPlan;

    @Size(max = 255, message = "Keyword không được vượt quá 255 ký tự")
    private String keyword;

    private Integer status;

    private Long fromDate;

    private Long toDate;

    @Override
    public String toString() {
        return "page=" + getPage() +
                "_size=" + getSize() +
                "_orderBy=" + getOrderBy() +
                "_sortBy=" + getSortBy() +
                "_q=" + (getQ() != null ? getQ() : "") +
                "_idFacility=" + (idFacility != null ? idFacility : "") +
                "_idPlan=" + (idPlan != null ? idPlan : "") +
                "_keyword=" + (keyword != null ? keyword : "") +
                "_status=" + (status != null ? status : "") +
                "_fromDate=" + (fromDate != null ? fromDate : "") +
                "_toDate=" + (toDate != null ? toDate : "");
    }
}
