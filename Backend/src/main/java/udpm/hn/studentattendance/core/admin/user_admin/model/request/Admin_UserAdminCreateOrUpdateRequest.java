package udpm.hn.studentattendance.core.admin.user_admin.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Admin_UserAdminCreateOrUpdateRequest {

    @NotBlank(message = "Không được để trống mã ban đào tạo")
    private String staffCode;

    @NotBlank(message = "Không được để trống email ban đào tạo")
    @Email(message = "Email không đúng định dạng")
    @Pattern(
            // regex: local‑part kí tự chữ số/chữ/các dấu ._%+- , sau đó @ rồi phải là 1
            // trong 3 domain
            regexp = "^[A-Za-z0-9._%+-]+@(gmail\\.com|fpt\\.edu\\.vn|fe\\.edu\\.vn)$", message = "Email phải có đuôi @gmail.com, @fpt.edu.vn hoặc @fe.edu.vn")
    private String email;

    @NotBlank(message = "Không được để trống tên ban đào tạo")
    private String staffName;

}
