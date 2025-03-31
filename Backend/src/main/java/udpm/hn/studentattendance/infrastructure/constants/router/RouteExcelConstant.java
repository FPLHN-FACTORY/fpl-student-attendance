package udpm.hn.studentattendance.infrastructure.constants.router;

import static udpm.hn.studentattendance.infrastructure.constants.RoutesConstant.PREFIX_API_EXCEL;

public final class RouteExcelConstant {

    public static final String DEFAULT_UPLOAD = "/upload";

    public static final String DEFAULT_IMPORT = "/import";

    public static final String DEFAULT_DOWNLOAD_TEMPLATE = "/download-template";

    public static final String DEFAULT_HISTORY_LOG = "/history-log";

    public static final String URL_API_PLAN_DATE = PREFIX_API_EXCEL + "/plan-date";

    public static final String URL_API_STAFF = PREFIX_API_EXCEL + "/staff";

    public static final String URL_API_FACTORY = PREFIX_API_EXCEL + "/factory";

    public static final String URL_API_STUDENT = PREFIX_API_EXCEL + "/student";

    public static final String URL_API_STUDENT_FACTORY = PREFIX_API_EXCEL + "/student-factory";
}
