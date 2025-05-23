package udpm.hn.studentattendance.core.staff.factory.model.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Getter
@Setter
public class USFactoryRequest extends PageableRequest {
    private String idProject;

    private String idStaff;

    private String idFacility;
    @Size(max = EntityProperties.LENGTH_NAME, message = "Tên nhóm xưởng không được vượt quá 255 ký tự")
    private String factoryName; // dùng thay cho searchQuery

    private String idSemester;

    private EntityStatus status;

}
