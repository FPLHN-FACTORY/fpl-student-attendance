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
public class SPDFilterPlanDateRequest extends PageableRequest {

    private String idPlanFactory;

    private String idFacility;

    @Size(max = 255, message = "Keyword không được vượt quá 255 ký tự")
    private String keyword;

    private Integer shift;

    private Integer type;

    private Long startDate;

    private String status;

    @Override
    public String toString() {
        return "page=" + getPage() +
                "_size=" + getSize() +
                "_orderBy=" + getOrderBy() +
                "_sortBy=" + getSortBy() +
                "_q=" + (getQ() != null ? getQ() : "") +
                "_idPlanFactory=" + (idPlanFactory != null ? idPlanFactory : "") +
                "_idFacility=" + (idFacility != null ? idFacility : "") +
                "_keyword=" + (keyword != null ? keyword : "") +
                "_shift=" + (shift != null ? shift : "") +
                "_type=" + (type != null ? type : "") +
                "_startDate=" + (startDate != null ? startDate : "") +
                "_status=" + (status != null ? status : "");
    }
}
