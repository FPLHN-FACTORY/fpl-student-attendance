package udpm.hn.studentattendance.infrastructure.constants.router;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RouteWebsocketConstantTest {
    @Test
    void testConstantsNotNullOrEmpty() {
        assertNotNull(RouteWebsocketConstant.END_POINT);
        assertNotNull(RouteWebsocketConstant.PREFIX_AD);
        assertNotNull(RouteWebsocketConstant.PREFIX_SIMPLE_BROKER);
        assertNotNull(RouteWebsocketConstant.APP_ATTENDANCE);
        assertNotNull(RouteWebsocketConstant.TOPIC_ATTENDANCE);
    }
}
