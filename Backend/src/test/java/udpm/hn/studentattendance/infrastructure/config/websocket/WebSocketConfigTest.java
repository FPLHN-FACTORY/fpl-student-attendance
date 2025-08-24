package udpm.hn.studentattendance.infrastructure.config.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import static org.junit.jupiter.api.Assertions.*;

class WebSocketConfigTest {
    @Test
    void testWebSocketConfigExists() {
        WebSocketConfig config = new WebSocketConfig();
        assertNotNull(config);
    }

    @Test
    void testConfigureMessageBrokerMethod() throws Exception {
        WebSocketConfig config = new WebSocketConfig();
        assertNotNull(config.getClass().getMethod("configureMessageBroker", MessageBrokerRegistry.class));
    }

    @Test
    void testRegisterStompEndpointsMethod() throws Exception {
        WebSocketConfig config = new WebSocketConfig();
        assertNotNull(config.getClass().getMethod("registerStompEndpoints", StompEndpointRegistry.class));
    }
}
