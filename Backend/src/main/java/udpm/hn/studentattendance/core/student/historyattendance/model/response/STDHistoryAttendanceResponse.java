package udpm.hn.studentattendance.core.student.historyattendance.model.response;

public interface STDHistoryAttendanceResponse {

    Integer getRowNumber();

    String getFactoryName();


    Long getPlanDateStartDate();

    String getPlanDateShift();

    String getStatusAttendance();


    String getPlanDateDescription();

    Integer getLateArrival();

    Long getPlanDateEndDate();

    String getFactoryId();

    Long getCheckIn();

    Long getCheckOut();

}
