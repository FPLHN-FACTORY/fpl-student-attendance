package udpm.hn.studentattendance.infrastructure.config.websocket.model.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceMessage {

    private String userStudentId;

    private String planDateId;

}
