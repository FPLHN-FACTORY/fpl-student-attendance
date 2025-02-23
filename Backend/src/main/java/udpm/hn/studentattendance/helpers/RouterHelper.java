package udpm.hn.studentattendance.helpers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;

public class RouterHelper {

    public static String appendWildcard(String url) {
        return url + "/**";
    }

    public static String appendApiWildcard(String url) {
        return appendWildcard(RoutesConstant.API_PREFIX);
    }

    public static String appendPrefixApi(String url) {
        return RoutesConstant.API_PREFIX + url;
    }

    public static String appendPrefixApi(String url, String wildCard) {
        return RoutesConstant.API_PREFIX + url + wildCard;
    }

    public static ResponseEntity<ApiResponse> createResponseApi(ApiResponse response, HttpStatus status) {
        return new ResponseEntity<>(response, status);
    }

}
