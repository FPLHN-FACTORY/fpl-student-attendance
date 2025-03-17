package udpm.hn.studentattendance.infrastructure.excel.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EXImportRequest {

    private Map<String, Object> meta;

    private Map<String, Object> data;

}
