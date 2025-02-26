package udpm.hn.studentattendance.core.staff.projectmanagement.model.response;

import org.springframework.beans.factory.annotation.Value;

public interface ProjectResponse {

    Integer getIndexs();

    String getId();

    String getName();

    String getNameLevelProject();

    String getNameSubjectFacility();

    String getStatus();
}
