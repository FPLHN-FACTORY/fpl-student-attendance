package udpm.hn.studentattendance.infrastructure.constants;

public class RoutesConstant {

    public final static String API_PREFIX = "/api/v1";

    /**
     * Người phụ trach
     * **/
    public static final String URL_API_STAFF_MANAGEMENT = API_PREFIX + "/staff-management";
    //API quản lý dự án
    public static final String URL_API_STAFF_PROJECT_MANAGEMENT = URL_API_STAFF_MANAGEMENT + "/project-management";
    //API quản lý cấp dự án
    public static final String URL_API_STAFF_LEVEL_PROJECT_MANAGEMENT = URL_API_STAFF_MANAGEMENT + "/level-project-management";

}
