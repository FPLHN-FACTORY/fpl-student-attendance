package udpm.hn.studentattendance.core.admin.useradmin.model.response;

public interface ADUserAdminResponse {
    Integer getRowNumber();

    String getUserAdminId();

    String getUserAdminCode();

    String getUserAdminName();

    String getUserAdminEmail();

    Integer getUserAdminStatus();
}
