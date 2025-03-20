package udpm.hn.studentattendance.core.student.attendance.model.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
@ToString
public class StudentAttendanceSearchRequest extends PageableRequest {

    private String idStudent;

    private long now;

    private long max;
}
