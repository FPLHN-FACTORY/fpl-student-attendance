package udpm.hn.studentattendance.infrastructure.config.application;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
class StringToLongConverter implements Converter<String, Long> {
    @Override
    public Long convert(String source) {
        if (source == null || source.trim().isEmpty() || "null".equalsIgnoreCase(source)) {
            return null;
        }
        return Long.valueOf(source);
    }
}
