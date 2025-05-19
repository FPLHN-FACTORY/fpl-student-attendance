package udpm.hn.studentattendance.core.staff.factory.model.response.userstudentfactory;

public interface STStudentFactoryResponse {
    Integer getRowNumber();

    String getStudentFactoryId();

    String getStudentId();

    String getFactoryId();

    String getStudentCode();

    String getStudentName();

    String getStudentEmail();

    Integer getStatusStudentFactory();

    Integer getTotalAbsentShift();

    Integer getTotalShift();

}
