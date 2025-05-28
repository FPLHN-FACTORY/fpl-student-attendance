package udpm.hn.studentattendance.core.staff.statistics.model.request;

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
public class SSSendMailStatsRequest {

    private String idSemester;

    private Set<String> emailAdmin;

    private Set<String> emailStaff;

    private Set<String> emailTeacher;

    private List<Long> rangeDate;

}
