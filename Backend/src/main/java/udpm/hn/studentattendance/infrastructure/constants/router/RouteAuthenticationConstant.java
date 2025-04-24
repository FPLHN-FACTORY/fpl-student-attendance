package udpm.hn.studentattendance.infrastructure.constants.router;


public final class RouteAuthenticationConstant {

    public final static String PARAM_ROUTE_ROLE = "role";

    public final static String PARAM_LOGIN_SUCCESS = "authencation_token";

    public final static String PARAM_LOGIN_FAILURE = "authencation_error";

    public final static String REDIRECT_LOGIN = "/authorization";

    public final static String REDIRECT_GOOGLE_AUTHORIZATION = "/oauth2/authorization/google";

    public final static String API_CONVERT_IMAGE_TO_BASE64 = "/get-avatar";

    public final static String API_GET_ALL_FACILITY = "/get-all-facility";

    public final static String API_GET_INFO_USER = "/get-info-user";

    public final static String API_STUDENT_REGISTER = "/student-register";

    public final static String API_STUDENT_UPDATE_FACE_ID = "/student-update-face-id";

    public final static String API_STUDENT_INFO = "/student-info";

}
