package udpm.hn.studentattendance.core.teacher.factory.model.response;

public interface TCStudentFactoryResponse {

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

    Integer getTotalLateAttendance();

    Integer getCurrentLateAttendance();

}
