package udpm.hn.studentattendance.utils;


import java.text.Normalizer;
import java.util.UUID;

public class CodeGeneratorUtils {

    private final static int LENGTH_DEFAULT = 6;

    public static String generateRandom() {
        return generateRandom(LENGTH_DEFAULT);
    }

    public static String generateRandom(int length) {
        return UUID.randomUUID().toString().substring(0, length).toUpperCase();
    }

    public static String generateCodeFromString(String name) {
        name = name.trim().toUpperCase();
        String normalizedString = Normalizer.normalize(name, Normalizer.Form.NFD);
        name = normalizedString.replaceAll("\\p{M}", "");
        name = name.replace("Đ", "D");
        name = name.replaceAll("[^a-zA-Z0-9\\s]", "");
        name = name.replaceAll("\\s+", "_");
        return name;
    }

}
