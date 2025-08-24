package udpm.hn.studentattendance.core.staff.project.model.response;

public interface USProjectResponse {

    Integer getIndexs();

    String getId();

    String getName();

    String getNameLevelProject();

    String getNameSubject();

    String getNameSemester();

    String getDescription();

    Integer getStatus();

    Integer getCurrentStatus();

    String getFacilityName();

    String getLevelProjectId();

    String getSemesterId();

    String getSubjectId();

    String getSubjectFacilityId();

    Integer getTotalPlanDate();

}
