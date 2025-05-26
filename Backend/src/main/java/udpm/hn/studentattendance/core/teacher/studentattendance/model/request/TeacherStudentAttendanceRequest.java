package udpm.hn.studentattendance.core.teacher.studentattendance.model.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TeacherStudentAttendanceRequest {

    private String idAttendance;

    private String idUserStudent;

    private String idPlanDate;

    private Integer status;

}
