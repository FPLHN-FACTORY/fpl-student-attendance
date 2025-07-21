package udpm.hn.studentattendance.helpers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class RouterHelperTest {
    @Test
    void canInstantiate() {
        RouterHelper helper = new RouterHelper();
        assertThat(helper).isNotNull();
    }

    @Test
    void testAppendWildcard() {
        // Given
        String url = "/api/test";

        // When
        String result = RouterHelper.appendWildcard(url);

        // Then
        assertEquals("/api/test/**", result);
    }

    @Test
    void testAppendWildcardWithEmptyString() {
        // Given
        String url = "";

        // When
        String result = RouterHelper.appendWildcard(url);

        // Then
        assertEquals("/**", result);
    }

    @Test
    void testAppendWildcardWithNull() {
        // Given
        String url = null;

        // When
        String result = RouterHelper.appendWildcard(url);

        // Then
        assertEquals("null/**", result);
    }

    @Test
    void testAppendApiWildcard() {
        // When
        String result = RouterHelper.appendApiWildcard("");

        // Then
        assertEquals(RoutesConstant.API_PREFIX + "/**", result);
    }

    @Test
    void testAppendPrefixApi() {
        // Given
        String url = "/test";

        // When
        String result = RouterHelper.appendPrefixApi(url);

        // Then
        assertEquals(RoutesConstant.API_PREFIX + "/test", result);
    }

    @Test
    void testAppendPrefixApiWithEmptyString() {
        // Given
        String url = "";

        // When
        String result = RouterHelper.appendPrefixApi(url);

        // Then
        assertEquals(RoutesConstant.API_PREFIX + "", result);
    }

    @Test
    void testAppendPrefixApiWithNull() {
        // Given
        String url = null;

        // When
        String result = RouterHelper.appendPrefixApi(url);

        // Then
        assertEquals(RoutesConstant.API_PREFIX + "null", result);
    }

    @Test
    void testAppendPrefixApiWithUrlAndWildcard() {
        // Given
        String url = "/test";
        String wildCard = "/**";

        // When
        String result = RouterHelper.appendPrefixApi(url, wildCard);

        // Then
        assertEquals(RoutesConstant.API_PREFIX + "/test/**", result);
    }

    @Test
    void testAppendPrefixApiWithEmptyUrlAndWildcard() {
        // Given
        String url = "";
        String wildCard = "/**";

        // When
        String result = RouterHelper.appendPrefixApi(url, wildCard);

        // Then
        assertEquals(RoutesConstant.API_PREFIX + "/**", result);
    }

    @Test
    void testCreateResponseApi() {
        // Given
        ApiResponse response = ApiResponse.success("Test message", "test data");
        HttpStatus status = HttpStatus.OK;

        // When
        ResponseEntity<ApiResponse> result = RouterHelper.createResponseApi(response, status);

        // Then
        assertNotNull(result);
        assertEquals(status, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void testCreateResponseApiWithErrorStatus() {
        // Given
        ApiResponse response = ApiResponse.error("Error message", "error data");
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // When
        ResponseEntity<ApiResponse> result = RouterHelper.createResponseApi(response, status);

        // Then
        assertNotNull(result);
        assertEquals(status, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void testResponseSuccessWithMessageOnly() {
        // Given
        String message = "Success message";

        // When
        ResponseEntity<ApiResponse> result = RouterHelper.responseSuccess(message);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(message, result.getBody().getMessage());
        assertEquals(RestApiStatus.SUCCESS, result.getBody().getStatus());
        assertNull(result.getBody().getData());
    }

    @Test
    void testResponseSuccessWithMessageAndData() {
        // Given
        String message = "Success message";
        Object data = "test data";

        // When
        ResponseEntity<ApiResponse> result = RouterHelper.responseSuccess(message, data);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(message, result.getBody().getMessage());
        assertEquals(RestApiStatus.SUCCESS, result.getBody().getStatus());
        assertEquals(data, result.getBody().getData());
    }

    @Test
    void testResponseSuccessWithNullData() {
        // Given
        String message = "Success message";

        // When
        ResponseEntity<ApiResponse> result = RouterHelper.responseSuccess(message, null);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(message, result.getBody().getMessage());
        assertEquals(RestApiStatus.SUCCESS, result.getBody().getStatus());
        assertNull(result.getBody().getData());
    }

    @Test
    void testResponseErrorWithMessageOnly() {
        // Given
        String message = "Error message";

        // When
        ResponseEntity<ApiResponse> result = RouterHelper.responseError(message);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(message, result.getBody().getMessage());
        assertEquals(RestApiStatus.ERROR, result.getBody().getStatus());
        assertNull(result.getBody().getData());
    }

    @Test
    void testResponseErrorWithMessageAndData() {
        // Given
        String message = "Error message";
        Object data = "error data";

        // When
        ResponseEntity<ApiResponse> result = RouterHelper.responseError(message, data);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(message, result.getBody().getMessage());
        assertEquals(RestApiStatus.ERROR, result.getBody().getStatus());
        assertEquals(data, result.getBody().getData());
    }

    @Test
    void testResponseErrorWithNullData() {
        // Given
        String message = "Error message";

        // When
        ResponseEntity<ApiResponse> result = RouterHelper.responseError(message, null);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(message, result.getBody().getMessage());
        assertEquals(RestApiStatus.ERROR, result.getBody().getStatus());
        assertNull(result.getBody().getData());
    }

    @Test
    void testGetRequiredRoleForUrlWithAdminUrl() {
        // Given
        String url = RoutesConstant.PREFIX_API_ADMIN_MANAGEMENT + "/test";

        // When
        RoleConstant result = RouterHelper.getRequiredRoleForUrl(url);

        // Then
        assertEquals(RoleConstant.ADMIN, result);
    }

    @Test
    void testGetRequiredRoleForUrlWithStaffUrl() {
        // Given
        String url = RoutesConstant.PREFIX_API_STAFF_MANAGEMENT + "/test";

        // When
        RoleConstant result = RouterHelper.getRequiredRoleForUrl(url);

        // Then
        assertEquals(RoleConstant.STAFF, result);
    }

    @Test
    void testGetRequiredRoleForUrlWithTeacherUrl() {
        // Given
        String url = RoutesConstant.PREFIX_API_TEACHER_MANAGEMENT + "/test";

        // When
        RoleConstant result = RouterHelper.getRequiredRoleForUrl(url);

        // Then
        assertEquals(RoleConstant.TEACHER, result);
    }

    @Test
    void testGetRequiredRoleForUrlWithStudentUrl() {
        // Given
        String url = RoutesConstant.PREFIX_API_STUDENT_MANAGEMENT + "/test";

        // When
        RoleConstant result = RouterHelper.getRequiredRoleForUrl(url);

        // Then
        assertEquals(RoleConstant.STUDENT, result);
    }

    @Test
    void testGetRequiredRoleForUrlWithUnknownUrl() {
        // Given
        String url = "/unknown/path";

        // When
        RoleConstant result = RouterHelper.getRequiredRoleForUrl(url);

        // Then
        assertNull(result);
    }

    @Test
    void testGetRequiredRoleForUrlWithEmptyString() {
        // Given
        String url = "";

        // When
        RoleConstant result = RouterHelper.getRequiredRoleForUrl(url);

        // Then
        assertNull(result);
    }

    @Test
    void testGetRequiredRoleForUrlWithNull() {
        // Given
        String url = null;

        // When
        RoleConstant result = RouterHelper.getRequiredRoleForUrl(url);

        // Then
        assertNull(result);
    }

    @Test
    void testGetRequiredRoleForUrlWithExactAdminPrefix() {
        // Given
        String url = RoutesConstant.PREFIX_API_ADMIN_MANAGEMENT;

        // When
        RoleConstant result = RouterHelper.getRequiredRoleForUrl(url);

        // Then
        assertEquals(RoleConstant.ADMIN, result);
    }

    @Test
    void testGetRequiredRoleForUrlWithExactStaffPrefix() {
        // Given
        String url = RoutesConstant.PREFIX_API_STAFF_MANAGEMENT;

        // When
        RoleConstant result = RouterHelper.getRequiredRoleForUrl(url);

        // Then
        assertEquals(RoleConstant.STAFF, result);
    }

    @Test
    void testGetRequiredRoleForUrlWithExactTeacherPrefix() {
        // Given
        String url = RoutesConstant.PREFIX_API_TEACHER_MANAGEMENT;

        // When
        RoleConstant result = RouterHelper.getRequiredRoleForUrl(url);

        // Then
        assertEquals(RoleConstant.TEACHER, result);
    }

    @Test
    void testGetRequiredRoleForUrlWithExactStudentPrefix() {
        // Given
        String url = RoutesConstant.PREFIX_API_STUDENT_MANAGEMENT;

        // When
        RoleConstant result = RouterHelper.getRequiredRoleForUrl(url);

        // Then
        assertEquals(RoleConstant.STUDENT, result);
    }
}
