package udpm.hn.studentattendance.core.staff.studentfactory.service.impl;

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
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.core.staff.factory.repository.factory.USFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.studentfactory.model.request.*;
import udpm.hn.studentattendance.core.staff.studentfactory.model.response.STDetailUserStudentFactory;
import udpm.hn.studentattendance.core.staff.studentfactory.model.response.STPDDetailShiftByStudentResponse;
import udpm.hn.studentattendance.core.staff.studentfactory.model.response.STStudentFactoryResponse;
import udpm.hn.studentattendance.core.staff.studentfactory.model.response.STUserStudentResponse;
import udpm.hn.studentattendance.core.staff.studentfactory.repository.USStudentFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.studentfactory.repository.USUSFUserStudentExtendRepository;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class USStudentFactoryServiceImplTest {

    @Mock
    private USStudentFactoryExtendRepository studentFactoryRepository;

    @Mock
    private USFactoryExtendRepository factoryRepository;

    @Mock
    private USUSFUserStudentExtendRepository userStudentFactoryExtendRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SessionHelper sessionHelper;

    @InjectMocks
    private USStudentFactoryServiceImpl studentFactoryService;

    @Test
    @DisplayName("getAllStudentInFactory should return paginated list of students in factory")
    void testGetAllStudentInFactory() {
        // Arrange
        String factoryId = "factory-1";
        USStudentFactoryRequest request = new USStudentFactoryRequest();

        Page<STStudentFactoryResponse> mockPage = new PageImpl<>(new ArrayList<>());
        when(studentFactoryRepository.getUserStudentInFactory(any(Pageable.class), eq(factoryId), eq(request)))
                .thenReturn(mockPage);

        // Act
        ResponseEntity<?> response = studentFactoryService.getAllStudentInFactory(factoryId, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy tất cả sinh viên trong nhóm xưởng thành công", apiResponse.getMessage());

        verify(studentFactoryRepository).getUserStudentInFactory(any(Pageable.class), eq(factoryId), eq(request));
    }

    @Test
    @DisplayName("deleteStudentInFactory should delete student and send notification")
    void testDeleteStudentInFactory_Success() {
        // Arrange
        String userStudentFactoryId = "usf-1";
        UserStudentFactory userStudentFactory = mock(UserStudentFactory.class);
        Factory factory = mock(Factory.class);
        when(factory.getName()).thenReturn("Factory A");
        when(userStudentFactory.getId()).thenReturn(userStudentFactoryId);
        when(userStudentFactory.getFactory()).thenReturn(factory);

        when(studentFactoryRepository.findById(userStudentFactoryId)).thenReturn(Optional.of(userStudentFactory));
        when(sessionHelper.getUserCode()).thenReturn("STAFF001");
        when(sessionHelper.getUserName()).thenReturn("Staff Name");

        // Act
        ResponseEntity<?> response = studentFactoryService.deleteStudentInFactory(userStudentFactoryId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Xoá sinh viên khỏi nhóm xưởng thành công", apiResponse.getMessage());

        verify(studentFactoryRepository).deleteById(userStudentFactoryId);
        verify(notificationService).add(any());
    }

    @Test
    @DisplayName("deleteStudentInFactory should return error when student not found")
    void testDeleteStudentInFactory_NotFound() {
        // Arrange
        String userStudentFactoryId = "nonexistent";
        when(studentFactoryRepository.findById(userStudentFactoryId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = studentFactoryService.deleteStudentInFactory(userStudentFactoryId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Sinh viên không tồn tại trong nhóm xưởng", apiResponse.getMessage());

        verify(studentFactoryRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("changeStatus should toggle student status successfully")
    void testChangeStatus_Success() {
        // Arrange
        String userStudentFactoryId = "usf-1";
        UserStudentFactory userStudentFactory = new UserStudentFactory();
        userStudentFactory.setId(userStudentFactoryId);
        userStudentFactory.setStatus(EntityStatus.ACTIVE);

        Factory factory = mock(Factory.class);
        when(factory.getId()).thenReturn("factory-1");
        UserStudent userStudent = mock(UserStudent.class);
        when(userStudent.getId()).thenReturn("student-1");

        userStudentFactory.setFactory(factory);
        userStudentFactory.setUserStudent(userStudent);

        when(studentFactoryRepository.findById(userStudentFactoryId)).thenReturn(Optional.of(userStudentFactory));
        when(sessionHelper.getFacilityId()).thenReturn("facility-1");
        when(userStudentFactoryExtendRepository.isStudentExistsShift(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(studentFactoryRepository.save(any(UserStudentFactory.class))).thenReturn(userStudentFactory);

        // Act
        ResponseEntity<?> response = studentFactoryService.changeStatus(userStudentFactoryId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Đổi trạng thaí sinh viên thành công", apiResponse.getMessage());
        assertEquals(EntityStatus.INACTIVE, userStudentFactory.getStatus());

        verify(studentFactoryRepository).save(userStudentFactory);
    }

    @Test
    @DisplayName("changeStatus should return error when student exists in another shift")
    void testChangeStatus_ExistsInShift() {
        // Arrange
        String userStudentFactoryId = "usf-1";
        UserStudentFactory userStudentFactory = mock(UserStudentFactory.class);
        Factory factory = mock(Factory.class);
        when(factory.getId()).thenReturn("factory-1");
        UserStudent userStudent = mock(UserStudent.class);
        when(userStudent.getId()).thenReturn("student-1");

        when(userStudentFactory.getFactory()).thenReturn(factory);
        when(userStudentFactory.getUserStudent()).thenReturn(userStudent);

        when(studentFactoryRepository.findById(userStudentFactoryId)).thenReturn(Optional.of(userStudentFactory));
        when(sessionHelper.getFacilityId()).thenReturn("facility-1");
        when(userStudentFactoryExtendRepository.isStudentExistsShift(anyString(), anyString(), anyString()))
                .thenReturn(true);

        // Act
        ResponseEntity<?> response = studentFactoryService.changeStatus(userStudentFactoryId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Đổi trạng thái sinh viên thất bại: Sinh viên tồn tại ở ca khác", apiResponse.getMessage());

        verify(studentFactoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrDeleteStudentFactory should create new student factory successfully")
    void testCreateOrDeleteStudentFactory_Success() {
        // Arrange
        USStudentFactoryCreateUpdateRequest request = new USStudentFactoryCreateUpdateRequest();
        request.setStudentId("student-1");
        request.setFactoryId("factory-1");

        Factory factory = mock(Factory.class);
        when(factory.getName()).thenReturn("Factory A");
        UserStudent userStudent = mock(UserStudent.class);

        when(studentFactoryRepository.getUserStudentFactoriesByUserStudentIdAndFactoryId(request.getStudentId(),
                request.getFactoryId()))
                .thenReturn(Optional.empty());
        when(factoryRepository.findById(request.getFactoryId())).thenReturn(Optional.of(factory));
        when(userStudentFactoryExtendRepository.findById(request.getStudentId())).thenReturn(Optional.of(userStudent));
        when(userStudentFactoryExtendRepository.isStudentGreaterThanTwenty(request.getFactoryId())).thenReturn(true);
        when(userStudentFactoryExtendRepository.isStudentExistsShift(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(sessionHelper.getFacilityId()).thenReturn("facility-1");
        when(sessionHelper.getUserCode()).thenReturn("STAFF001");
        when(sessionHelper.getUserName()).thenReturn("Staff Name");

        UserStudentFactory savedUserStudentFactory = new UserStudentFactory();
        savedUserStudentFactory.setId("new-usf-id");
        when(studentFactoryRepository.save(any(UserStudentFactory.class))).thenReturn(savedUserStudentFactory);

        // Act
        ResponseEntity<?> response = studentFactoryService.createOrDeleteStudentFactory(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Thêm sinh viên vào nhóm xưởng thành công", apiResponse.getMessage());

        verify(studentFactoryRepository).save(any(UserStudentFactory.class));
        verify(notificationService).add(any());
    }

    @Test
    @DisplayName("createOrDeleteStudentFactory should return error when student already exists")
    void testCreateOrDeleteStudentFactory_AlreadyExists() {
        // Arrange
        USStudentFactoryCreateUpdateRequest request = new USStudentFactoryCreateUpdateRequest();
        request.setStudentId("student-1");
        request.setFactoryId("factory-1");

        when(studentFactoryRepository.getUserStudentFactoriesByUserStudentIdAndFactoryId(request.getStudentId(),
                request.getFactoryId()))
                .thenReturn(Optional.of(mock(UserStudentFactory.class)));

        // Act
        ResponseEntity<?> response = studentFactoryService.createOrDeleteStudentFactory(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Thêm sinh viên thất bại: Sinh viên đã có trong nhóm xưởng", apiResponse.getMessage());

        verify(studentFactoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrDeleteStudentFactory should return error when factory is full")
    void testCreateOrDeleteStudentFactory_FactoryFull() {
        // Arrange
        USStudentFactoryCreateUpdateRequest request = new USStudentFactoryCreateUpdateRequest();
        request.setStudentId("student-1");
        request.setFactoryId("factory-1");

        when(studentFactoryRepository.getUserStudentFactoriesByUserStudentIdAndFactoryId(request.getStudentId(),
                request.getFactoryId()))
                .thenReturn(Optional.empty());
        when(userStudentFactoryExtendRepository.isStudentGreaterThanTwenty(request.getFactoryId())).thenReturn(false);

        // Act
        ResponseEntity<?> response = studentFactoryService.createOrDeleteStudentFactory(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Thêm sinh viên thất bại: Số lượng sinh viên trong nhóm vượt quá 20", apiResponse.getMessage());
    }

    @Test
    @DisplayName("getStudentFactoryExist should return list of students in factory")
    void testGetStudentFactoryExist() {
        // Arrange
        String factoryId = "factory-1";
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        List<STUserStudentResponse> studentList = Arrays.asList(
                mock(STUserStudentResponse.class),
                mock(STUserStudentResponse.class));
        when(userStudentFactoryExtendRepository.getAllUserStudentExistFactory(facilityId, factoryId))
                .thenReturn(studentList);

        // Act
        ResponseEntity<?> response = studentFactoryService.getStudentFactoryExist(factoryId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy danh sách sinh viên đã tham gia xưởng thành công", apiResponse.getMessage());
        assertEquals(studentList, apiResponse.getData());
    }

    @Test
    @DisplayName("getAllStudent should return paginated list of all students")
    void testGetAllStudent() {
        // Arrange
        USUserStudentRequest request = new USUserStudentRequest();
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        Page<UserStudent> mockPage = new PageImpl<>(new ArrayList<>());
        when(userStudentFactoryExtendRepository.getAllUserStudent(
                any(Pageable.class), eq(facilityId), eq(EntityStatus.ACTIVE), eq(request)))
                .thenReturn(mockPage);

        // Act
        ResponseEntity<?> response = studentFactoryService.getAllStudent(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy tất cả sinh viên theo cơ sở thành công", apiResponse.getMessage());

        verify(userStudentFactoryExtendRepository).getAllUserStudent(
                any(Pageable.class), eq(facilityId), eq(EntityStatus.ACTIVE), eq(request));
    }

    @Test
    @DisplayName("createStudent should add student to factory by student code")
    void testCreateStudent_Success() {
        // Arrange
        USStudentFactoryAddRequest request = new USStudentFactoryAddRequest();
        request.setStudentCode("ST001");
        request.setFactoryId("factory-1");

        UserStudent userStudent = mock(UserStudent.class);
        when(userStudent.getId()).thenReturn("student-1");
        Factory factory = mock(Factory.class);
        when(factory.getId()).thenReturn("factory-1");
        when(factory.getName()).thenReturn("Factory A");

        when(userStudentFactoryExtendRepository.getUserStudentByCode(request.getStudentCode()))
                .thenReturn(Optional.of(userStudent));
        when(studentFactoryRepository.getUserStudentFactoriesByUserStudentIdAndFactoryId("student-1",
                request.getFactoryId()))
                .thenReturn(Optional.empty());
        when(factoryRepository.findById(request.getFactoryId())).thenReturn(Optional.of(factory));
        when(userStudentFactoryExtendRepository.findById("student-1")).thenReturn(Optional.of(userStudent));
        when(userStudentFactoryExtendRepository.isStudentGreaterThanTwenty(request.getFactoryId())).thenReturn(true);
        when(userStudentFactoryExtendRepository.isStudentExistsShift(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(sessionHelper.getFacilityId()).thenReturn("facility-1");
        when(sessionHelper.getUserCode()).thenReturn("STAFF001");
        when(sessionHelper.getUserName()).thenReturn("Staff Name");

        UserStudentFactory savedUserStudentFactory = new UserStudentFactory();
        savedUserStudentFactory.setId("new-usf-id");
        when(studentFactoryRepository.save(any(UserStudentFactory.class))).thenReturn(savedUserStudentFactory);

        // Act
        ResponseEntity<?> response = studentFactoryService.createStudent(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Thêm sinh viên vào nhóm xưởng thành công", apiResponse.getMessage());

        verify(studentFactoryRepository).save(any(UserStudentFactory.class));
        verify(notificationService).add(any());
    }

    @Test
    @DisplayName("detailStudentFactory should return student detail")
    void testDetailStudentFactory() {
        // Arrange
        String userStudentId = "student-1";
        STDetailUserStudentFactory detail = mock(STDetailUserStudentFactory.class);
        when(studentFactoryRepository.getDetailUserStudent(userStudentId)).thenReturn(Optional.of(detail));

        // Act
        ResponseEntity<?> response = studentFactoryService.detailStudentFactory(userStudentId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy thành công sinh viên", apiResponse.getMessage());
        assertEquals(Optional.of(detail), apiResponse.getData());
    }

    @Test
    @DisplayName("getAllPlanDateByStudent should return paginated plan dates")
    void testGetAllPlanDateByStudent() {
        // Arrange
        String userStudentId = "student-1";
        USPDDetailShiftByStudentRequest request = new USPDDetailShiftByStudentRequest();

        Page<STPDDetailShiftByStudentResponse> mockPage = new PageImpl<>(new ArrayList<>());
        when(studentFactoryRepository.getAllPlanDateByStudent(any(Pageable.class), eq(request), eq(userStudentId)))
                .thenReturn(mockPage);

        // Act
        ResponseEntity<?> response = studentFactoryService.getAllPlanDateByStudent(request, userStudentId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());

        verify(studentFactoryRepository).getAllPlanDateByStudent(any(Pageable.class), eq(request), eq(userStudentId));
    }
}