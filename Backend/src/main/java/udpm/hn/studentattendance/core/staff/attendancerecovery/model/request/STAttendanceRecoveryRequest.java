package udpm.hn.studentattendance.core.staff.attendancerecovery.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class STAttendanceRecoveryRequest extends PageableRequest {

    private String searchQuery;

    private Long fromDate;

    private Long toDate;

    private String semesterId;

    @Override
    public String toString() {
        return "page=" + getPage() +
                "_size=" + getSize() +
                "_orderBy=" + getOrderBy() +
                "_sortBy=" + getSortBy() +
                "_q=" + (getQ() != null ? getQ() : "") +
                "_searchQuery=" + (searchQuery != null ? searchQuery : "") +
                "_fromDate=" + (fromDate != null ? fromDate : "") +
                "_toDate=" + (toDate != null ? toDate : "") +
                "_semesterId=" + (semesterId != null ? semesterId : "");
    }
}
