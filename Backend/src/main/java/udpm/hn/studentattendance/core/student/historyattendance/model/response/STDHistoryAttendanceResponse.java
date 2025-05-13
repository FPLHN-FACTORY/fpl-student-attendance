package udpm.hn.studentattendance.core.student.historyattendance.model.response;

public interface STDHistoryAttendanceResponse {
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

    Long getPlanDateEndDate();

}
