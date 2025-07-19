package udpm.hn.studentattendance.core.staff.plan.model.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SPDFilterPlanRequest extends PageableRequest {

    private String idFacility;

    @Size(max = EntityProperties.LENGTH_NAME, message = "Keyword không được vượt quá " + EntityProperties.LENGTH_NAME
            + " ký tự")
    private String keyword;

    private String level;

    private Integer status;

    private String semester;

    private Integer year;

    private String subject;

    @Override
    public String toString() {
        return "page=" + getPage() +
                "_size=" + getSize() +
                "_orderBy=" + getOrderBy() +
                "_sortBy=" + getSortBy() +
                "_q=" + (getQ() != null ? getQ() : "") +
                "_idFacility=" + (idFacility != null ? idFacility : "") +
                "_keyword=" + (keyword != null ? keyword : "") +
                "_level=" + (level != null ? level : "") +
                "_status=" + (status != null ? status : "") +
                "_semester=" + (semester != null ? semester : "") +
                "_year=" + (year != null ? year : "") +
                "_subject=" + (subject != null ? subject : "");
    }
}
