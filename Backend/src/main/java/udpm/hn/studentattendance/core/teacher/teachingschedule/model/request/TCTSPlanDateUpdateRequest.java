package udpm.hn.studentattendance.core.teacher.teachingschedule.model.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TCTSPlanDateUpdateRequest {
    private String idPlanDate;

    @Size(max = 255)
    private String description;

    // @Max(value = 60)
    private Integer lateArrival;

    @Size(max = 255)
    private String link;

}
