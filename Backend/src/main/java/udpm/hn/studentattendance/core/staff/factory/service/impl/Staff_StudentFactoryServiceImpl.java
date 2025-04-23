package udpm.hn.studentattendance.core.staff.factory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.notification.model.request.NotificationAddRequest;
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_StudentFactoryAddRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_StudentFactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_StudentFactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_UserStudentRequest;
import udpm.hn.studentattendance.core.staff.factory.model.response.Staff_UserStudentResponse;
import udpm.hn.studentattendance.core.staff.factory.repository.Staff_FactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.repository.Staff_StudentFactoryRepository;
import udpm.hn.studentattendance.core.staff.factory.repository.Staff_UserStudentFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.service.Staff_StudentFactoryService;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.helpers.NotificationHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class Staff_StudentFactoryServiceImpl implements Staff_StudentFactoryService {

        private final Staff_StudentFactoryRepository studentFactoryRepository;

        private final Staff_FactoryExtendRepository factoryRepository;

        private final Staff_UserStudentFactoryExtendRepository userStudentFactoryExtendRepository;

        private final NotificationService notificationService;

        private final SessionHelper sessionHelper;

        @Override
        public ResponseEntity<?> getAllStudentInFactory(String factoryId,
                        Staff_StudentFactoryRequest studentFactoryRequest) {
                Pageable pageable = PaginationHelper.createPageable(studentFactoryRequest, "createdAt");
                PageableObject listStudentFactory = PageableObject.of(studentFactoryRepository
                                .getUserStudentInFactory(pageable, factoryId, studentFactoryRequest));
                return new ResponseEntity<>(
                                new ApiResponse(
                                                RestApiStatus.SUCCESS,
                                                "Lấy tất cả sinh viên trong nhóm xưởng thành công",
                                                listStudentFactory),
                                HttpStatus.OK);
        }

        @Override
        public ResponseEntity<?> deleteStudentInFactory(String userStudentFactoryId) {
                Optional<UserStudentFactory> existStudentFactory = studentFactoryRepository
                                .findById(userStudentFactoryId);
                if (existStudentFactory.isPresent()) {
                        UserStudentFactory userStudentFactory = existStudentFactory.get();
                        studentFactoryRepository.deleteById(userStudentFactoryId);

                        Map<String, Object> dataNotification = new HashMap<>();
                        dataNotification.put(NotificationHelper.KEY_USER_STAFF,
                                        sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());
                        dataNotification.put(NotificationHelper.KEY_FACTORY, userStudentFactory.getFactory().getName());
                        NotificationAddRequest notificationAddRequest = new NotificationAddRequest();
                        notificationAddRequest.setIdUser(userStudentFactory.getId());
                        notificationAddRequest.setType(NotificationHelper.TYPE_REMOVE_STUDENT_TO_FACTORY);
                        notificationAddRequest.setData(dataNotification);
                        notificationService.add(notificationAddRequest);

                        return new ResponseEntity<>(
                                        new ApiResponse(
                                                        RestApiStatus.SUCCESS,
                                                        "Xoá sinh viên khỏi nhóm xưởng thành công",
                                                        null),
                                        HttpStatus.OK);
                }
                return new ResponseEntity<>(
                                new ApiResponse(
                                                RestApiStatus.WARNING,
                                                "Sinh viên không tồn tại trong nhóm xưởng",
                                                null),
                                HttpStatus.NOT_FOUND);
        }

        @Override
        public ResponseEntity<?> changeStatus(String userStudentFactoryId) {
                Optional<UserStudentFactory> existStudentFactory = studentFactoryRepository
                                .findById(userStudentFactoryId);
                if (existStudentFactory.isPresent()) {
                        UserStudentFactory userStudentFactory = existStudentFactory.get();
                        userStudentFactory.setStatus(
                                        userStudentFactory.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE
                                                        : EntityStatus.ACTIVE);
                        studentFactoryRepository.save(userStudentFactory);
                        return new ResponseEntity<>(
                                        new ApiResponse(
                                                        RestApiStatus.SUCCESS,
                                                        "Đổi trạng thaí sinh viên thành công",
                                                        userStudentFactory),
                                        HttpStatus.OK);
                }
                return new ResponseEntity<>(
                                new ApiResponse(
                                                RestApiStatus.WARNING,
                                                "Sinh viên không tồn tại trong nhóm xưởng",
                                                null),
                                HttpStatus.NOT_FOUND);
        }

        @Override
        @Modifying
        @Transactional
        public ResponseEntity<?> createOrDeleteStudentFactory(
                        Staff_StudentFactoryCreateUpdateRequest studentFactoryCreateUpdateRequest) {
                Optional<UserStudentFactory> existStudentFactory = studentFactoryRepository
                                .getUserStudentFactoriesByUserStudentIdAndFactoryId(
                                                studentFactoryCreateUpdateRequest.getStudentId(),
                                                studentFactoryCreateUpdateRequest.getFactoryId());

                Optional<Factory> existFactory = factoryRepository
                                .findById(studentFactoryCreateUpdateRequest.getFactoryId());
                Optional<UserStudent> existUserStudent = userStudentFactoryExtendRepository
                                .findById(studentFactoryCreateUpdateRequest.getStudentId());

                boolean isGreaterThanTwenty = userStudentFactoryExtendRepository
                                .isStudentGreaterThanTwenty(studentFactoryCreateUpdateRequest.getFactoryId());
                boolean isExistsShift = userStudentFactoryExtendRepository
                                .isStudentExistsShift(
                                                sessionHelper.getFacilityId(),
                                                studentFactoryCreateUpdateRequest.getFactoryId(),
                                                studentFactoryCreateUpdateRequest.getStudentId());
                if (existStudentFactory.isPresent()) {
                        return new ResponseEntity<>(
                                        new ApiResponse(
                                                        RestApiStatus.ERROR,
                                                        "Thêm sinh viên thất bại: Sinh viên đã có trong nhóm xưởng",
                                                        null),
                                        HttpStatus.BAD_REQUEST);
                }
                if (!isGreaterThanTwenty) {
                        return new ResponseEntity<>(
                                        new ApiResponse(
                                                        RestApiStatus.ERROR,
                                                        "Thêm sinh viên thất bại: Số lượng sinh viên trong nhóm  vượt quá 20",
                                                        null),
                                        HttpStatus.BAD_REQUEST);
                }

                if (!isExistsShift) {
                        return new ResponseEntity<>(
                                        new ApiResponse(
                                                        RestApiStatus.ERROR,
                                                        "Thêm sinh viên thất bại: Sinh viên  tồn tại ở ca khác",
                                                        null),
                                        HttpStatus.BAD_REQUEST);
                }

                UserStudentFactory userStudentFactory = new UserStudentFactory();
                userStudentFactory.setUserStudent(existUserStudent.get());
                userStudentFactory.setFactory(existFactory.get());
                userStudentFactory.setStatus(EntityStatus.ACTIVE);
                studentFactoryRepository.save(userStudentFactory);

                Map<String, Object> dataNotification = new HashMap<>();
                dataNotification.put(NotificationHelper.KEY_USER_STAFF,
                                sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());
                dataNotification.put(NotificationHelper.KEY_FACTORY, existFactory.get().getName());
                NotificationAddRequest notificationAddRequest = new NotificationAddRequest();
                notificationAddRequest.setIdUser(userStudentFactory.getId());
                notificationAddRequest.setType(NotificationHelper.TYPE_ADD_STUDENT_TO_FACTORY);
                notificationAddRequest.setData(dataNotification);
                notificationService.add(notificationAddRequest);

                return new ResponseEntity<>(
                                new ApiResponse(
                                                RestApiStatus.SUCCESS,
                                                "Thêm sinh viên vào nhóm xưởng thành công",
                                                userStudentFactory),
                                HttpStatus.OK);
        }

        @Override
        public ResponseEntity<?> getStudentFactoryExist(String factoryId) {
                List<Staff_UserStudentResponse> listStudentInFactory = userStudentFactoryExtendRepository
                                .getAllUserStudentExistFactory(sessionHelper.getFacilityId(), factoryId);
                return new ResponseEntity<>(
                                new ApiResponse(
                                                RestApiStatus.SUCCESS,
                                                "Lấy danh sách sinh viên đã tham gia xưởng thành công",
                                                listStudentInFactory),
                                HttpStatus.OK);
        }

        @Override
        public ResponseEntity<?> getAllStudent(Staff_UserStudentRequest userStudentRequest) {
                Pageable pageable = PaginationHelper.createPageable(userStudentRequest, "createdAt");
                PageableObject listStudent = PageableObject
                                .of(userStudentFactoryExtendRepository.getAllUserStudent(pageable,
                                                sessionHelper.getFacilityId(), EntityStatus.ACTIVE,
                                                userStudentRequest));
                return new ResponseEntity<>(
                                new ApiResponse(
                                                RestApiStatus.SUCCESS,
                                                "Lấy tất cả sinh viên theo cơ sở thành công",
                                                listStudent),
                                HttpStatus.OK);
        }

        @Override
        public ResponseEntity<?> createStudent(Staff_StudentFactoryAddRequest addRequest) {

                Optional<UserStudent> existUserStudentByCode = userStudentFactoryExtendRepository
                                .getUserStudentByCode(addRequest.getStudentCode());
                Optional<UserStudentFactory> existStudentFactory = studentFactoryRepository
                                .getUserStudentFactoriesByUserStudentIdAndFactoryId(
                                                existUserStudentByCode.get().getId(),
                                                addRequest.getFactoryId());

                Optional<Factory> existFactory = factoryRepository.findById(addRequest.getFactoryId());
                Optional<UserStudent> existUserStudent = userStudentFactoryExtendRepository
                                .findById(existUserStudentByCode.get().getId());

                boolean isGreaterThanTwenty = userStudentFactoryExtendRepository
                                .isStudentGreaterThanTwenty(addRequest.getFactoryId());
                boolean isExistsShift = userStudentFactoryExtendRepository
                                .isStudentExistsShift(
                                                sessionHelper.getFacilityId(),
                                                addRequest.getFactoryId(),
                                                existUserStudentByCode.get().getId());

                if (existUserStudent.isEmpty()) {
                        return new ResponseEntity<>(
                                        new ApiResponse(
                                                        RestApiStatus.ERROR,
                                                        "Thêm sinh viên thất bại: Sinh viên không tồn tại trong cơ sở",
                                                        null),
                                        HttpStatus.BAD_REQUEST);
                }
                if (existStudentFactory.isPresent()) {
                        return new ResponseEntity<>(
                                        new ApiResponse(
                                                        RestApiStatus.ERROR,
                                                        "Thêm sinh viên thất bại: Sinh viên đã có trong nhóm xưởng",
                                                        null),
                                        HttpStatus.BAD_REQUEST);
                }
                if (!isGreaterThanTwenty) {
                        return new ResponseEntity<>(
                                        new ApiResponse(
                                                        RestApiStatus.ERROR,
                                                        "Thêm sinh viên thất bại: Số lượng sinh viên trong nhóm  vượt quá 20",
                                                        null),
                                        HttpStatus.BAD_REQUEST);
                }

                if (!isExistsShift) {
                        return new ResponseEntity<>(
                                        new ApiResponse(
                                                        RestApiStatus.ERROR,
                                                        "Thêm sinh viên thất bại: Sinh viên  tồn tại ở ca khác",
                                                        null),
                                        HttpStatus.BAD_REQUEST);
                }

                UserStudentFactory userStudentFactory = new UserStudentFactory();
                userStudentFactory.setUserStudent(existUserStudent.get());
                userStudentFactory.setFactory(existFactory.get());
                userStudentFactory.setStatus(EntityStatus.ACTIVE);
                studentFactoryRepository.save(userStudentFactory);

                Map<String, Object> dataNotification = new HashMap<>();
                dataNotification.put(NotificationHelper.KEY_USER_STAFF,
                                sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());
                dataNotification.put(NotificationHelper.KEY_FACTORY, existFactory.get().getName());
                NotificationAddRequest notificationAddRequest = new NotificationAddRequest();
                notificationAddRequest.setIdUser(userStudentFactory.getId());
                notificationAddRequest.setType(NotificationHelper.TYPE_ADD_STUDENT_TO_FACTORY);
                notificationAddRequest.setData(dataNotification);
                notificationService.add(notificationAddRequest);

                return new ResponseEntity<>(
                                new ApiResponse(
                                                RestApiStatus.SUCCESS,
                                                "Thêm sinh viên vào nhóm xưởng thành công",
                                                userStudentFactory),
                                HttpStatus.OK);
        }

}
