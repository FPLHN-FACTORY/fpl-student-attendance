package udpm.hn.studentattendance.core.teacher.statistics.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TSSendMailStatsRequest {

    private String idSemester;

    private Set<String> emailAdmin;

    private Set<String> emailStaff;

    private List<Long> rangeDate;

}
