package udpm.hn.studentattendance.core.teacher.factory.model.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TCFilterPlanDateAttendanceRequest extends PageableRequest {
    
    @Size(max = EntityProperties.LENGTH_NAME, message = "Từ khóa không được quá:" + EntityProperties.LENGTH_NAME)
    private String keyword;

    private Integer status;

    private String idPlanDate;

    private String idFacility;

    private String idUserStudent;

}
