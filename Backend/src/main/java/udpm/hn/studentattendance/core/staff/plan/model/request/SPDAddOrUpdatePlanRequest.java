package udpm.hn.studentattendance.core.staff.plan.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SPDAddOrUpdatePlanRequest {

    private String id;

    private String idProject;

    private List<Long> rangeDate;

    @Max(value = 50, message = "Số buổi checkin/checkout muộn không được vượt quá 50% tổng số buổi")
    @Min(value = 0, message = "Số buổi checkin/checkout muộn không được nhỏ hơn 0%")
    private Integer maxLateArrival;

    @NotBlank(message = "Vui lòng nhập tên kế hoạch")
    @Size(max = EntityProperties.LENGTH_NAME, message = "Tên kế hoạch không được vượt quá "
            + EntityProperties.LENGTH_NAME + " ký tự")
    private String name;

    private String description;

}
