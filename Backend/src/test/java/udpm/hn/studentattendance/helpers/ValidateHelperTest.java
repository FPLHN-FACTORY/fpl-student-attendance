package udpm.hn.studentattendance.helpers;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class ValidateHelperTest {
    @Test
    void canInstantiate() {
        ValidateHelper helper = new ValidateHelper();
        assertThat(helper).isNotNull();
    }

    @Test
    void testIsValidPhoneNumber() {
        assertThat(ValidateHelper.isValidPhoneNumber("0123456789")).isTrue();
        assertThat(ValidateHelper.isValidPhoneNumber("01234567890")).isTrue();
        assertThat(ValidateHelper.isValidPhoneNumber("123456789")).isFalse();
        assertThat(ValidateHelper.isValidPhoneNumber(null)).isFalse();
        assertThat(ValidateHelper.isValidPhoneNumber("")).isFalse();
    }

    @Test
    void testIsValidEmail() {
        assertThat(ValidateHelper.isValidEmail("test@example.com")).isTrue();
        assertThat(ValidateHelper.isValidEmail("invalid-email")).isFalse();
        assertThat(ValidateHelper.isValidEmail(null)).isFalse();
    }

    @Test
    void testIsValidFullname() {
        assertThat(ValidateHelper.isValidFullname("Nguyen Van A")).isTrue();
        assertThat(ValidateHelper.isValidFullname("A")).isFalse();
        assertThat(ValidateHelper.isValidFullname(null)).isFalse();
    }

    @Test
    void testIsValidCode() {
        assertThat(ValidateHelper.isValidCode("CODE_123")).isTrue();
        assertThat(ValidateHelper.isValidCode("code.test")).isTrue();
        assertThat(ValidateHelper.isValidCode("code test")).isFalse();
        assertThat(ValidateHelper.isValidCode(null)).isFalse();
    }

    @Test
    void testIsValidEmailFE() {
        assertThat(ValidateHelper.isValidEmailFE("abc@fe.edu.vn")).isTrue();
        assertThat(ValidateHelper.isValidEmailFE("abc@gmail.com")).isFalse();
        assertThat(ValidateHelper.isValidEmailFE(null)).isFalse();
    }

    @Test
    void testIsValidEmailFPT() {
        assertThat(ValidateHelper.isValidEmailFPT("abc@fpt.edu.vn")).isTrue();
        assertThat(ValidateHelper.isValidEmailFPT("abc@gmail.com")).isFalse();
        assertThat(ValidateHelper.isValidEmailFPT(null)).isFalse();
    }

    @Test
    void testIsValidEmailGmail() {
        assertThat(ValidateHelper.isValidEmailGmail("abc@gmail.com")).isTrue();
        assertThat(ValidateHelper.isValidEmailGmail("abc@fpt.edu.vn")).isFalse();
        assertThat(ValidateHelper.isValidEmailGmail(null)).isFalse();
    }

    @Test
    void testIsValidDomain() {
        assertThat(ValidateHelper.isValidDomain("fpt.edu.vn")).isTrue();
        assertThat(ValidateHelper.isValidDomain("invalid_domain")).isFalse();
        assertThat(ValidateHelper.isValidDomain(null)).isFalse();
    }

    @Test
    void testIsValidIPv4() {
        assertThat(ValidateHelper.isValidIPv4("192.168.1.1")).isTrue();
        assertThat(ValidateHelper.isValidIPv4("::1")).isFalse();
        assertThat(ValidateHelper.isValidIPv4(null)).isFalse();
    }

    @Test
    void testIsValidIPv6() {
        assertThat(ValidateHelper.isValidIPv6("::1")).isTrue();
        assertThat(ValidateHelper.isValidIPv6("192.168.1.1")).isFalse();
        assertThat(ValidateHelper.isValidIPv6(null)).isFalse();
    }

    @Test
    void testIsValidIPv4CIDR() {
        assertThat(ValidateHelper.isValidIPv4CIDR("192.168.1.0/24")).isTrue();
        assertThat(ValidateHelper.isValidIPv4CIDR("192.168.1.0/33")).isFalse();
        assertThat(ValidateHelper.isValidIPv4CIDR(null)).isFalse();
    }

    @Test
    void testIsValidIPv6CIDR() {
        assertThat(ValidateHelper.isValidIPv6CIDR("2001:db8::/32")).isTrue();
        assertThat(ValidateHelper.isValidIPv6CIDR("invalid")).isFalse();
        assertThat(ValidateHelper.isValidIPv6CIDR(null)).isFalse();
    }

    @Test
    void testIsLocalhost() {
        assertThat(ValidateHelper.isLocalhost("127.0.0.1")).isTrue();
        assertThat(ValidateHelper.isLocalhost("::1")).isTrue();
        assertThat(ValidateHelper.isLocalhost("0:0:0:0:0:0:0:1")).isTrue();
        assertThat(ValidateHelper.isLocalhost("192.168.1.1")).isFalse();
    }

    @Test
    void testIsValidDnsSuffix() {
        assertThat(ValidateHelper.isValidDnsSuffix("fpt.edu.vn")).isTrue();
        assertThat(ValidateHelper.isValidDnsSuffix("-invalid.vn")).isFalse();
        assertThat(ValidateHelper.isValidDnsSuffix("")).isFalse();
        assertThat(ValidateHelper.isValidDnsSuffix(null)).isFalse();
    }

    @Test
    void testIsValidURL() {
        assertThat(ValidateHelper.isValidURL("https://fpt.edu.vn")).isTrue();
        assertThat(ValidateHelper.isValidURL("http://fpt.edu.vn/path?query=1")).isTrue();
        assertThat(ValidateHelper.isValidURL("invalid-url")).isFalse();
        assertThat(ValidateHelper.isValidURL(null)).isFalse();
    }

    @Test
    void testIsAllowedIP() {
        Set<String> allowed = new HashSet<>();
        allowed.add("192.168.1.0/24");
        allowed.add("127.0.0.1");
        assertThat(ValidateHelper.isAllowedIP("192.168.1.10", allowed)).isTrue();
        assertThat(ValidateHelper.isAllowedIP("127.0.0.1", allowed)).isTrue();
        assertThat(ValidateHelper.isAllowedIP("10.0.0.1", allowed)).isFalse();
    }

    @Test
    void testIsIPInIPv6Range() {
        assertThat(ValidateHelper.isIPInIPv6Range("2001:db8::1", "2001:db8::/32")).isTrue();
        assertThat(ValidateHelper.isIPInIPv6Range("fe80::1", "2001:db8::/32")).isFalse();
        assertThat(ValidateHelper.isIPInIPv6Range("invalid", "2001:db8::/32")).isFalse();
    }
}
