package udpm.hn.studentattendance.core.teacher.factory.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCPlanDateStudentFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCStudentFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCPlanDateStudentFactoryResponse;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCStudentFactoryResponse;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCStudentFactoryExtendRepository;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TCStudentFactoryServiceImplTest {

    @Mock
    private TCStudentFactoryExtendRepository studentFactoryRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @Mock
    private SessionHelper sessionHelper;

    @InjectMocks
    private TCStudentFactoryServiceImpl studentFactoryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(studentFactoryService, "redisTTL", 3600L);
    }

    @Test
    @DisplayName("getAllStudentFactory should return paginated student list")
    void testGetAllStudentFactory() {
        // Arrange
        TCStudentFactoryRequest request = new TCStudentFactoryRequest();
        request.setFactoryId("factory-1");

        Page<TCStudentFactoryResponse> page = new PageImpl<>(new ArrayList<>());
        when(studentFactoryRepository.getUserStudentInFactory(any(Pageable.class), eq("factory-1"), eq(request)))
                .thenReturn(page);

        // Act
        ResponseEntity<?> response = studentFactoryService.getAllStudentFactory(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy tất cả học sinh trong nhóm xưởng thành công", apiResponse.getMessage());

        verify(studentFactoryRepository).getUserStudentInFactory(any(Pageable.class), eq("factory-1"), eq(request));
    }

    @Test
    @DisplayName("getDetailAttendance should return attendance details for a student")
    void testGetDetailAttendance() {
        // Arrange
        TCPlanDateStudentFactoryRequest request = new TCPlanDateStudentFactoryRequest();
        request.setIdUserStudent("student-1");
        request.setIdFactory("factory-1");

        List<TCPlanDateStudentFactoryResponse> mockAttendanceList = Arrays.asList(
                mock(TCPlanDateStudentFactoryResponse.class),
                mock(TCPlanDateStudentFactoryResponse.class));

        when(studentFactoryRepository.getAllPlanDateAttendanceByIdStudent(request)).thenReturn(mockAttendanceList);

        // Act
        ResponseEntity<?> response = studentFactoryService.getDetailAttendance(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());
        assertEquals(mockAttendanceList, apiResponse.getData());

        verify(studentFactoryRepository).getAllPlanDateAttendanceByIdStudent(request);
    }

    @Test
    @DisplayName("getAllAttendance should return all attendance records for a factory")
    void testGetAllAttendance() {
        // Arrange
        String factoryId = "factory-1";

        List<TCPlanDateStudentFactoryResponse> mockAttendanceList = Arrays.asList(
                mock(TCPlanDateStudentFactoryResponse.class),
                mock(TCPlanDateStudentFactoryResponse.class));

        when(studentFactoryRepository.getAllPlanDateAttendanceByIdFactory(factoryId)).thenReturn(mockAttendanceList);

        // Act
        ResponseEntity<?> response = studentFactoryService.getAllAttendance(factoryId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());
        assertEquals(mockAttendanceList, apiResponse.getData());

        verify(studentFactoryRepository).getAllPlanDateAttendanceByIdFactory(factoryId);
    }

    @Test
    @DisplayName("deleteStudentFactoryById should delete student when found")
    void testDeleteStudentFactoryById_Success() {
        // Arrange
        String studentFactoryId = "student-factory-1";

        UserStudentFactory userStudentFactory = mock(UserStudentFactory.class);
        UserStudent student = mock(UserStudent.class);
        when(student.getCode()).thenReturn("SV001");
        when(userStudentFactory.getUserStudent()).thenReturn(student);

        when(studentFactoryRepository.findById(studentFactoryId)).thenReturn(Optional.of(userStudentFactory));
        doNothing().when(studentFactoryRepository).deleteById(studentFactoryId);

        // Act
        ResponseEntity<?> response = studentFactoryService.deleteStudentFactoryById(studentFactoryId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Xoá sinh viên có mã SV001 thành công", apiResponse.getMessage());

        verify(studentFactoryRepository).findById(studentFactoryId);
        verify(studentFactoryRepository).deleteById(studentFactoryId);
    }

    @Test
    @DisplayName("deleteStudentFactoryById should return error when student not found")
    void testDeleteStudentFactoryById_NotFound() {
        // Arrange
        String studentFactoryId = "non-existent-id";

        when(studentFactoryRepository.findById(studentFactoryId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = studentFactoryService.deleteStudentFactoryById(studentFactoryId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Sinh viên không tồn tại", apiResponse.getMessage());

        verify(studentFactoryRepository).findById(studentFactoryId);
        verify(studentFactoryRepository, never()).deleteById(anyString());
    }
}