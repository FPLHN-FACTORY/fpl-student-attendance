package udpm.hn.studentattendance.core.staff.factory.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
public class USFactoryCreateUpdateRequest {

    private String id;

    @NotBlank(message = "Tên nhóm xưởng không được để trống")
    @Size(max = EntityProperties.LENGTH_NAME, message = "Tên nhóm xưởng chỉ được tối đa " + EntityProperties.LENGTH_NAME + " ký tự")
    private String factoryName;
    @Size(max = EntityProperties.LENGTH_TEXT, message = "Mô tả nhóm xưởng không được vượt quá:  " + EntityProperties.LENGTH_TEXT + " ký tự")
    private String factoryDescription;

    private String idProject;

    private String idUserStaff;

}
