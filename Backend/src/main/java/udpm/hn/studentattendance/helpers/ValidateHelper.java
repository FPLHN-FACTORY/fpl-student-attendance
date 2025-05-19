package udpm.hn.studentattendance.helpers;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;
import udpm.hn.studentattendance.utils.AppUtils;

public class ValidateHelper {

    private static final String CODE_REGEX = "^[a-zA-Z0-9._]+$";

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private static final String EMAIL_FE_REGEX = "^[A-Za-z0-9._%+-]+@fe.edu.vn$";

    private static final String EMAIL_FPT_REGEX = "^[A-Za-z0-9._%+-]+@fpt.edu.vn$";

    private static final String PHONE_REGEX = "^0[0-9]{9,10}$";

    private static final String FULLNAME_REGEX = "^[\\p{L}]+(\\s[\\p{L}]+)+$";

    private static final String URL_REGEX = "^(https?)://[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)+([/?].*)?$";

    private static final String EMAIL_GMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@gmail\\.com$";

    private static final String DOMAIN_REGEX = "^(?=.{1,253}$)([a-zA-Z0-9-]{1,63}\\.)+[a-zA-Z]{2,}$";

    private static final InetAddressValidator validator = InetAddressValidator.getInstance();

    public static boolean isValidPhoneNumber(String phoneNumber) {
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        return phoneNumber != null && pattern.matcher(phoneNumber).matches();
    }

    public static boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        return email != null && pattern.matcher(email).matches();
    }

    public static boolean isValidFullname(String name) {
        Pattern pattern = Pattern.compile(FULLNAME_REGEX);
        return name != null && pattern.matcher(name).matches();
    }

    public static boolean isValidCode(String code) {
        return code != null && code.matches(CODE_REGEX);
    }

    public static boolean isValidEmailFE(String email) {
        Pattern pattern = Pattern.compile(EMAIL_FE_REGEX);
        return email != null && pattern.matcher(email).matches();
    }

    public static boolean isValidEmailFPT(String email) {
        Pattern pattern = Pattern.compile(EMAIL_FPT_REGEX);
        return email != null && pattern.matcher(email).matches();
    }

    public static boolean isValidEmailGmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_GMAIL_REGEX);
        return email != null && pattern.matcher(email).matches();
    }

    public static boolean isValidDomain(String domain) {
        Pattern pattern = Pattern.compile(DOMAIN_REGEX);
        return domain != null && pattern.matcher(domain).matches();
    }

    public static boolean isValidIPv4(String ip) {
        return validator.isValidInet4Address(ip);
    }

    public static boolean isValidIPv6(String ip) {
        return validator.isValidInet6Address(ip);
    }

    public static boolean isValidIPv4CIDR(String cidr) {
        try {
            new SubnetUtils(cidr);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidIPv6CIDR(String cidr) {
        return cidr.matches("^([0-9a-fA-F:]+:+)+[0-9a-fA-F]+/\\d{1,3}$");
    }

    public static String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (isLocalhost(ip)) {
            ip = getPublicIP();
        }
        return ip;
    }

    private static boolean isLocalhost(String ip) {
        return ip.equals("127.0.0.1") || ip.equals("::1") || ip.equals("0:0:0:0:0:0:0:1");
    }

    public static String getPublicIP() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api64.ipify.org?format=json"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.body());
            return jsonNode.get("ip").asText();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isAllowedIP(String ip, Set<String> ALLOWED_IP_OR_CIDR) {
        if (!validator.isValid(ip)) {
            return false;
        }

        for (String allowed : ALLOWED_IP_OR_CIDR) {
            if (isValidDnsSuffix(allowed)) {
                List<String> lstDNSSuffix = AppUtils.getDnsSuffixes();
                if (lstDNSSuffix.contains(allowed)) {
                    return true;
                }
            } else {
                if (allowed.contains("/")) {
                    if (allowed.contains(":")) {
                        if (isIPInIPv6Range(ip, allowed)) {
                            return true;
                        }
                    } else {
                        SubnetUtils subnet = new SubnetUtils(allowed);
                        if (subnet.getInfo().isInRange(ip)) {
                            return true;
                        }
                    }
                } else {
                    if (allowed.equals(ip)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isIPInIPv6Range(String ip, String cidr) {
        if (!validator.isValidInet6Address(ip)) {
            return false;
        }
        return ip.startsWith(cidr.split("/")[0]);
    }

    public static boolean isValidDnsSuffix(String suffix) {
        if (suffix == null || suffix.isEmpty()) {
            return false;
        }

        String[] labels = suffix.split("\\.");
        for (String label : labels) {
            if (label.isEmpty() || label.length() > 63) {
                return false;
            }

            if (label.startsWith("-") || label.endsWith("-")) {
                return false;
            }

            if (!label.matches("^[a-zA-Z0-9-]+$")) {
                return false;
            }
        }

        return true;
    }

    public static boolean isValidURL(String url) {
        try {
            URL u = new URL(url);
            u.toURI();
            return Pattern.compile(URL_REGEX).matcher(url).matches();
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }

    public static boolean isValidStudentCode(String code) {
        return code != null && code.length() <= EntityProperties.LENGTH_CODE;
    }

    public static boolean isValidStudentName(String name) {
        return name != null && name.length() <= EntityProperties.LENGTH_NAME;
    }

}
