package udpm.hn.studentattendance.core.staff.plan.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.helpers.ShiftHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SPDAddOrUpdatePlanDateRequest {

    private String idFacility;

    private String idPlanFactory;

    private String id;

    private Long startDate;

    private String link;

    private Integer requiredIp = StatusType.ENABLE.getKey();

    private Integer requiredLocation = StatusType.ENABLE.getKey();

    private List<Integer> shift;

    private Integer type;

    @Min(value = 0, message = "Thời gian điểm danh muộn nhất phải lớn hơn hoặc bằng 0")
    @Max(value = ShiftHelper.MAX_LATE_ARRIVAL, message = "Thời gian điểm danh muộn nhất không quá " + ShiftHelper.MAX_LATE_ARRIVAL + " phút")
    private Integer lateArrival;

    @NotBlank(message = "Vui lòng nhập nội dung buổi học")
    private String description;

}
