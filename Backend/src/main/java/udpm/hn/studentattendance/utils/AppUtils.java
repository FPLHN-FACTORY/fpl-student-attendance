package udpm.hn.studentattendance.utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

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

}
