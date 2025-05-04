package udpm.hn.studentattendance.infrastructure.config.websocket.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import udpm.hn.studentattendance.infrastructure.config.websocket.model.message.AttendanceMessage;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteWebsocketConstant;

@Controller
public class WSAttendanceController {

    @MessageMapping(RouteWebsocketConstant.APP_ATTENDANCE)
    @SendTo(RouteWebsocketConstant.TOPIC_ATTENDANCE)
    private AttendanceMessage handleAttendance(AttendanceMessage message) {
        return message;
    }

}
