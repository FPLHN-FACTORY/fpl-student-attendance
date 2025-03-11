package udpm.hn.studentattendance.core.staff.factory.model.response;

public interface Staff_UserStudentResponse {
    String getStudentFactoryId();

    String getStudentId();

    String getFactoryId();

    String getStudentCode();

    String getStudentName();

    String getStudentEmail();

    Boolean isChecked();

    Integer getRowNumber();
}
