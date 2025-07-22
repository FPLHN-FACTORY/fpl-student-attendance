package udpm.hn.studentattendance.core.staff.plan.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SPDDeletePlanDateRequest {

    private String idPlanFactory;

    private List<String> days;

}
