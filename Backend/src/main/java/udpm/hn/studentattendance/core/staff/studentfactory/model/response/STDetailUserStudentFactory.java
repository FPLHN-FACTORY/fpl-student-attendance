package udpm.hn.studentattendance.core.staff.studentfactory.model.response;

public interface STDetailUserStudentFactory {
    String getId();

    String getUserStudentCode();

    String getUserStudentName();

    Integer getUserStudentStatus();

    String getStudentActiveShift();

    Long getStartDate();

    Long getEndDate();

    String getSemesterCode();


}
