package udpm.hn.studentattendance.core.staff.plan.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class SPDAddOrUpdatePlanDateRequest {

    private String idFacility;

    private String idPlanFactory;

    private String id;

    private Long startDate;

    private String link;

    @Min(value = 1, message = "Ca học sớm nhất là ca 1")
    @Max(value = 6, message = "Ca học muộn nhất là ca 6")
    private Integer shift;

    private Integer type;

    @Min(value = 0, message = "Thời gian điểm danh muộn nhất phải lớn hơn 0")
    @Max(value = 60, message = "Thời gian điểm danh muộn nhất không quá 60 phút")
    private Integer lateArrival;

    @NotBlank(message = "Vui lòng nhập nội dung buổi học")
    private String description;

}
