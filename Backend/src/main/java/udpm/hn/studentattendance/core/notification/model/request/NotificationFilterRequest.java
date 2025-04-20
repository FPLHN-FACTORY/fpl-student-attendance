package udpm.hn.studentattendance.core.notification.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationFilterRequest extends PageableRequest {

    private String idUser;

    private Integer status;

    private EntityStatus entityStatus;

}
