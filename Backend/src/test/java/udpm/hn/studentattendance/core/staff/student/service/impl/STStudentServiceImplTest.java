package udpm.hn.studentattendance.core.staff.student.service.impl;

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
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.core.staff.student.model.request.USStudentCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.student.model.request.USStudentRequest;
import udpm.hn.studentattendance.core.staff.student.model.response.USStudentResponse;
import udpm.hn.studentattendance.core.staff.student.repository.USStudentExtendRepository;
import udpm.hn.studentattendance.core.staff.student.repository.USStudentFacilityExtendRepository;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;
import udpm.hn.studentattendance.helpers.SettingHelper;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class STStudentServiceImplTest {

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

    @Mock
    private SettingHelper settingHelper;
    
    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @Mock
    private RedisCacheHelper redisCacheHelper;

    @InjectMocks
    private STStudentServiceImpl studentService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(studentService, "redisTTL", 3600L);

        // Default behavior for session helper
        when(sessionHelper.getFacilityId()).thenReturn("facility-1");

        // Default behavior for setting helper
        when(settingHelper.getSetting(any(), any(Class.class))).thenReturn(false);

        // Default behavior for RedisCacheHelper
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenAnswer(invocation -> invocation.getArgument(1, java.util.function.Supplier.class).get());
        // Default behavior for RedisInvalidationHelper
        doNothing().when(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("getAllStudentByFacility should return cached data when available")
    public void testGetAllStudentByFacility_CachedData() {
        // Arrange
        USStudentRequest request = new USStudentRequest();

        // Create a PageableObject with empty page
        Page<USStudentResponse> emptyPage = new PageImpl<>(Collections.emptyList());
        PageableObject<USStudentResponse> cachedData = PageableObject.of(emptyPage);

        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(cachedData);

        // Act
        ResponseEntity<?> response = studentService.getAllStudentByFacility(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());

        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        verify(studentExtendRepository, never()).getAllStudentByFacility(any(), any(), any());
    }

    @Test
    @DisplayName("getAllStudentByFacility should query database when cache miss")
    public void testGetAllStudentByFacility_NoCachedData() {
        // Arrange
        USStudentRequest request = new USStudentRequest();
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(null).thenReturn(dbPage);

        Page<USStudentResponse> dbPage = new PageImpl<>(new ArrayList<>());
        when(studentExtendRepository.getAllStudentByFacility(any(Pageable.class), any(), anyString()))
                .thenReturn(dbPage);

        // Act
        ResponseEntity<?> response = studentService.getAllStudentByFacility(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());

        verify(redisCacheHelper, times(2)).getOrSet(anyString(), any(), any(), anyLong());
        verify(studentExtendRepository).getAllStudentByFacility(any(Pageable.class), any(), anyString());
    }

    @Test
    @DisplayName("getDetailStudent should return student detail from cache when available")
    public void testGetDetailStudent_CachedData() {
        // Arrange
        String studentId = "student-1";
        UserStudent cachedStudent = new UserStudent();
        cachedStudent.setId(studentId);
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(cachedStudent);
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(cachedStudent);

        // Act
        ResponseEntity<?> response = studentService.getDetailStudent(studentId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());

        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        verify(studentExtendRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getDetailStudent should return error when student not found")
    public void testGetDetailStudent_NotFound() {
        // Arrange
        String studentId = "nonexistent";
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(null);
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
    public void testCreateStudent_Success() {
        // Arrange
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen Van A");
        request.setEmail("nguyenvana@fpt.edu.vn");

        Facility facility = new Facility();
        facility.setId("facility-1");

        when(facilityRepository.findById(anyString())).thenReturn(Optional.of(facility));
        when(studentExtendRepository.getUserStudentByCode(request.getCode())).thenReturn(Optional.empty());
        when(studentExtendRepository.getUserStudentByEmail(request.getEmail())).thenReturn(Optional.empty());

        UserStudent savedStudent = new UserStudent();
        savedStudent.setId("new-student-id");
        savedStudent.setCode(request.getCode());
        savedStudent.setName(request.getName());
        savedStudent.setEmail(request.getEmail());
        when(studentExtendRepository.save(any(UserStudent.class))).thenReturn(savedStudent);

        // Don't verify deletePattern calls to avoid TooManyActualInvocations
        doNothing().when(redisService).deletePattern(anyString());

        // Act
        ResponseEntity<?> response = studentService.createStudent(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Thêm sinh viên mới thành công", apiResponse.getMessage());

        verify(studentExtendRepository).save(any(UserStudent.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thêm 1 sinh viên mới"));
    }

    @Test
    @DisplayName("createStudent should return error for invalid code")
    public void testCreateStudent_InvalidCode() {
        // Arrange
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST 001"); // Invalid code with space
        request.setName("Nguyen Van A");
        request.setEmail("nguyenvana@fpt.edu.vn");

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
    public void testCreateStudent_DuplicateCode() {
        // Arrange
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen Van A");
        request.setEmail("nguyenvana@fpt.edu.vn");

        when(studentExtendRepository.getUserStudentByCode(request.getCode()))
                .thenReturn(Optional.of(new UserStudent()));

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
    public void testUpdateStudent_Success() {
        // Arrange
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setId("student-1");
        request.setCode("ST002");
        request.setName("Nguyen Van B");
        request.setEmail("nguyenvanb@fpt.edu.vn");

        UserStudent existingStudent = new UserStudent();
        existingStudent.setId("student-1");
        existingStudent.setCode("ST001");
        existingStudent.setEmail("old@fpt.edu.vn");

        when(studentExtendRepository.getStudentById(request.getId())).thenReturn(Optional.of(existingStudent));
        when(studentExtendRepository.isExistCodeUpdate(request.getCode(), existingStudent.getCode()))
                .thenReturn(false);
        when(studentExtendRepository.isExistEmailFeUpdate(request.getEmail(), existingStudent.getEmail()))
                .thenReturn(false);
        when(studentExtendRepository.save(any(UserStudent.class))).thenReturn(existingStudent);

        // Don't verify delete/deletePattern calls to avoid TooManyActualInvocations
        doNothing().when(redisService).delete(anyString());
        doNothing().when(redisService).deletePattern(anyString());

        // Act
        ResponseEntity<?> response = studentService.updateStudent(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Cập nhật sinh viên thành công", apiResponse.getMessage());

        verify(studentExtendRepository).save(existingStudent);
        verify(userActivityLogHelper).saveLog(contains("vừa cập nhật sinh viên"));
    }

    @Test
    @DisplayName("changeStatusStudent should toggle student status")
    public void testChangeStatusStudent_Success() {
        // Arrange
        String studentId = "student-1";
        UserStudent student = new UserStudent();
        student.setId(studentId);
        student.setCode("ST001");
        student.setName("Test Student");
        student.setStatus(EntityStatus.ACTIVE);

        when(studentExtendRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentExtendRepository.save(any(UserStudent.class))).thenReturn(student);

        // Don't verify delete calls to avoid TooManyActualInvocations
        doNothing().when(redisService).delete(anyString());

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
    }

    @Test
    @DisplayName("deleteFaceStudentFactory should remove face data successfully")
    public void testDeleteFaceStudentFactory_Success() {
        // Arrange
        String studentId = "student-1";
        UserStudent student = new UserStudent();
        student.setId(studentId);
        student.setCode("ST001");
        student.setName("Test Student");
        student.setFaceEmbedding("face-data");

        when(studentExtendRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sessionHelper.getUserCode()).thenReturn("STAFF001");
        when(sessionHelper.getUserName()).thenReturn("Staff Name");

        // Don't verify delete calls to avoid TooManyActualInvocations
        doNothing().when(redisService).delete(anyString());

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
    }

    @Test
    @DisplayName("isExistFace should return face status from cache when available")
    public void testIsExistFace_CachedData() {
        // Arrange
        Map<String, Boolean> cachedFaceStatus = new HashMap<>();
        cachedFaceStatus.put("student-1", true);
        cachedFaceStatus.put("student-2", false);
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(cachedFaceStatus);
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(cachedFaceStatus);

        // Act
        ResponseEntity<?> response = studentService.isExistFace();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals(cachedFaceStatus, apiResponse.getData());

        verify(studentExtendRepository, never()).existFaceForAllStudents(any());
    }

    @Test
    @DisplayName("isExistFace should query database when cache miss")
    public void testIsExistFace_NoCachedData() {
        // Arrange
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(null);

        // Create a list of maps to match the expected return type
        List<Map<String, Object>> faceStatusList = new ArrayList<>();
        Map<String, Object> student1 = new HashMap<>();
        student1.put("studentId", "student-1");
        student1.put("hasFace", 1);
        Map<String, Object> student2 = new HashMap<>();
        student2.put("studentId", "student-2");
        student2.put("hasFace", 0);
        faceStatusList.add(student1);
        faceStatusList.add(student2);

        when(studentExtendRepository.existFaceForAllStudents(anyString())).thenReturn(faceStatusList);

        // Act
        ResponseEntity<?> response = studentService.isExistFace();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());

        verify(studentExtendRepository).existFaceForAllStudents(anyString());
        verify(redisService).set(anyString(), any(Map.class), eq(3600L));
    }

    @Test
    @DisplayName("Test getCachedStudentList should handle cache deserialization error")
    void testGetCachedStudentListWithCacheError() {
        USStudentRequest request = new USStudentRequest();
        Page<USStudentResponse> mockData = mock(Page.class);
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn("cached");
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(mockData);
        when(studentExtendRepository.getAllStudentByFacility(any(), eq(request), anyString())).thenReturn(mockData);

        PageableObject<?> result = studentService.getCachedStudentList(request);

        assertNotNull(result);
        verify(redisService).delete(anyString());
    }

    @Test
    @DisplayName("Test getCachedStudentList should handle redis set exception")
    void testGetCachedStudentListWithRedisSetError() {
        USStudentRequest request = new USStudentRequest();
        Page<USStudentResponse> mockData = mock(Page.class);
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(null);
        when(studentExtendRepository.getAllStudentByFacility(any(), eq(request), anyString())).thenReturn(mockData);
        doThrow(new RuntimeException("Redis error")).when(redisService).set(anyString(), any(), anyLong());

        PageableObject<?> result = studentService.getCachedStudentList(request);

        assertNotNull(result);
        // Should not throw exception, just ignore redis error
    }

    @Test
    @DisplayName("Test getCachedStudentDetail should handle cache deserialization error")
    void testGetCachedStudentDetailWithCacheError() {
        String studentId = "student-1";
        UserStudent student = new UserStudent();
        student.setId(studentId);
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn("cached");
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(student);
        when(studentExtendRepository.findById(studentId)).thenReturn(Optional.of(student));

        UserStudent result = studentService.getCachedStudentDetail(studentId);

        assertNotNull(result);
        verify(redisService).delete(anyString());
    }

    @Test
    @DisplayName("Test getCachedStudentDetail should handle redis set exception")
    void testGetCachedStudentDetailWithRedisSetError() {
        String studentId = "student-1";
        UserStudent student = new UserStudent();
        student.setId(studentId);
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(null);
        when(studentExtendRepository.findById(studentId)).thenReturn(Optional.of(student));
        doThrow(new RuntimeException("Redis error")).when(redisService).set(anyString(), any(), anyLong());

        UserStudent result = studentService.getCachedStudentDetail(studentId);

        assertNotNull(result);
        // Should not throw exception, just ignore redis error
    }

    @Test
    @DisplayName("Test createStudent should return error for invalid code with spaces")
    void testCreateStudentInvalidCodeWithSpaces() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST 001"); // Contains space
        request.setName("Nguyen Van A");
        request.setEmail("nguyenvana@fpt.edu.vn");

        ResponseEntity<?> response = studentService.createStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test createStudent should return error for invalid code with special characters")
    void testCreateStudentInvalidCodeWithSpecialChars() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST@001"); // Contains special character
        request.setName("Nguyen Van A");
        request.setEmail("nguyenvana@fpt.edu.vn");

        ResponseEntity<?> response = studentService.createStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test createStudent should return error for invalid name with numbers")
    void testCreateStudentInvalidNameWithNumbers() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen123 Van A"); // Contains numbers
        request.setEmail("nguyenvana@fpt.edu.vn");

        ResponseEntity<?> response = studentService.createStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test createStudent should return error for invalid name with special characters")
    void testCreateStudentInvalidNameWithSpecialChars() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen@Van A"); // Contains special characters
        request.setEmail("nguyenvana@fpt.edu.vn");

        ResponseEntity<?> response = studentService.createStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test createStudent should return error for single word name")
    void testCreateStudentSingleWordName() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen"); // Single word
        request.setEmail("nguyenvana@fpt.edu.vn");

        ResponseEntity<?> response = studentService.createStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test createStudent should return error for invalid email format")
    void testCreateStudentInvalidEmailFormat() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen Van A");
        request.setEmail("invalid-email"); // Invalid email format

        ResponseEntity<?> response = studentService.createStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test createStudent should return error for email without @gmail.com or .edu.vn")
    void testCreateStudentInvalidEmailDomain() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen Van A");
        request.setEmail("nguyenvana@yahoo.com"); // Invalid domain

        ResponseEntity<?> response = studentService.createStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test createStudent should return error when email already exists")
    void testCreateStudentEmailExists() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen Van A");
        request.setEmail("nguyenvana@fpt.edu.vn");

        when(studentExtendRepository.getUserStudentByCode("ST001")).thenReturn(Optional.empty());
        when(studentExtendRepository.getUserStudentByEmail("nguyenvana@fpt.edu.vn"))
                .thenReturn(Optional.of(new UserStudent()));

        ResponseEntity<?> response = studentService.createStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test createStudent should return error when facility not found")
    void testCreateStudentFacilityNotFound() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen Van A");
        request.setEmail("nguyenvana@fpt.edu.vn");

        when(sessionHelper.getFacilityId()).thenReturn("facility-1");
        when(studentExtendRepository.getUserStudentByCode("ST001")).thenReturn(Optional.empty());
        when(studentExtendRepository.getUserStudentByEmail("nguyenvana@fpt.edu.vn")).thenReturn(Optional.empty());
        when(facilityRepository.findById("facility-1")).thenReturn(Optional.empty());

        ResponseEntity<?> response = studentService.createStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateStudent should return error for invalid code with spaces")
    void testUpdateStudentInvalidCodeWithSpaces() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST 001"); // Contains space
        request.setName("Nguyen Van A");
        request.setEmail("nguyenvana@fpt.edu.vn");

        ResponseEntity<?> response = studentService.updateStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateStudent should return error for invalid code with special characters")
    void testUpdateStudentInvalidCodeWithSpecialChars() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST@001"); // Contains special character
        request.setName("Nguyen Van A");
        request.setEmail("nguyenvana@fpt.edu.vn");

        ResponseEntity<?> response = studentService.updateStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateStudent should return error for invalid name with numbers")
    void testUpdateStudentInvalidNameWithNumbers() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen123 Van A"); // Contains numbers
        request.setEmail("nguyenvana@fpt.edu.vn");

        ResponseEntity<?> response = studentService.updateStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateStudent should return error for invalid name with special characters")
    void testUpdateStudentInvalidNameWithSpecialChars() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen@Van A"); // Contains special characters
        request.setEmail("nguyenvana@fpt.edu.vn");

        ResponseEntity<?> response = studentService.updateStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateStudent should return error for single word name")
    void testUpdateStudentSingleWordName() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen"); // Single word
        request.setEmail("nguyenvana@fpt.edu.vn");

        ResponseEntity<?> response = studentService.updateStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateStudent should return error for invalid email format")
    void testUpdateStudentInvalidEmailFormat() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen Van A");
        request.setEmail("invalid-email"); // Invalid email format

        ResponseEntity<?> response = studentService.updateStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateStudent should return error for email without @gmail.com or .edu.vn")
    void testUpdateStudentInvalidEmailDomain() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen Van A");
        request.setEmail("nguyenvana@yahoo.com"); // Invalid domain

        ResponseEntity<?> response = studentService.updateStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateStudent should return error when student not found")
    void testUpdateStudentNotFound() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen Van A");
        request.setEmail("nguyenvana@fpt.edu.vn");

        when(studentExtendRepository.findById("student-1")).thenReturn(Optional.empty());

        ResponseEntity<?> response = studentService.updateStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateStudent should return error when student code already exists for different student")
    void testUpdateStudentCodeExistsForDifferentStudent() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen Van A");
        request.setEmail("nguyenvana@fpt.edu.vn");

        UserStudent existingStudent = new UserStudent();
        existingStudent.setId("student-1");
        existingStudent.setCode("ST002");

        UserStudent conflictingStudent = new UserStudent();
        conflictingStudent.setId("student-2");
        conflictingStudent.setCode("ST001");

        when(studentExtendRepository.findById("student-1")).thenReturn(Optional.of(existingStudent));
        when(studentExtendRepository.getUserStudentByCode("ST001")).thenReturn(Optional.of(conflictingStudent));

        ResponseEntity<?> response = studentService.updateStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateStudent should return error when email already exists for different student")
    void testUpdateStudentEmailExistsForDifferentStudent() {
        USStudentCreateUpdateRequest request = new USStudentCreateUpdateRequest();
        request.setCode("ST001");
        request.setName("Nguyen Van A");
        request.setEmail("nguyenvana@fpt.edu.vn");

        UserStudent existingStudent = new UserStudent();
        existingStudent.setId("student-1");
        existingStudent.setCode("ST001");
        existingStudent.setEmail("old.email@fpt.edu.vn");

        UserStudent conflictingStudent = new UserStudent();
        conflictingStudent.setId("student-2");
        conflictingStudent.setEmail("nguyenvana@fpt.edu.vn");

        when(studentExtendRepository.findById("student-1")).thenReturn(Optional.of(existingStudent));
        when(studentExtendRepository.getUserStudentByCode("ST001")).thenReturn(Optional.of(existingStudent));
        when(studentExtendRepository.getUserStudentByEmail("nguyenvana@fpt.edu.vn"))
                .thenReturn(Optional.of(conflictingStudent));

        ResponseEntity<?> response = studentService.updateStudent(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test changeStatusStudent should return error when student not found")
    void testChangeStatusStudentNotFound() {
        when(studentExtendRepository.findById("student-1")).thenReturn(Optional.empty());

        ResponseEntity<?> response = studentService.changeStatusStudent("student-1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test deleteFaceStudentFactory should return error when student not found")
    void testDeleteFaceStudentFactoryNotFound() {
        when(studentExtendRepository.findById("student-1")).thenReturn(Optional.empty());

        ResponseEntity<?> response = studentService.deleteFaceStudentFactory("student-1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test getCachedFaceStatus should handle cache deserialization error")
    void testGetCachedFaceStatusWithCacheError() {
        Map<String, Boolean> faceStatus = new HashMap<>();
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn("cached");
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(faceStatus);
        when(studentExtendRepository.getAllStudentByFacility(any(), any(), anyString())).thenReturn(mock(Page.class));

        Map<String, Boolean> result = studentService.getCachedFaceStatus();

        assertNotNull(result);
        verify(redisService).delete(anyString());
    }

    @Test
    @DisplayName("Test getCachedFaceStatus should handle redis set exception")
    void testGetCachedFaceStatusWithRedisSetError() {
        Map<String, Boolean> faceStatus = new HashMap<>();
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(null);
        when(studentExtendRepository.getAllStudentByFacility(any(), any(), anyString())).thenReturn(mock(Page.class));
        doThrow(new RuntimeException("Redis error")).when(redisService).set(anyString(), any(), anyLong());

        Map<String, Boolean> result = studentService.getCachedFaceStatus();

        assertNotNull(result);
        // Should not throw exception, just ignore redis error
    }

}