package udpm.hn.studentattendance.core.admin.facility.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AFFilterFacilityShiftRequest extends PageableRequest {

    private String idFacility;

    private Integer shift;

    private Integer status;

}
