package udpm.hn.studentattendance.core.admin.statistics.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADSProjectSubjectFacilityResponse;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADSSubjectFacilityChartResponse;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADStatisticsStatResponse;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADSTotalProjectAndSubjectResponse;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ADSAllStartsAndChartDTO implements Serializable {

    private List<ADSSubjectFacilityChartResponse> subjectFacilityChartResponse;

    private ADStatisticsStatResponse statisticsStatResponse;

    private List<ADSProjectSubjectFacilityResponse> projectSubjectFacilityResponses;

    private ADSTotalProjectAndSubjectResponse totalProjectAndSubjectResponse;
}
