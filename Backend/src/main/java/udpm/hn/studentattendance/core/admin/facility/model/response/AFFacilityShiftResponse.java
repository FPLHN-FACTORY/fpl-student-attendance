package udpm.hn.studentattendance.core.admin.facility.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface AFFacilityShiftResponse extends IsIdentify, HasOrderNumber {

    Integer getShift();

    Integer getStatus();

    Long getFromHour();

    Long getFromMinute();

    Long getToHour();

    Long getToMinute();

    String getStartTime();

    String getEndTime();

}
