package udpm.hn.studentattendance.core.staff.factory.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Getter
@Setter
public class StudentFactoryRequest {
    private String studentId;

    private String factoryId;

    private String studentFactoryId;
    
    private String searchQuery;

    private EntityStatus status;
}
