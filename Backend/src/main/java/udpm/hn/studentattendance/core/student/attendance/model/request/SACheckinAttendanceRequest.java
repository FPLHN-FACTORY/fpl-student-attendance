package udpm.hn.studentattendance.core.student.attendance.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SACheckinAttendanceRequest {

    private String idPlanDate;

    private Double latitude;

    private Double longitude;

}
