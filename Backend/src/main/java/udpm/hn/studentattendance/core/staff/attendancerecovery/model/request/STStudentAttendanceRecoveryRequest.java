package udpm.hn.studentattendance.core.staff.attendancerecovery.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class STStudentAttendanceRecoveryRequest {

    private Long day;

    private String studentCode;

    // vẽ sơ đồ
//    private String factoryName;
}
