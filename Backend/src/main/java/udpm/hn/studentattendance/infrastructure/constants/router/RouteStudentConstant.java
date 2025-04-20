package udpm.hn.studentattendance.infrastructure.constants.router;

import static udpm.hn.studentattendance.infrastructure.constants.RoutesConstant.PREFIX_API_STUDENT_MANAGEMENT;

public final class RouteStudentConstant {

    public static final String URL_API_STUDENT_ATTENDANCE_HISTORY_MANAGEMENT = PREFIX_API_STUDENT_MANAGEMENT + "/attendance-history";
    public static final String URL_API_STUDENT_ATTENDANCE_MANAGEMENT = PREFIX_API_STUDENT_MANAGEMENT + "/attendance";
    public static final String URL_API_STUDENT_ATTENDANCE_SCHEDULE_MANAGEMENT = PREFIX_API_STUDENT_MANAGEMENT + "/plan-attendance";

    public static final String URL_API_ATTENDANCE = PREFIX_API_STUDENT_MANAGEMENT + "/attendance";

}
