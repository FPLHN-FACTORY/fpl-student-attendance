package udpm.hn.studentattendance.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import udpm.hn.studentattendance.helpers.ValidateHelper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppUtils {

    private static int TIME_LIVE_SIGN = 5;

    public static String imageUrlToBase64(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            String contentType = connection.getContentType();
            InputStream inputStream = connection.getInputStream();
            byte[] imageBytes = inputStream.readAllBytes(); // Java 9+
            inputStream.close();
            return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            return imageUrl;
        }
    }

    public static List<String> getDnsSuffixes() {
        List<String> suffixes = new ArrayList<>();
        String os = System.getProperty("os.name").toLowerCase();
        String cmd = os.contains("win") ? "ipconfig /all" : "ip a";
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.toLowerCase().startsWith("connection-specific dns suffix")
                        || line.toLowerCase().startsWith("primary dns suffix")) {

                    int colonIndex = line.indexOf(":");
                    if (colonIndex != -1 && colonIndex + 1 < line.length()) {
                        String suffix = line.substring(colonIndex + 1).trim();
                        if (!suffix.isEmpty() && !suffixes.contains(suffix)) {
                            suffixes.add(suffix);
                        }
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
        }
        return suffixes;
    }

    public static String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("CF-Connecting-IP");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                ip = ip.split(",")[0].trim();
            }
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ValidateHelper.isLocalhost(ip) || (ip != null && ip.contains(":"))) {
            ip = getPublicIP();
        }

        return ip;
    }

    public static String getPublicIP() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api4.ipify.org?format=json"))
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

    public static boolean isSignatureValidated(String request_signature, String data, String secret_key) {
        if (request_signature == null || request_signature.isEmpty()) {
            return false;
        }

        try {
            Set<String> serverSignature = new HashSet<>();
            for (int i = 0; i < TIME_LIVE_SIGN; i++) {
                long timestamp = DateTimeUtils.getCurrentTimeSecond() - i;
                String toSign = data + "|" + timestamp;
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(secret_key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
                byte[] hash = mac.doFinal(toSign.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (byte b : hash) {
                    sb.append(String.format("%02x", b));
                }
                String signature = sb.toString();
                serverSignature.add(signature);
            }

            return serverSignature.contains(request_signature);
        } catch (Exception e) {
            return false;
        }
    }

}
