package udpm.hn.studentattendance.infrastructure.common.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasAudit;
import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface UALResponse extends IsIdentify, HasOrderNumber, HasAudit {
}
