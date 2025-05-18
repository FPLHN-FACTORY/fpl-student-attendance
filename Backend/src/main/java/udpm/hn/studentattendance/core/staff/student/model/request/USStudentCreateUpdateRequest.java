package udpm.hn.studentattendance.core.staff.student.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class USStudentCreateUpdateRequest {

    private String id;
    private String code;
    private String name;
    private String email;

}
