package udpm.hn.studentattendance.core.staff.project.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class USProjectUpdateRequest {

    private String name;

    private String description;

    private String idLevelProject;

    private String idSemester;

    private String idSubjectFacility;

}
