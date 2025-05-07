package udpm.hn.studentattendance.core.teacher.studentattendance.model.response;

public interface TeacherStudentAttendanceResponse {
    Integer getRowNumber();

    String getAttendanceId();

    String getUserStudentCode();

    String getUserStudentName();

    String getUserStudentId();

    Integer getAttendanceStatus();

}
