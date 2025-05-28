package udpm.hn.studentattendance.core.teacher.statistics.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSAllStatsResponse;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSChartLevelProjectResponse;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSChartSubjectFacilityResponse;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TSAllStatsAndChartDto implements Serializable {

    private TSAllStatsResponse stats;

    private List<TSChartLevelProjectResponse> levelStats;

    private List<TSChartSubjectFacilityResponse> subjectFacilityStats;

}
