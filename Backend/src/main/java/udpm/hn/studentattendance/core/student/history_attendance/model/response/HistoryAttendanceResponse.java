package udpm.hn.studentattendance.core.student.history_attendance.model.response;

public interface HistoryAttendanceResponse {
    Integer getRowNumber();

    String getUserStudentFactoryId();

    String getFactoryId();

    String getSemesterId();

    String getFactoryName();

    String getProjectName();

    Long getPlanDateStartDate();

    String getPlanDateShift();

    String getStatusAttendance();

    String getPlanDateName();

    String getPlanDateDescription();

    Integer getLateArrival();

}
