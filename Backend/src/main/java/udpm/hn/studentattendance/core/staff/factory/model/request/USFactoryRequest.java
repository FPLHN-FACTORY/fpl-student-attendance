package udpm.hn.studentattendance.core.staff.factory.model.request;

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
public class USFactoryRequest extends PageableRequest {
    private String idProject;

    private String idFacility;

    @Size(max = EntityProperties.LENGTH_NAME, message = "Tên nhóm xưởng không được vượt quá " + EntityProperties.LENGTH_NAME + " ký tự")
    private String factoryName;

    private String idSemester;

    private String idUserStaff;

    private Integer status;

}
