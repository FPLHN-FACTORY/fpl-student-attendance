package udpm.hn.studentattendance.infrastructure.excel.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EXImportRequest {

    private String code;

    private String fileName;

    private int line;

    private Map<String, Object> data;

    private Map<String, String> item;

    public Map<String, String> getItem() {
        if (item == null) return null;

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : item.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null) {
                value = value.replaceAll("[^\\p{L}\\p{N}\\s~`!@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?\\\\]", "").replaceAll("\\s+", " ").trim();
            }
            result.put(key, value);
        }
        return result;
    }
}
