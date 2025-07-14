package udpm.hn.studentattendance.core.teacher.teachingschedule.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class TCTeachingScheduleRequest extends PageableRequest {

    private String idSubject;

    private String idFactory;

    private String idProject;

    private Integer shift;

    private Long startDate;

    private Long endDate;

    private Integer shiftType;

    @Override
    public String toString() {
        return "page=" + getPage() +
                "_size=" + getSize() +
                "_orderBy=" + getOrderBy() +
                "_sortBy=" + getSortBy() +
                "_q=" + (getQ() != null ? getQ() : "") +
                "_idSubject=" + (idSubject != null ? idSubject : "") +
                "_idFactory=" + (idFactory != null ? idFactory : "") +
                "_idProject=" + (idProject != null ? idProject : "") +
                "_shift=" + (shift != null ? shift : "") +
                "_startDate=" + (startDate != null ? startDate : "") +
                "_endDate=" + (endDate != null ? endDate : "") +
                "_shiftType=" + (shiftType != null ? shiftType : "");
    }
}
