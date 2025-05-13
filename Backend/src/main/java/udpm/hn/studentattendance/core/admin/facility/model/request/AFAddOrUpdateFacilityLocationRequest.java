package udpm.hn.studentattendance.core.admin.facility.model.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AFAddOrUpdateFacilityLocationRequest {

    private String id;

    private String idFacility;

    @NotBlank(message = "Vui lòng nhập tên địa điểm")
    @Size(max = EntityProperties.LENGTH_NAME, message = "Tên địa điểm không được vượt quá "
            + EntityProperties.LENGTH_NAME + " ký tự")
    private String name;

    @DecimalMin(value = "-90.0", message = "Vĩ độ không được nhỏ hơn -90", inclusive = true)
    @DecimalMax(value = "90.0", message = "Vĩ độ không được lớn hơn 90", inclusive = true)
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Kinh độ không được nhỏ hơn -180", inclusive = true)
    @DecimalMax(value = "180.0", message = "Kinh độ không được lớn hơn 180", inclusive = true)
    private Double longitude;

    @Min(value = 1, message = "Bán kính tối thiểu là 1m")
    @Max(value = 500, message = "Bán kính tối đa là 500m")
    private Integer radius = 0;

}
