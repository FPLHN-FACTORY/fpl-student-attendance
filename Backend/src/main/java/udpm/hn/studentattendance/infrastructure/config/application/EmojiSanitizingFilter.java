package udpm.hn.studentattendance.infrastructure.config.application;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class EmojiSanitizingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        HttpServletRequestWrapper wrapped = new HttpServletRequestWrapper(request) {
            @Override
            public String getParameter(String name) {
                return sanitize(super.getParameter(name));
            }

            @Override
            public String[] getParameterValues(String name) {
                String[] values = super.getParameterValues(name);
                if (values == null) return null;
                return Arrays.stream(values).map(EmojiSanitizingFilter::sanitize).toArray(String[]::new);
            }
        };

        filterChain.doFilter(wrapped, response);
    }

    private static String sanitize(String input) {
        if (input == null) return null;
        return input.replaceAll("[^\\p{L}\\p{N}\\s~`!@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?\\\\]", "").trim();
    }

}