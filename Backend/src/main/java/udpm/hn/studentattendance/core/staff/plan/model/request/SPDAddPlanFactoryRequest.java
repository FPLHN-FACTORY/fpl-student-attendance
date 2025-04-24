package udpm.hn.studentattendance.core.staff.plan.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SPDAddPlanFactoryRequest {

    private String idPlan;

    private String idFactory;

    private List<Integer> days;

    private Integer type;

    private String link;

    private Integer requiredIp = StatusType.ENABLE.getKey();

    private Integer requiredLocation = StatusType.ENABLE.getKey();

    private List<Integer> shift;

    @Min(value = 0, message = "Thời gian điểm danh muộn nhất phải lớn hơn 0")
    private Integer lateArrival;

}
