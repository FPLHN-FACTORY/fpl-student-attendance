package udpm.hn.studentattendance.core.admin.statistics.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ADStatisticRequest {
    private Long fromDay;

    private Long toDay;
}
