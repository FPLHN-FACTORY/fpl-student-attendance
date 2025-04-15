package udpm.hn.studentattendance.core.notification.model.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import udpm.hn.studentattendance.helpers.NotificationHelper;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.util.Map;

@Getter
public class NotificationResponse {

    private String id;

    private Integer type;

    private Map<String, Object> data;

    private Integer status;

    private Long createdAt;

    private String message;

    private static final ObjectMapper mapper = new ObjectMapper();

    public NotificationResponse(String id, Integer type, String dataJson, EntityStatus status, Long createdAt) {
        this.id = id;
        this.type = type;
        this.status = status.ordinal();
        this.createdAt = createdAt;
        try {
            this.data = mapper.readValue(dataJson, new TypeReference<>() {});
        } catch (Exception e) {
            this.data = Map.of();
        }
        this.message = NotificationHelper.renderMessage(this);
    }

}
