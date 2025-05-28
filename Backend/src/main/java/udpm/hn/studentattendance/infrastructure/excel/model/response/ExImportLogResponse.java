package udpm.hn.studentattendance.infrastructure.excel.model.response;

import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface ExImportLogResponse extends IsIdentify {

    String getFileName();

    Integer getTotalSuccess();

    Integer getTotalError();

    Long getCreatedAt();

}
