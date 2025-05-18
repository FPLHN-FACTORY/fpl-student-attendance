package udpm.hn.studentattendance.core.student.historyattendance.model.response;

public interface STDHistoryAttendanceResponse {

    Integer getRowNumber();

    String getFactoryName();

    Long getPlanDateStartDate();

    Long getPlanDateEndDate();

    String getPlanDateShift();

    String getStatusAttendance();

    String getPlanDateId();

    String getPlanDateDescription();

    Integer getLateArrival();

    String getFactoryId();

    Long getCheckIn();

    Long getCheckOut();

    Integer getRequiredCheckIn();

    Integer getRequiredCheckOut();

}
