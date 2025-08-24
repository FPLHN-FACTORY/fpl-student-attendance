package udpm.hn.studentattendance.core.admin.semester.model.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Getter
@Setter
@ToString
public class ADSemesterRequest extends PageableRequest {
    @Size(max = EntityProperties.LENGTH_NAME, message = "Từ khóa không được quá:" + EntityProperties.LENGTH_NAME)
    private String semesterCode;

    private Long fromDateSemester;

    private Long toDateSemester;

    private EntityStatus status;

}
