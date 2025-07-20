package udpm.hn.studentattendance.core.staff.plan.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SPDAddOrUpdatePlanDateRequest {

    private String idFacility;

    private String idPlanFactory;

    private String id;

    private Long startDate;

    private String link;

    @Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Phòng học phải có ít nhất 2 ký tự và không được vượt quá "
            + EntityProperties.LENGTH_NAME + " ký tự")
    private String room;

    private Integer requiredIp = StatusType.ENABLE.getKey();

    private Integer requiredLocation = StatusType.ENABLE.getKey();

    private Integer requiredCheckin = StatusType.ENABLE.getKey();

    private Integer requiredCheckout = StatusType.ENABLE.getKey();

    private List<Integer> shift;

    private Integer type;

    @Min(value = 0, message = "Thời gian điểm danh muộn nhất phải lớn hơn hoặc bằng 0")
    private Integer lateArrival;

    private String description;

    private List<Long> customTime;

}
