package udpm.hn.studentattendance.infrastructure.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalRestExceptionHandlerTest {
    @InjectMocks
    private GlobalRestExceptionHandler handler;

    @Test
    void testGlobalRestExceptionHandlerExists() {
        assertNotNull(handler);
    }

    @Test
    void testHandleRestApiException() {
        RestApiException exception = new RestApiException("Test error");

        ResponseEntity<udpm.hn.studentattendance.infrastructure.common.ApiResponse> response = handler
                .handleValidationException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleException() {
        Exception exception = new Exception("Generic error");

        ResponseEntity<udpm.hn.studentattendance.infrastructure.common.ApiResponse> response = handler
                .handleException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleValidationExceptions() {
        org.springframework.validation.BindException bindException = mock(
                org.springframework.validation.BindException.class);
        org.springframework.validation.ObjectError objectError = mock(org.springframework.validation.ObjectError.class);
        org.springframework.validation.BindingResult bindingResult = mock(
                org.springframework.validation.BindingResult.class);

        when(objectError.getDefaultMessage()).thenReturn("Validation error");
        when(bindingResult.getAllErrors()).thenReturn(java.util.List.of(objectError));
        when(bindException.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<udpm.hn.studentattendance.infrastructure.common.ApiResponse> response = handler
                .handleValidationExceptions(bindException);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}