package udpm.hn.studentattendance.core.staff.factory.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentFactoryCreateUpdateRequest {

    private String studentId;

    private String factoryId;
}
