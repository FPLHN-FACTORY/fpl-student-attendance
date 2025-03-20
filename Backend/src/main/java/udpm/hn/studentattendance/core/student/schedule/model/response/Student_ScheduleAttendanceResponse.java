package udpm.hn.studentattendance.core.student.schedule.model.response;

public interface Student_ScheduleAttendanceResponse {

    Integer getIndexs();

    String getId();

    Long getAttendanceDay();

    String getSubjectCode();

    String getSubjectName();

    String getStaffName();

    Integer getShift();

    String getDescription();

    String getFactoryName();

    String getProjectName();

}
