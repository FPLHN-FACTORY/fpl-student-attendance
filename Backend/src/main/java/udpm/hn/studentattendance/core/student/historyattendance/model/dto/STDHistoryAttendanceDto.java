package udpm.hn.studentattendance.core.student.historyattendance.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.core.student.historyattendance.model.response.STDHistoryAttendanceResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class STDHistoryAttendanceDto {

    private PageableObject<STDHistoryAttendanceResponse> page;

    private Integer totalShift;

    private Integer totalPresent;

    private Integer totalAbsent;

}
