package udpm.hn.studentattendance.core.admin.semester.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class SemesterRequest extends PageableRequest {
    private String semesterName;
    private Long fromDateSemester;
    private Long toDateSemester;

}
