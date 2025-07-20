package udpm.hn.studentattendance.core.staff.attendancerecovery.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
public class STCreateOrUpdateNewEventRequest {

    @NotBlank(message = "Không được để trống tên sự kiện")
    @Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Tên sự kiện phải có ít nhất 2 ký tự và chỉ được tối đa "
            + EntityProperties.LENGTH_NAME + " ký tự")
    private String name;

    private String description;

    private Long day;

}
