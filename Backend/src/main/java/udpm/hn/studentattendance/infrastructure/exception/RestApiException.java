package udpm.hn.studentattendance.infrastructure.exception;

import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

@Setter
public class RestApiException extends RuntimeException {

    private RestApiStatus status;

    private String message;

    private Object errors;

    private RestApiException() {
        this.status = RestApiStatus.ERROR;
    }

    public RestApiException(String message) {
        this();
        this.message = message;
    }

    public <T> RestApiException(String message, T errors) {
        this();
        this.message = message;
        this.errors = errors;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
