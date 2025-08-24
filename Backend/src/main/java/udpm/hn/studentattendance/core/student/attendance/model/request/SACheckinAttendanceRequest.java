package udpm.hn.studentattendance.core.student.attendance.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SACheckinAttendanceRequest {

    private String idPlanDate;

    private Double latitude;

    private Double longitude;

    private String signature;

}
