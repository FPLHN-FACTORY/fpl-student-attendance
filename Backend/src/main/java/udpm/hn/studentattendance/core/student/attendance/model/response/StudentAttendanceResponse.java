package udpm.hn.studentattendance.core.student.attendance.model.response;

public interface StudentAttendanceResponse {

    Integer getIndexs();

    String getId();

    Long getAttendanceDay();

    String getSubjectCode();

    String getSubjectName();

    String getStaffName();

    Integer getShift();

    String getDescription();
}
