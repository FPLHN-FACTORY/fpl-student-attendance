package udpm.hn.studentattendance.core.teacher.teaching_schedule.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import javax.swing.text.html.parser.Entity;

@Getter
@Setter
public class Teacher_TSPlanDateUpdateRequest {
    private String idPlanDate;

    @Size(max = 255)
    private String description;

//    @Max(value = 60)
    private Integer lateArrival;

}
