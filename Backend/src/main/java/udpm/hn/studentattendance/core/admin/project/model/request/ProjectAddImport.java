package udpm.hn.studentattendance.core.admin.project.model.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ProjectAddImport {
    private String name;

    private String description;

    private String levelProjectCode;

    private String semesterCode;

    private String subjectCode;

    private String facilityCode;
}
