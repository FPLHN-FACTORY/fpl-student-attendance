package udpm.hn.studentattendance.core.student.statistics.model.dto;

import lombok.*;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsAttendanceChartResponse;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsFactoryChartResponse;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsStatResponse;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class STDStatisticDto {

    private STDStatisticsStatResponse stdStatisticsStatResponse;

    private List<STDStatisticsFactoryChartResponse> factoryChartResponse;

    private STDStatisticsAttendanceChartResponse attendanceChartResponses;

}
