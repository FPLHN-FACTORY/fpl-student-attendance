package udpm.hn.studentattendance.core.student.attendance.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SAFilterAttendanceRequest extends PageableRequest {

    private String idFacility;

    private String idUserStudent;

    private String keyword;

    private Integer status;

    private Integer type;

}
