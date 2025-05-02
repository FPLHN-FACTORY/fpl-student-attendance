package udpm.hn.studentattendance.core.staff.factory.model.request.userstudentfactory;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class USPDDetailShiftByStudentRequest extends PageableRequest {

    @Size(max = 255, message = "Keyword không được vượt quá 255 ký tự")
    private String keyword;

    private Integer shift;

    private Integer type;

    private Long startDate;

    private String status;
}
