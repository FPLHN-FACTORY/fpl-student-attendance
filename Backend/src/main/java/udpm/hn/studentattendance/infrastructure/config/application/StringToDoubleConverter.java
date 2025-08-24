package udpm.hn.studentattendance.infrastructure.config.application;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToDoubleConverter implements Converter<String, Double> {
    @Override
    public Double convert(String source) {
        if (source == null || source.trim().isEmpty() || "null".equalsIgnoreCase(source)) {
            return null;
        }
        return Double.valueOf(source);
    }
}
