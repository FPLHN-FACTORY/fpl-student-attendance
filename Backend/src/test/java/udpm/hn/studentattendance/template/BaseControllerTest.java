package udpm.hn.studentattendance.template;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * A base test class that provides common assertion methods for controller
 * tests.
 * Extend this class when creating controller test classes.
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseControllerTest {

    /**
     * Verifies that a response is successful and contains the expected message
     * 
     * @param response        The response to check
     * @param expectedMessage The expected message
     */
    protected void assertSuccessResponse(ResponseEntity<?> response, String expectedMessage) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(expectedMessage, apiResponse.getMessage());
    }

    /**
     * Verifies that a response is successful, contains the expected message, and
     * returns the expected data
     * 
     * @param response        The response to check
     * @param expectedMessage The expected message
     * @param expectedData    The expected data
     */
    protected void assertSuccessResponse(ResponseEntity<?> response, String expectedMessage, Object expectedData) {
        assertSuccessResponse(response, expectedMessage);
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(expectedData, apiResponse.getData());
    }

    /**
     * Verifies that a response is an error and contains the expected message
     * 
     * @param response        The response to check
     * @param expectedMessage The expected error message
     */
    protected void assertErrorResponse(ResponseEntity<?> response, String expectedMessage) {
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(expectedMessage, apiResponse.getMessage());
    }

    /**
     * Creates a mock ResponseEntity with success status
     * 
     * @param message Success message
     * @param data    Response data
     * @return Mocked response entity
     */
    @SuppressWarnings("rawtypes")
    protected ResponseEntity createSuccessResponseEntity(String message, Object data) {
        ApiResponse apiResponse = ApiResponse.success(message, data);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Creates a mock ResponseEntity with error status
     * 
     * @param message Error message
     * @return Mocked response entity
     */
    @SuppressWarnings("rawtypes")
    protected ResponseEntity createErrorResponseEntity(String message) {
        ApiResponse apiResponse = ApiResponse.error(message);
        return ResponseEntity.badRequest().body(apiResponse);
    }
}
