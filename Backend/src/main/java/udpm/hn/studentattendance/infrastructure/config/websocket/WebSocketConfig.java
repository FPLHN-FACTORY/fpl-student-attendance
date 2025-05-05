package udpm.hn.studentattendance.infrastructure.config.websocket;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteWebsocketConstant;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${allowed.origin}")
    public String ALLOWED_ORIGIN;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        if (StringUtils.hasText(ALLOWED_ORIGIN)) {
            String[] origins = ALLOWED_ORIGIN.split(",");
            registry.addEndpoint(RouteWebsocketConstant.END_POINT)
                    .setAllowedOrigins(origins)
                    .withSockJS();
        }
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes(RouteWebsocketConstant.PREFIX_AD);
        registry.enableSimpleBroker(RouteWebsocketConstant.PREFIX_SIMPLE_BROKER);
    }

}
