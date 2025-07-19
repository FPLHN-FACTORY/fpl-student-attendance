package udpm.hn.studentattendance.core.staff.studentfactory.model.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class USPDDetailShiftByStudentRequest extends PageableRequest {

    @Size(max = EntityProperties.LENGTH_NAME, message = "Keyword không được vượt quá " + EntityProperties.LENGTH_NAME + " ký tự")
    private String keyword;

    private Integer shift;

    private Integer type;

    private Long startDate;

    private String status;
}
