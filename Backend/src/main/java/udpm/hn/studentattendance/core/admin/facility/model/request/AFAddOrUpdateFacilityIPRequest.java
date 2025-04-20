package udpm.hn.studentattendance.core.admin.facility.model.request;

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
public class AFAddOrUpdateFacilityIPRequest {

    private String id;

    private String idFacility;

    @NotBlank(message = "Vui lòng nhập IP/Dải IP")
    @Size(max = EntityProperties.LENGTH_NAME, message = "IP không được vượt quá " + EntityProperties.LENGTH_NAME + " ký tự")
    private String ip;

    private Integer type;

}
