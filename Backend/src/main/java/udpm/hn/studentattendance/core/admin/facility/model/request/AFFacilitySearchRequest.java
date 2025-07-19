package udpm.hn.studentattendance.core.admin.facility.model.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Getter
@Setter
public class AFFacilitySearchRequest extends PageableRequest {
    @Size(max = EntityProperties.LENGTH_CODE, message = "Từ khóa không được vượt quá 50 ký tự")
    private String name;

    private EntityStatus status;

    @Override
    public String toString() {
        return "page=" + getPage() +
                "_size=" + getSize() +
                "_orderBy=" + getOrderBy() +
                "_sortBy=" + getSortBy() +
                "_q=" + (getQ() != null ? getQ() : "") +
                "_name=" + (name != null ? name : "") +
                "_status=" + (status != null ? status : "");
    }
}
