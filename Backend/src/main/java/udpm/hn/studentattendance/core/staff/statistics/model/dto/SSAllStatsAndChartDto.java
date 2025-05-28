package udpm.hn.studentattendance.core.staff.statistics.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.core.staff.statistics.model.response.SSAllStatsResponse;
import udpm.hn.studentattendance.core.staff.statistics.model.response.SSChartLevelProjectResponse;
import udpm.hn.studentattendance.core.staff.statistics.model.response.SSChartSubjectFacilityResponse;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SSAllStatsAndChartDto implements Serializable {

    private SSAllStatsResponse stats;

    private List<SSChartLevelProjectResponse> levelStats;

    private List<SSChartSubjectFacilityResponse> subjectFacilityStats;

}
