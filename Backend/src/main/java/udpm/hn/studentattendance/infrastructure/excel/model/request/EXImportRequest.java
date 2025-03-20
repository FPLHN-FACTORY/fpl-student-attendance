package udpm.hn.studentattendance.infrastructure.excel.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

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

}
