package udpm.hn.studentattendance.core.student.schedule.model.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
@ToString
public class Student_ScheduleAttendanceSearchRequest extends PageableRequest {

    private String idStudent;

    private Long now;

    private Long max;
}
