package udpm.hn.studentattendance.infrastructure.config.application;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
class StringToIntegerConverter implements Converter<String, Integer> {
    @Override
    public Integer convert(String source) {
        if (source == null || source.trim().isEmpty() || "null".equalsIgnoreCase(source)) {
            return null;
        }
        return Integer.valueOf(source);
    }
}
