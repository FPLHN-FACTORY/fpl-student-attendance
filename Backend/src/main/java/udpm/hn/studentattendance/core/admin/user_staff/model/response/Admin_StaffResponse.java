package udpm.hn.studentattendance.core.admin.user_staff.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface Admin_StaffResponse extends IsIdentify, HasOrderNumber {
    Integer getRowNumber();

    String getStaffName();

    String getStaffCode();

    String getStaffEmailFe();

    String getStaffEmailFpt();

    String getFacilityName();

    Integer getStaffStatus();

    String getRoleCode();
}
