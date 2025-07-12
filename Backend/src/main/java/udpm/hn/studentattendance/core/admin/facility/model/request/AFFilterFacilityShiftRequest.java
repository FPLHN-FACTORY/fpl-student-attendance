package udpm.hn.studentattendance.core.admin.facility.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AFFilterFacilityShiftRequest extends PageableRequest {

    private String idFacility;

    private Integer shift;

    private Integer status;

    @Override
    public String toString() {
        return "page=" + getPage() +
                "_size=" + getSize() +
                "_orderBy=" + getOrderBy() +
                "_sortBy=" + getSortBy() +
                "_q=" + (getQ() != null ? getQ() : "") +
                "_idFacility=" + (idFacility != null ? idFacility : "") +
                "_shift=" + (shift != null ? shift : "") +
                "_status=" + (status != null ? status : "");
    }
}
