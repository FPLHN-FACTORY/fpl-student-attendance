package udpm.hn.studentattendance.core.support.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupportEmailRequest {

    private String email;

    @NotBlank(message = "Không được để trống tiêu đề hỗ trợ")
    @Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Tiêu đề hỗ trợ phải có ít nhất 2 ký tự và không được vượt quá "
            + EntityProperties.LENGTH_NAME + " ký tự")
    private String title;

    private String message;

    private String code;

    private MultipartFile[] files;
}
