package udpm.hn.studentattendance.infrastructure.common;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

@Setter
@Getter
public class ApiResponse {

    private RestApiStatus status;

    private String message;

    private Object data;

    private ApiResponse() { }

    public <T> ApiResponse(T o) {
        this.status = RestApiStatus.SUCCESS;
        this.data = o;
    }

    public <T> ApiResponse(String message, T o) {
        this.status = RestApiStatus.SUCCESS;
        this.message = message;
        this.data = o;
    }

    public <T> ApiResponse(RestApiStatus status, String message, T o) {
        this.status = status;
        this.message = message;
        this.data = o;
    }

    public static ApiResponse success(String message) {
        return new ApiResponse(message, null);
    }

    public static <T> ApiResponse success(String message, T o) {
        return new ApiResponse(message, o);
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(RestApiStatus.ERROR, message, null);
    }

    public static <T> ApiResponse error(String message, T o) {
        return new ApiResponse(RestApiStatus.ERROR, message, o);
    }

    public static ApiResponse warning(String message) {
        return new ApiResponse(RestApiStatus.WARNING, message, null);
    }

    public static <T> ApiResponse warning(String message, T o) {
        return new ApiResponse(RestApiStatus.WARNING, message, o);
    }

    public static ApiResponse pending(String message) {
        return new ApiResponse(RestApiStatus.PENDING, message, null);
    }

    public static <T> ApiResponse pending(String message, T o) {
        return new ApiResponse(RestApiStatus.PENDING, message, o);
    }

}
