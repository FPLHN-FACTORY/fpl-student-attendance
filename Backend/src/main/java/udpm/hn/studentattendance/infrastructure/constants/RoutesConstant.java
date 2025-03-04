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

    public static final String URL_ADMIN_MANAGEMENT =  API_PREFIX + "/admin";

    public static final String URL_ADMIN_FACILITY_MANAGEMENT =  URL_ADMIN_MANAGEMENT + "/facilities";

    public static final String URL_ADMIN_SEMESTER_MANAGEMENT =  URL_ADMIN_MANAGEMENT + "/semesters";

    public static final String URL_ADMIN_STAFF_MANAGEMENT =  URL_ADMIN_MANAGEMENT + "/staffs";

    public static final String URL_ADMIN_STAFF_ROLE_MANAGEMENT =  URL_ADMIN_STAFF_MANAGEMENT + "/roles";
}
