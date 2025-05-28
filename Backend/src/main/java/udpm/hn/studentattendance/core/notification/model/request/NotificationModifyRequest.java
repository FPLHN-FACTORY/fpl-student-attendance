package udpm.hn.studentattendance.core.notification.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationModifyRequest {

    private String idUser;

    private List<String> ids;

}
