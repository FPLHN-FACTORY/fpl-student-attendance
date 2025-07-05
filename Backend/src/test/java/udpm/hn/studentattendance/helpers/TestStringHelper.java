package udpm.hn.studentattendance.helpers;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for test string translations and encoding.
 * This class is used to provide English equivalents for Vietnamese strings
 * used in assertions, to avoid encoding issues.
 */
public class TestStringHelper {

    private static final Map<String, String> TRANSLATIONS = new HashMap<>();

    static {
        // Level Project translations
        TRANSLATIONS.put("Lấy danh sách cấp độ dự án thành công", "Get level project list successfully");
        TRANSLATIONS.put("Lấy danh sách cấp độ dự án thành công (cached)",
                "Get level project list successfully (cached)");
        TRANSLATIONS.put("Thêm mới cấp độ dự án thành công", "Add new level project successfully");
        TRANSLATIONS.put("vừa thêm cấp độ dự án", "just added level project");
        TRANSLATIONS.put("Cấp độ dự án đã tồn tại trên hệ thống", "Level project already exists in the system");
        TRANSLATIONS.put("Cập nhật cấp độ dự án thành công", "Update level project successfully");
        TRANSLATIONS.put("vừa cập nhật cấp độ dự án", "just updated level project");
        TRANSLATIONS.put("Không tìm thấy cấp độ dự án muốn chỉnh sửa", "Level project not found for editing");
        TRANSLATIONS.put("Lấy thông tin cấp độ dự án thành công", "Get level project details successfully");
        TRANSLATIONS.put("Lấy thông tin cấp độ dự án thành công (cached)",
                "Get level project details successfully (cached)");
        TRANSLATIONS.put("Chuyển trạng thái cấp độ dự án thành công", "Change level project status successfully");
        TRANSLATIONS.put("vừa thay đổi trạng thái cấp độ dự án", "just changed level project status");
        TRANSLATIONS.put("Không tìm thấy cấp độ dự án muốn thay đổi trạng thái",
                "Level project not found for status change");

        // Facility translations
        TRANSLATIONS.put("Lấy danh sách dữ liệu thành công", "Get data list successfully");
        TRANSLATIONS.put("Tạo mới địa điểm thành công", "Created new location successfully");
        TRANSLATIONS.put("vừa thêm địa điểm mới", "just added new location");
    }

    /**
     * Get the English equivalent for a Vietnamese string.
     * 
     * @param vietnameseText The Vietnamese text to translate
     * @return The English translation, or the original text if no translation
     *         exists
     */
    public static String getEnglishEquivalent(String vietnameseText) {
        return TRANSLATIONS.getOrDefault(vietnameseText, vietnameseText);
    }

    /**
     * Check if a test assertion should use the English translation.
     * 
     * @param assertion The assertion text to check
     * @return true if the assertion should use the English translation
     */
    public static boolean shouldUseEnglish(String assertion) {
        return TRANSLATIONS.containsKey(assertion);
    }
}