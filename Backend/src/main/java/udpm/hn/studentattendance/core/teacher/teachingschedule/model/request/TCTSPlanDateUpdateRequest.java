package udpm.hn.studentattendance.core.teacher.teachingschedule.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
public class TCTSPlanDateUpdateRequest {

    private String idPlanDate;

    private String description;

    @Min(value = 0, message = "Thời gian điểm danh muộn nhất phải lớn hơn hoặc bằng 0")
    private Integer lateArrival;

    private String link;

    @Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Phòng học không được vượt quá "
            + EntityProperties.LENGTH_NAME + " ký tự")
    private String room;

}
