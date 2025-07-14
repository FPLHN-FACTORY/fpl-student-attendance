package udpm.hn.studentattendance.core.staff.studentfactory.service.impl;

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
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class USStudentFactoryServiceImplTest {

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
        public void testGetAllStudentInFactory() {
                // Arrange
                String factoryId = "factory-1";
                USStudentFactoryRequest request = new USStudentFactoryRequest();

                List<STStudentFactoryResponse> studentList = new ArrayList<>();
                Page<STStudentFactoryResponse> page = new PageImpl<>(studentList);

                when(studentFactoryRepository.getUserStudentInFactory(any(), eq(factoryId), eq(request)))
                                .thenReturn(page);

                // Act
                ResponseEntity<?> response = studentFactoryService.getAllStudentInFactory(factoryId, request);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertNotNull(apiResponse);
                assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        }

        @Test
        public void testDeleteStudentInFactory_Success() {
                // Arrange
                String id = "test-id";
                UserStudentFactory userStudentFactory = new UserStudentFactory();
                Factory factory = new Factory();
                factory.setName("Test Factory");
                userStudentFactory.setFactory(factory);

                when(studentFactoryRepository.findById(id)).thenReturn(Optional.of(userStudentFactory));
                when(sessionHelper.getUserCode()).thenReturn("STAFF001");
                when(sessionHelper.getUserName()).thenReturn("Test Staff");

                // Act
                ResponseEntity<?> response = studentFactoryService.deleteStudentInFactory(id);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                verify(studentFactoryRepository).deleteById(id);
        }

        @Test
        public void testDeleteStudentInFactory_NotFound() {
                // Arrange
                String id = "non-existent-id";
                when(studentFactoryRepository.findById(id)).thenReturn(Optional.empty());

                // Act
                ResponseEntity<?> response = studentFactoryService.deleteStudentInFactory(id);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                verify(studentFactoryRepository, never()).deleteById(any());
        }

        @Test
        public void testGetStudentFactoryExist() {
                // Arrange
                String factoryId = "factory-1";
                String facilityId = "facility-1";
                List<STUserStudentResponse> studentList = new ArrayList<>();

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(userStudentFactoryExtendRepository.getAllUserStudentExistFactory(facilityId, factoryId))
                                .thenReturn(studentList);

                // Act
                ResponseEntity<?> response = studentFactoryService.getStudentFactoryExist(factoryId);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        }

        @Test
        public void testGetAllStudent() {
                // Arrange
                USUserStudentRequest request = new USUserStudentRequest();
                String facilityId = "facility-1";
                List<UserStudent> studentList = new ArrayList<>();
                Page<UserStudent> page = new PageImpl<>(studentList);

                when(sessionHelper.getFacilityId()).thenReturn(facilityId);
                when(userStudentFactoryExtendRepository.getAllUserStudent(
                                any(), eq(facilityId), eq(EntityStatus.ACTIVE), eq(request)))
                                .thenReturn(page);

                // Act
                ResponseEntity<?> response = studentFactoryService.getAllStudent(request);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        }

        @Test
        public void testDetailStudentFactory() {
                // Arrange
                String studentId = "student-1";
                STDetailUserStudentFactory detail = mock(STDetailUserStudentFactory.class);

                when(studentFactoryRepository.getDetailUserStudent(studentId)).thenReturn(Optional.of(detail));

                // Act
                ResponseEntity<?> response = studentFactoryService.detailStudentFactory(studentId);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        }

        @Test
        public void testGetAllPlanDateByStudent() {
                // Arrange
                String userStudentId = "student-1";
                USPDDetailShiftByStudentRequest request = new USPDDetailShiftByStudentRequest();
                List<STPDDetailShiftByStudentResponse> responseList = new ArrayList<>();
                Page<STPDDetailShiftByStudentResponse> page = new PageImpl<>(responseList);

                when(studentFactoryRepository.getAllPlanDateByStudent(any(), eq(request), eq(userStudentId)))
                                .thenReturn(page);

                // Act
                ResponseEntity<?> response = studentFactoryService.getAllPlanDateByStudent(request, userStudentId);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                ApiResponse apiResponse = (ApiResponse) response.getBody();
                assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        }
}