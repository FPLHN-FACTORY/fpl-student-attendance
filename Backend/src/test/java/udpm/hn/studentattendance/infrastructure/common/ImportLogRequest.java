package udpm.hn.studentattendance.infrastructure.common;

import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.ImportLogType;

public class ImportLogRequest {
    private ImportLogType type;
    private String code;
    private String fileName;
    private EntityStatus status;
    private Integer line;
    private String message;

    public ImportLogRequest() {
    }

    public ImportLogRequest(ImportLogType type, String code, String fileName, EntityStatus status, Integer line, String message) {
        this.type = type;
        this.code = code;
        this.fileName = fileName;
        this.status = status;
        this.line = line;
        this.message = message;
    }

    public ImportLogType getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    public String getFileName() {
        return fileName;
    }

    public EntityStatus getStatus() {
        return status;
    }

    public Integer getLine() {
        return line;
    }

    public String getMessage() {
        return message;
    }

    public void setType(ImportLogType type) {
        this.type = type;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setStatus(EntityStatus status) {
        this.status = status;
    }

    public void setLine(Integer line) {
        this.line = line;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
