package udpm.hn.studentattendance.core.notification.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationAddRequest {

    private String idUser;

    private int type;

    private Map<String, Object> data;

}
