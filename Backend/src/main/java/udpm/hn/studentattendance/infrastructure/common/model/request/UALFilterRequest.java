package udpm.hn.studentattendance.infrastructure.common.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UALFilterRequest extends PageableRequest {

    private String facilityId;

    private String userId;

    private Integer role;

}
