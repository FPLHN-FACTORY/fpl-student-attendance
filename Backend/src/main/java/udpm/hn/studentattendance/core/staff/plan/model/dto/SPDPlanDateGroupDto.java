package udpm.hn.studentattendance.core.staff.plan.model.dto;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateResponse;

import java.util.List;
import java.util.Set;

@Setter
@Getter
public class SPDPlanDateGroupDto {

    private Long orderNumber;

    private Long startDate;

    private String day;

    private Integer totalShift;

    private Set<Integer> types;

    private String status;

    private List<SPDPlanDateResponse> planDates;

}
