package udpm.hn.studentattendance.core.staff.student.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.core.staff.student.model.request.USStudentCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.student.model.request.USStudentRequest;
import udpm.hn.studentattendance.core.staff.student.repository.USStudentExtendRepository;
import udpm.hn.studentattendance.core.staff.student.repository.USStudentFacilityExtendRepository;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;
import udpm.hn.studentattendance.core.staff.student.model.response.USStudentResponse;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class STStudentServiceImplTest {

    @Mock
    private USStudentExtendRepository studentExtendRepository;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private USStudentFacilityExtendRepository facilityRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserActivityLogHelper userActivityLogHelper;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private STStudentServiceImpl studentService;

    @Test
    @DisplayName("getAllStudentByFacility should return cached data when available")
    void testGetAllStudentByFacility_CachedData() {
        // Arrange
        USStudentRequest request = new USStudentRequest();
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        PageableObject cachedData = PageableObject.of(Page.empty());
        when(redisService.get(anyString())).thenReturn(cachedData);

        // Act
        ResponseEntity<?> response = studentService.getAllStudentByFacility(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy danh sách sinh viên thành công (cached)", apiResponse.getMessage());
        assertEquals(cachedData, apiResponse.getData());

        verify(redisService).get(anyString());
        verify(studentExtendRepository, never()).getAllStudentByFacility(any(), any(), any());
    }

    @Test
    @DisplayName("getAllStudentByFacility should query database when cache miss")
    void testGetAllStudentByFacility_NoCachedData() {
        // Arrange
        USStudentRequest request = new USStudentRequest();
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(redisService.get(anyString())).thenReturn(null);

        Page<USStudentResponse> dbPage = new PageImpl<>(new ArrayList<>());
        when(studentExtendRepository.getAllStudentByFacility(any(Pageable.class), any(), eq(facilityId)))
                .thenReturn(dbPage);

        ReflectionTestUtils.setField(studentService, "redisTTL", 3600L);

        // Act
        ResponseEntity<?> response = studentService.getAllStudentByFacility(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy danh sách sinh viên thành công", apiResponse.getMessage());

        verify(redisService).get(anyString());
        verify(redisService).set(anyString(), any(), eq(3600L));
        verify(studentExtendRepository).getAllStudentByFacility(any(Pageable.class), any(), eq(facilityId));
    }

    @Test
    @DisplayName("getDetailStudent should return student detail from cache when available")
    void testGetDetailStudent_CachedData() {
        // Arrange
        String studentId = "student-1";
        UserStudent cachedStudent = new UserStudent();
        cachedStudent.setId(studentId);
        when(redisService.get("student:detail:" + studentId)).thenReturn(cachedStudent);

        // Act
        ResponseEntity<?> response = studentService.getDetailStudent(studentId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Hiện thị chi tiết sinh viên thành công (cached)", apiResponse.getMessage());

        verify(redisService).get("student:detail:" + studentId);
        verify(studentExtendRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getDetailStudent should return error when student not found")
    void testGetDetailStudent_NotFound() {
        // Arrange
        String studentId = "nonexistent";
        when(redisService.get(anyString())).thenReturn(null);
        when(studentExtendRepository.findById(studentId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = studentService.getDetailStudent(studentId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Sinh viên không tồn tại", apiResponse.getMessage());
    }

    @Test
    @DisplayName("createStudent should create new student successfully")
    void testCreateStudent_Success() {
        // Arrange
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen Van A");
        request.setEmail("nguyenvana@fe.edu.vn");

        String facilityId = "facility-1";
        Facility facility = new Facility();
        facility.setId(facilityId);

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(studentExtendRepository.getUserStudentByCode(request.getCode())).thenReturn(Optional.empty());
        when(studentExtendRepository.getUserStudentByEmail(request.getEmail())).thenReturn(Optional.empty());

        UserStudent savedStudent = new UserStudent();
        savedStudent.setId("new-student-id");
        savedStudent.setCode(request.getCode());
        savedStudent.setName(request.getName());
        savedStudent.setEmail(request.getEmail());
        when(studentExtendRepository.save(any(UserStudent.class))).thenReturn(savedStudent);

        ReflectionTestUtils.setField(studentService, "isDisableCheckEmailFpt", "false");

        // Act
        ResponseEntity<?> response = studentService.createStudent(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Thêm sinh viên mới thành công", apiResponse.getMessage());

        verify(studentExtendRepository).save(any(UserStudent.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thêm 1 sinh viên mới"));
        verify(redisService).deletePattern(anyString());
    }

    @Test
    @DisplayName("createStudent should return error for invalid code")
    void testCreateStudent_InvalidCode() {
        // Arrange
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST 001"); // Invalid code with space
        request.setName("Nguyen Van A");
        request.setEmail("nguyenvana@fe.edu.vn");

        // Act
        ResponseEntity<?> response = studentService.createStudent(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertTrue(apiResponse.getMessage().contains("Mã sinh viên không hợp lệ"));

        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("createStudent should return error for duplicate code")
    void testCreateStudent_DuplicateCode() {
        // Arrange
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen Van A");
        request.setEmail("nguyenvana@fe.edu.vn");

        when(studentExtendRepository.getUserStudentByCode(request.getCode()))
                .thenReturn(Optional.of(new UserStudent()));

        ReflectionTestUtils.setField(studentService, "isDisableCheckEmailFpt", "false");

        // Act
        ResponseEntity<?> response = studentService.createStudent(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Mã sinh viên đã tồn tại", apiResponse.getMessage());
    }

    @Test
    @DisplayName("updateStudent should update student successfully")
    void testUpdateStudent_Success() {
        // Arrange
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setId("student-1");
        request.setCode("ST002");
        request.setName("Nguyen Van B");
        request.setEmail("nguyenvanb@fe.edu.vn");

        UserStudent existingStudent = new UserStudent();
        existingStudent.setId("student-1");
        existingStudent.setCode("ST001");
        existingStudent.setEmail("old@fe.edu.vn");

        when(studentExtendRepository.getStudentById(request.getId())).thenReturn(Optional.of(existingStudent));
        when(studentExtendRepository.isExistCodeUpdate(request.getCode(), existingStudent.getCode()))
                .thenReturn(false);
        when(studentExtendRepository.isExistEmailFeUpdate(request.getEmail(), existingStudent.getEmail()))
                .thenReturn(false);
        when(studentExtendRepository.save(any(UserStudent.class))).thenReturn(existingStudent);

        ReflectionTestUtils.setField(studentService, "isDisableCheckEmailFpt", "false");

        // Act
        ResponseEntity<?> response = studentService.updateStudent(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Cập nhật sinh viên thành công", apiResponse.getMessage());

        verify(studentExtendRepository).save(existingStudent);
        verify(userActivityLogHelper).saveLog(contains("vừa cập nhật sinh viên"));
        verify(redisService).delete("student:detail:" + existingStudent.getId());
        verify(redisService).deletePattern(anyString());
    }

    @Test
    @DisplayName("changeStatusStudent should toggle student status")
    void testChangeStatusStudent_Success() {
        // Arrange
        String studentId = "student-1";
        UserStudent student = new UserStudent();
        student.setId(studentId);
        student.setCode("ST001");
        student.setName("Test Student");
        student.setStatus(EntityStatus.ACTIVE);

        when(studentExtendRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentExtendRepository.save(any(UserStudent.class))).thenReturn(student);

        // Act
        ResponseEntity<?> response = studentService.changeStatusStudent(studentId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Thay đổi trạng thái sinh viên thành công", apiResponse.getMessage());
        assertEquals(EntityStatus.INACTIVE, student.getStatus());

        verify(studentExtendRepository).save(student);
        verify(userActivityLogHelper).saveLog(contains("vừa thay đổi trạng thái sinh viên"));
        verify(redisService).delete("student:detail:" + studentId);
    }

    @Test
    @DisplayName("deleteFaceStudentFactory should remove face data successfully")
    void testDeleteFaceStudentFactory_Success() {
        // Arrange
        String studentId = "student-1";
        String facilityId = "facility-1";
        UserStudent student = new UserStudent();
        student.setId(studentId);
        student.setCode("ST001");
        student.setName("Test Student");
        student.setFaceEmbedding("face-data");

        when(studentExtendRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(sessionHelper.getUserCode()).thenReturn("STAFF001");
        when(sessionHelper.getUserName()).thenReturn("Staff Name");

        // Act
        ResponseEntity<?> response = studentService.deleteFaceStudentFactory(studentId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Cấp quyền thay đổi mặt sinh viên thành công", apiResponse.getMessage());
        assertNull(student.getFaceEmbedding());

        verify(studentExtendRepository).save(student);
        verify(notificationService).add(any());
        verify(userActivityLogHelper).saveLog(contains("vừa xóa dữ liệu khuôn mặt"));
        verify(redisService).delete("student:detail:" + studentId);
        verify(redisService).delete("student:face:status:" + facilityId);
    }

    @Test
    @DisplayName("isExistFace should return face status from cache when available")
    void testIsExistFace_CachedData() {
        // Arrange
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        Map<String, Boolean> cachedFaceStatus = new HashMap<>();
        cachedFaceStatus.put("student-1", true);
        cachedFaceStatus.put("student-2", false);
        when(redisService.get("student:face:status:" + facilityId)).thenReturn(cachedFaceStatus);

        // Act
        ResponseEntity<?> response = studentService.isExistFace();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy trạng thái face của sinh viên thành công (cached)", apiResponse.getMessage());
        assertEquals(cachedFaceStatus, apiResponse.getData());

        verify(studentExtendRepository, never()).existFaceForAllStudents(any());
    }
}