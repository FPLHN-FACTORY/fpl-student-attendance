package udpm.hn.studentattendance.infrastructure.excel.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExExportPlanDateModel {

    private String code;

    private Set<String> planDates;

}
