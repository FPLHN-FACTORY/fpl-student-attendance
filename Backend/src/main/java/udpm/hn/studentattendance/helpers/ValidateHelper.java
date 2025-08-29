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

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private static final String EMAIL_FPT_REGEX = "^[A-Za-z0-9._+-]+@fpt\\.edu\\.vn$";

    private static final String EMAIL_FE_REGEX = "^[A-Za-z0-9._+-]+@fe\\.edu\\.vn$";

    private static final String PHONE_REGEX = "^0[0-9]{9,10}$";

    private static final String FULLNAME_REGEX = "^[\\p{L}#]+(\\s[\\p{L}#]+)+$";

    private static final String NAME_REGEX = "^[\\p{L}\\d\\s_#-]+$";

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

    public static boolean isValidName(String name) {
        Pattern pattern = Pattern.compile(NAME_REGEX);
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
        if (ip == null) {
            return false;
        }
        return validator.isValidInet6Address(ip);
    }

    public static boolean isValidIPv4CIDR(String cidr) {
        if (cidr == null) {
            return false;
        }
        try {
            new SubnetUtils(cidr);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidIPv6CIDR(String cidr) {
        if (cidr == null) {
            return false;
        }
        // Simple IPv6 CIDR validation - check format and prefix length
        if (!cidr.contains("/")) {
            return false;
        }
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            return false;
        }
        String ip = parts[0];
        String prefixLength = parts[1];

        try {
            int prefix = Integer.parseInt(prefixLength);
            if (prefix < 0 || prefix > 128) {
                return false;
            }
            // Basic IPv6 format check
            return ip.matches(
                    "^([0-9a-fA-F]{1,4}:){0,7}[0-9a-fA-F]{1,4}$|^::$|^([0-9a-fA-F]{1,4}:){1,7}:$|^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$");
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isLocalhost(String ip) {
        if (ip == null) {
            return false;
        }
        return ip.equals("127.0.0.1") || ip.equals("::1") || ip.equals("0:0:0:0:0:0:0:1");
    }

    public static boolean isAllowedIP(String ip, Set<String> ALLOWED_IP_OR_CIDR) {
        if (ip == null || ALLOWED_IP_OR_CIDR == null || ALLOWED_IP_OR_CIDR.isEmpty()) {
            return false;
        }

        if (!validator.isValid(ip)) {
            return false;
        }

        for (String allowed : ALLOWED_IP_OR_CIDR) {
            if (allowed == null) {
                continue;
            }

            // First check if it's an exact IP match
            if (allowed.equals(ip)) {
                return true;
            }

            // Then check if it's a CIDR range
            if (allowed.contains("/")) {
                if (allowed.contains(":")) {
                    // IPv6 CIDR
                    if (isIPInIPv6Range(ip, allowed)) {
                        return true;
                    }
                } else {
                    // IPv4 CIDR
                    try {
                        SubnetUtils subnet = new SubnetUtils(allowed);
                        if (subnet.getInfo().isInRange(ip)) {
                            return true;
                        }
                    } catch (IllegalArgumentException e) {
                        // Invalid CIDR format, skip this entry
                        continue;
                    }
                }
            }

            // Finally check if it's a DNS suffix (this should be rare for IP validation)
            if (isValidDnsSuffix(allowed)) {
                List<String> lstDNSSuffix = AppUtils.getDnsSuffixes();
                if (lstDNSSuffix != null && lstDNSSuffix.contains(allowed)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isIPInIPv6Range(String ip, String cidr) {
        if (ip == null || cidr == null) {
            return false;
        }
        if (!validator.isValidInet6Address(ip)) {
            return false;
        }
        if (!cidr.contains("/")) {
            return false;
        }
        try {
            String networkPart = cidr.split("/")[0];
            return ip.startsWith(networkPart);
        } catch (Exception e) {
            return false;
        }
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

}
