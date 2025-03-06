package udpm.hn.studentattendance.core.staff.factory.model.response;

public interface FactoryResponse {
    String getFactoryId();
    String getFactoryName();
    Integer getFactoryStatus();
    String getProjectName();
    String getSubjectCode();
    String getStaffName();
    Long getPlanStartDate();
    String getPlanShift();
}
