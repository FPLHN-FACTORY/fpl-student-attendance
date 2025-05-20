package udpm.hn.studentattendance.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import udpm.hn.studentattendance.helpers.ValidateHelper;

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
import java.util.List;

public class AppUtils {

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
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }

        if (ValidateHelper.isLocalhost(ip)) {
            ip = getPublicIP();
        }

        if (ip != null && ip.contains(":")) {
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

}
