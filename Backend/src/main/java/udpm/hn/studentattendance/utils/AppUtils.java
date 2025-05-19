package udpm.hn.studentattendance.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
        try {
            Process process = Runtime.getRuntime().exec("ipconfig /all");
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

        } catch (Exception e) {
        }
        return suffixes;
    }

}
