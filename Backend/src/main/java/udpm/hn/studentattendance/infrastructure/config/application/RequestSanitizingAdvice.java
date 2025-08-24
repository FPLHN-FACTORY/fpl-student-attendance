package udpm.hn.studentattendance.infrastructure.config.application;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

@ControllerAdvice
public class RequestSanitizingAdvice extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage,
                                MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        sanitizeObject(body);
        return body;
    }

    private void sanitizeObject(Object obj) {
        if (obj == null) return;
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.getType() == String.class) {
                field.setAccessible(true);
                try {
                    String value = (String) field.get(obj);
                    if (value != null) {
                        field.set(obj, sanitize(value));
                    }
                } catch (IllegalAccessException ignored) {}
            }
        }
    }

    private static String sanitize(String input) {
        return input.replaceAll("[^\\p{L}\\p{N}\\s~`!@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?\\\\]", "").replaceAll("\\s+", " ").trim();
    }

}


