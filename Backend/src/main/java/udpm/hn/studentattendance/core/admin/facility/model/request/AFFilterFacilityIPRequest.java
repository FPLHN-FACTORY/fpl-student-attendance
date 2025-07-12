package udpm.hn.studentattendance.core.admin.facility.model.request;

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
public class AFFilterFacilityIPRequest extends PageableRequest {

    private String idFacility;

    @Size(max = 255, message = "Keyword không được vượt quá 255 ký tự")
    private String keyword;

    private Integer status;

    private Integer type;

    @Override
    public String toString() {
        return "page=" + getPage() +
                "_size=" + getSize() +
                "_orderBy=" + getOrderBy() +
                "_sortBy=" + getSortBy() +
                "_q=" + (getQ() != null ? getQ() : "") +
                "_idFacility=" + (idFacility != null ? idFacility : "") +
                "_keyword=" + (keyword != null ? keyword : "") +
                "_status=" + (status != null ? status : "") +
                "_type=" + (type != null ? type : "");
    }
}
