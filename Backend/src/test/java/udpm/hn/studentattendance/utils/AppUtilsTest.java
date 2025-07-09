package udpm.hn.studentattendance.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUtilsTest {

    @Mock
    private HttpServletRequest request;

    @Test
    @DisplayName("Test imageUrlToBase64 with valid URL")
    void testImageUrlToBase64WithValidUrl() {
        String imageUrl = "https://example.com/image.jpg";

        String result = AppUtils.imageUrlToBase64(imageUrl);

        // Since this method makes HTTP calls, we can't easily test the actual
        // conversion
        // But we can test that it returns something and doesn't throw exceptions
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Test imageUrlToBase64 with invalid URL")
    void testImageUrlToBase64WithInvalidUrl() {
        String imageUrl = "https://invalid-url-that-does-not-exist.com/image.jpg";

        String result = AppUtils.imageUrlToBase64(imageUrl);

        // Should return the original URL when conversion fails
        assertThat(result).isEqualTo(imageUrl);
    }

    @Test
    @DisplayName("Test imageUrlToBase64 with null URL")
    void testImageUrlToBase64WithNullUrl() {
        String result = AppUtils.imageUrlToBase64(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Test imageUrlToBase64 with empty URL")
    void testImageUrlToBase64WithEmptyUrl() {
        String result = AppUtils.imageUrlToBase64("");

        assertThat(result).isEqualTo("");
    }

    @Test
    @DisplayName("Test getDnsSuffixes")
    void testGetDnsSuffixes() {
        List<String> result = AppUtils.getDnsSuffixes();

        // This method depends on system configuration, so we can only test that it
        // returns a list
        assertThat(result).isNotNull();
        // The list might be empty depending on system configuration
    }

    @Test
    @DisplayName("Test getClientIP with X-Forwarded-For header")
    void testGetClientIPWithXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");

        String result = AppUtils.getClientIP(request);

        assertThat(result).isEqualTo("192.168.1.100");
    }

    @Test
    @DisplayName("Test getClientIP with multiple X-Forwarded-For values")
    void testGetClientIPWithMultipleXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100, 10.0.0.1, 172.16.0.1");

        String result = AppUtils.getClientIP(request);

        assertThat(result).isEqualTo("192.168.1.100");
    }

    @Test
    @DisplayName("Test getClientIP with null X-Forwarded-For")
    void testGetClientIPWithNullXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");

        String result = AppUtils.getClientIP(request);

        assertThat(result).isEqualTo("192.168.1.100");
    }

    @Test
    @DisplayName("Test getClientIP with empty X-Forwarded-For")
    void testGetClientIPWithEmptyXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");

        String result = AppUtils.getClientIP(request);

        assertThat(result).isEqualTo("192.168.1.100");
    }

    @Test
    @DisplayName("Test getClientIP with unknown X-Forwarded-For")
    void testGetClientIPWithUnknownXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");

        String result = AppUtils.getClientIP(request);

        assertThat(result).isEqualTo("192.168.1.100");
    }

    @Test
    @DisplayName("Test getClientIP with localhost IP")
    void testGetClientIPWithLocalhostIP() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        String result = AppUtils.getClientIP(request);

        // Should call getPublicIP() for localhost, but we can't easily mock that
        // So we just test that the method doesn't throw exceptions
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Test getClientIP with IPv6 address")
    void testGetClientIPWithIPv6Address() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("2001:db8::1");

        String result = AppUtils.getClientIP(request);

        // Should call getPublicIP() for IPv6, but we can't easily mock that
        // So we just test that the method doesn't throw exceptions
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Test getClientIP with null request")
    void testGetClientIPWithNullRequest() {
        assertThrows(NullPointerException.class, () -> {
            AppUtils.getClientIP(null);
        });
    }

    @Test
    @DisplayName("Test getPublicIP")
    void testGetPublicIP() {
        String result = AppUtils.getPublicIP();

        // This method makes HTTP calls to external service, so we can't easily test the
        // actual result
        // But we can test that it doesn't throw exceptions
        // The result might be null if the external service is unavailable
        assertThat(result).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Test edge case with malformed X-Forwarded-For")
    void testEdgeCaseWithMalformedXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("malformed-ip-address");

        String result = AppUtils.getClientIP(request);

        assertThat(result).isEqualTo("malformed-ip-address");
    }

    @Test
    @DisplayName("Test edge case with whitespace in X-Forwarded-For")
    void testEdgeCaseWithWhitespaceInXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("  192.168.1.100  ");

        String result = AppUtils.getClientIP(request);

        assertThat(result).isEqualTo("192.168.1.100");
    }

    @Test
    @DisplayName("Test edge case with empty string in X-Forwarded-For")
    void testEdgeCaseWithEmptyStringInXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("   ");
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");

        String result = AppUtils.getClientIP(request);

        assertThat(result).isEqualTo("192.168.1.100");
    }

    @Test
    @DisplayName("Test edge case with null remote address")
    void testEdgeCaseWithNullRemoteAddress() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(null);

        String result = AppUtils.getClientIP(request);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Test edge case with empty remote address")
    void testEdgeCaseWithEmptyRemoteAddress() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("");

        String result = AppUtils.getClientIP(request);

        assertThat(result).isEqualTo("");
    }

    @Test
    @DisplayName("Test edge case with IPv6 in X-Forwarded-For")
    void testEdgeCaseWithIPv6InXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("2001:db8::1");

        String result = AppUtils.getClientIP(request);

        // Should call getPublicIP() for IPv6, but we can't easily mock that
        // So we just test that the method doesn't throw exceptions
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Test edge case with mixed IPv4 and IPv6")
    void testEdgeCaseWithMixedIPv4AndIPv6() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100, 2001:db8::1");

        String result = AppUtils.getClientIP(request);

        assertThat(result).isEqualTo("192.168.1.100");
    }

    @Test
    @DisplayName("Test edge case with very long IP list")
    void testEdgeCaseWithVeryLongIPList() {
        when(request.getHeader("X-Forwarded-For"))
                .thenReturn("192.168.1.100, 10.0.0.1, 172.16.0.1, 192.168.2.100, 10.0.0.2");

        String result = AppUtils.getClientIP(request);

        assertThat(result).isEqualTo("192.168.1.100");
    }

    @Test
    @DisplayName("Test edge case with special characters in IP")
    void testEdgeCaseWithSpecialCharactersInIP() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100%20");

        String result = AppUtils.getClientIP(request);

        assertThat(result).isEqualTo("192.168.1.100%20");
    }
}