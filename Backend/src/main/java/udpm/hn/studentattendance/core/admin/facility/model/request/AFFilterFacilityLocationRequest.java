package udpm.hn.studentattendance.core.admin.facility.model.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AFFilterFacilityLocationRequest extends PageableRequest {

    private String idFacility;

    @Size(max = EntityProperties.LENGTH_NAME, message = "Keyword không được vượt quá " + EntityProperties.LENGTH_NAME + " ký tự")
    private String keyword;

    private Integer status;

}
