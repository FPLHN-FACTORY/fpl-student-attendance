package udpm.hn.studentattendance.helpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;


@Slf4j
@Component
public class RequestTrimHelper {

    public static <T> T trimStringFields(T request) {
        if (request == null) {
            return null;
        }

        try {
            Class<?> clazz = request.getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(request);

                if (value instanceof String) {
                    String stringValue = (String) value;
                    if (stringValue != null) {
                        String trimmedValue = stringValue.trim();
                        field.set(request, trimmedValue);
                    }
                } else if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> list = (List<Object>) value;
                    if (list != null) {
                        for (int i = 0; i < list.size(); i++) {
                            Object item = list.get(i);
                            if (item instanceof String) {
                                String stringItem = (String) item;
                                if (stringItem != null) {
                                    list.set(i, stringItem.trim());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to trim string fields in request object: {}", e.getMessage());
        }

        return request;
    }

    public static <T> T trimStringFieldsWithLogging(T request) {
        if (request == null) {
            return null;
        }

        log.debug("Trimming string fields in request object: {}", request.getClass().getSimpleName());
        return trimStringFields(request);
    }
}