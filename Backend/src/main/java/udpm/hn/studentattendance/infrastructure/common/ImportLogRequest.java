package udpm.hn.studentattendance.infrastructure.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.ImportLogType;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ImportLogRequest {
    private ImportLogType type;
    private String code;
    private String fileName;
    private EntityStatus status;
    private Integer line;
    private String message;
}
