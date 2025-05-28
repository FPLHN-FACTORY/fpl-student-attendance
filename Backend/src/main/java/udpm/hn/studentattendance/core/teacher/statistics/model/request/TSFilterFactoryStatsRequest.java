package udpm.hn.studentattendance.core.teacher.statistics.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TSFilterFactoryStatsRequest extends PageableRequest {

    private String idSemester;

    private String idFacility;

    private String idUserStaff;

}
