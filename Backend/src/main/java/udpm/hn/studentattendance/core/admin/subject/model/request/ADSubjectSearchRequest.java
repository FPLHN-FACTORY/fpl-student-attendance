package udpm.hn.studentattendance.core.admin.subject.model.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
public class ADSubjectSearchRequest extends PageableRequest {

    @Size(max = EntityProperties.LENGTH_NAME, message = "Từ khóa không được quá:" + EntityProperties.LENGTH_NAME)
    private String name;

    private Integer status;

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
