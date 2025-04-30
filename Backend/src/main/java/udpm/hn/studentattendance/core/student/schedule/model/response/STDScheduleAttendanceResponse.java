package udpm.hn.studentattendance.core.student.schedule.model.response;

public interface STDScheduleAttendanceResponse {

    Integer getIndexs();

    String getId();

    Long getAttendanceDayStart();

    Long getAttendanceDayEnd();

    String getSubjectCode();

    String getSubjectName();

    String getStaffName();

    String getShift();

    String getDescription();

    String getFactoryName();

    String getProjectName();

    String getLink();

    String getLocation();

    Integer getType();

}
