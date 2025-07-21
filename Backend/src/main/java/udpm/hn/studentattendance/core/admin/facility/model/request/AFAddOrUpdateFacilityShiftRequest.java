package udpm.hn.studentattendance.core.admin.facility.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AFAddOrUpdateFacilityShiftRequest {

    private String id;

    private String idFacility;

    @Min(value = 1, message = "Ca sớm nhất là ca 1")
    @Max(value = 6, message = "Ca muộn nhất là ca 6")
    private Integer shift;

    @Min(value = 0, message = "Giờ bắt đầu không hợp lệ")
    @Max(value = 23, message = "Giờ bắt đầu không hợp lệ")
    private Integer fromHour;

    @Min(value = 0, message = "Phút bắt đầu không hợp lệ")
    @Max(value = 59, message = "Phút bắt đầu không hợp lệ")
    private Integer fromMinute;

    @Min(value = 0, message = "Giờ kết thúc không hợp lệ")
    @Max(value = 23, message = "Giờ kết thúc không hợp lệ")
    private Integer toHour;

    @Min(value = 0, message = "Phút kết thúc không hợp lệ")
    @Max(value = 59, message = "Phút kết thúc không hợp lệ")
    private Integer toMinute;

}
