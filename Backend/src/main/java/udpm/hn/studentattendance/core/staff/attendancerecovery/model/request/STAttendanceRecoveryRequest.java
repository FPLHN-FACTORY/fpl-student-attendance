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
}
