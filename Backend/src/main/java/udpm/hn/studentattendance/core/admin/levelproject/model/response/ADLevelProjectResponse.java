package udpm.hn.studentattendance.core.admin.levelproject.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface ADLevelProjectResponse extends IsIdentify, HasOrderNumber {

    String getName();

    String getDescription();

    String getCode();

    Integer getStatus();

}
