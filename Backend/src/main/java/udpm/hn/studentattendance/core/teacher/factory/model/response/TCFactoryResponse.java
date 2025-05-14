package udpm.hn.studentattendance.core.teacher.factory.model.response;

public interface TCFactoryResponse {

    Integer getRowNumber();

    String getFactoryId();

    String getProjectId();

    String getUserStaffId();

    String getFactoryName();

    String getProjectName();

    String getFactoryDescription();

    Integer getFactoryStatus();

    Integer getTotalShift();

    Integer getTotalStudent();

}
