package udpm.hn.studentattendance.core.authentication.router;


public final class RouteAuthentication {

    public final static String PARAM_LOGIN_SUCCESS = "authencation_token";

    public final static String PARAM_LOGIN_FAILURE = "authencation_error";

    public final static String REDIRECT_LOGIN = "/authorization";

    public final static String REDIRECT_GOOGLE_AUTHORIZATION = "/oauth2/authorization/google";

    public final static String API_GET_ALL_FACILITY = "/get-all-facility";

    public final static String API_GET_INFO_USER = "/get-info-user";

}
