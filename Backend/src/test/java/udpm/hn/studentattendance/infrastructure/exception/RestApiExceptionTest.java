package udpm.hn.studentattendance.infrastructure.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RestApiExceptionTest {
    @Test
    void testRestApiExceptionExists() {
        RestApiException exception = new RestApiException("Test message");
        assertNotNull(exception);
    }

    @Test
    void testMessageConstructor() {
        String message = "Error occurred";
        RestApiException exception = new RestApiException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testMessageAndErrorsConstructor() {
        String message = "Error occurred";
        Object cause = new Object();
        RestApiException exception = new RestApiException(message, cause);
        assertEquals(message, exception.getMessage());
        // No getErrors() method to test
    }

    @Test
    void testGetMessage() {
        String message = "Test";
        RestApiException exception = new RestApiException(message);
        assertEquals(message, exception.getMessage());
    }
}
