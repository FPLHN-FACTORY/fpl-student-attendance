package udpm.hn.studentattendance.core.admin.adminprojectmanagement.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProjectUpdateRequest {

    private String name;

    private String description;

    private String idLevelProject;

    private String idSemester;

    private String idSubject;

    private String status;

}
