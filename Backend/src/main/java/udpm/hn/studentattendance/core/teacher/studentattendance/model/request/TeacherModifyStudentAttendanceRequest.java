package udpm.hn.studentattendance.core.teacher.studentattendance.model.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class TeacherModifyStudentAttendanceRequest {

    private List<TeacherStudentAttendanceRequest> students;

}
