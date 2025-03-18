package udpm.hn.studentattendance.core.student.history_attendance.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class HistoryAttendanceRequest extends PageableRequest {

    private String studentFactoryId;

    private String factoryId;

    private String semesterId;
}
