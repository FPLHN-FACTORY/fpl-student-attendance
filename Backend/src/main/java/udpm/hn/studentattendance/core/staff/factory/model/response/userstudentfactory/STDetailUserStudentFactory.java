package udpm.hn.studentattendance.core.staff.factory.model.response.userstudentfactory;

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
