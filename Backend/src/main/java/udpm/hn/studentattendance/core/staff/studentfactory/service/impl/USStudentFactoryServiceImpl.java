package udpm.hn.studentattendance.core.staff.studentfactory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.notification.model.request.NotificationAddRequest;
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.core.staff.studentfactory.model.request.*;
import udpm.hn.studentattendance.core.staff.studentfactory.model.response.STDetailUserStudentFactory;
import udpm.hn.studentattendance.core.staff.studentfactory.model.response.STPDDetailShiftByStudentResponse;
import udpm.hn.studentattendance.core.staff.studentfactory.model.response.STUserStudentResponse;
import udpm.hn.studentattendance.core.staff.factory.repository.factory.USFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.studentfactory.repository.USStudentFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.studentfactory.repository.USUSFUserStudentExtendRepository;
import udpm.hn.studentattendance.core.staff.studentfactory.service.USStudentFactoryService;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.helpers.NotificationHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class USStudentFactoryServiceImpl implements USStudentFactoryService {

        private final USStudentFactoryExtendRepository studentFactoryRepository;

        private final USFactoryExtendRepository factoryRepository;

        private final USUSFUserStudentExtendRepository userStudentFactoryExtendRepository;

        private final NotificationService notificationService;

        private final SessionHelper sessionHelper;

        @Override
        public ResponseEntity<?> getAllStudentInFactory(String factoryId,
                        USStudentFactoryRequest studentFactoryRequest) {
                Pageable pageable = PaginationHelper.createPageable(studentFactoryRequest, "createdAt");
                PageableObject listStudentFactory = PageableObject.of(studentFactoryRepository
                                .getUserStudentInFactory(pageable, factoryId, studentFactoryRequest));
                return RouterHelper.responseSuccess("Lấy tất cả sinh viên trong nhóm xưởng thành công",
                                listStudentFactory);
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

                        return RouterHelper.responseSuccess("Xoá sinh viên khỏi nhóm xưởng thành công", null);
                }
                return RouterHelper.responseError("Sinh viên không tồn tại trong nhóm xưởng");
        }

        @Override
        public ResponseEntity<?> changeStatus(String userStudentFactoryId) {
                Optional<UserStudentFactory> existStudentFactory = studentFactoryRepository
                                .findById(userStudentFactoryId);
                boolean isExistsShift = userStudentFactoryExtendRepository
                                .isStudentExistsShift(
                                                sessionHelper.getFacilityId(),
                                                existStudentFactory.get().getFactory().getId(),
                                                existStudentFactory.get().getUserStudent().getId());
                if (isExistsShift) {
                        return RouterHelper.responseError(
                                        "Đổi trạng thái sinh viên thất bại: Sinh viên tồn tại ở ca khác");
                }
                if (existStudentFactory.isPresent()) {
                        UserStudentFactory userStudentFactory = existStudentFactory.get();
                        userStudentFactory.setStatus(
                                        userStudentFactory.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE
                                                        : EntityStatus.ACTIVE);
                        studentFactoryRepository.save(userStudentFactory);
                        return RouterHelper.responseSuccess("Đổi trạng thaí sinh viên thành công", userStudentFactory);
                }
                return RouterHelper.responseError("Sinh viên không tồn tại trong nhóm xưởng");
        }

        @Override
        @Modifying
        @Transactional
        public ResponseEntity<?> createOrDeleteStudentFactory(
                        USStudentFactoryCreateUpdateRequest studentFactoryCreateUpdateRequest) {
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
                        return RouterHelper.responseError("Thêm sinh viên thất bại: Sinh viên đã có trong nhóm xưởng");
                }
                if (!isGreaterThanTwenty) {
                        return RouterHelper.responseError(
                                        "Thêm sinh viên thất bại: Số lượng sinh viên trong nhóm vượt quá 20");
                }

                if (isExistsShift) {
                        return RouterHelper.responseError("Thêm sinh viên thất bại: Sinh viên tồn tại ở ca khác");
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

                return RouterHelper.responseSuccess("Thêm sinh viên vào nhóm xưởng thành công", userStudentFactory);
        }

        @Override
        public ResponseEntity<?> getStudentFactoryExist(String factoryId) {
                List<STUserStudentResponse> listStudentInFactory = userStudentFactoryExtendRepository
                                .getAllUserStudentExistFactory(sessionHelper.getFacilityId(), factoryId);
                return RouterHelper.responseSuccess("Lấy danh sách sinh viên đã tham gia xưởng thành công",
                                listStudentInFactory);
        }

        @Override
        public ResponseEntity<?> getAllStudent(USUserStudentRequest userStudentRequest) {
                Pageable pageable = PaginationHelper.createPageable(userStudentRequest, "createdAt");
                PageableObject listStudent = PageableObject
                                .of(userStudentFactoryExtendRepository.getAllUserStudent(pageable,
                                                sessionHelper.getFacilityId(), EntityStatus.ACTIVE,
                                                userStudentRequest));
                return RouterHelper.responseSuccess("Lấy tất cả sinh viên theo cơ sở thành công", listStudent);
        }

        @Override
        public ResponseEntity<?> createStudent(USStudentFactoryAddRequest addRequest) {
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
                                                existFactory.get().getId(),
                                                existUserStudent.get().getId());

                if (existUserStudent.isEmpty()) {
                        return RouterHelper
                                        .responseError("Thêm sinh viên thất bại: Sinh viên không tồn tại trong cơ sở");
                }
                if (existStudentFactory.isPresent()) {
                        return RouterHelper.responseError("Thêm sinh viên thất bại: Sinh viên đã có trong nhóm xưởng");
                }
                if (!isGreaterThanTwenty) {
                        return RouterHelper.responseError(
                                        "Thêm sinh viên thất bại: Số lượng sinh viên trong nhóm vượt quá 20");
                }

                if (isExistsShift) {
                        return RouterHelper.responseError("Thêm sinh viên thất bại: Sinh viên tồn tại ở ca khác");
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

                return RouterHelper.responseSuccess("Thêm sinh viên vào nhóm xưởng thành công", userStudentFactory);
        }

        @Override
        public ResponseEntity<?> detailStudentFactory(String userStudentId) {
                Optional<STDetailUserStudentFactory> detailUserStudentFactory = studentFactoryRepository
                                .getDetailUserStudent(userStudentId);
                return RouterHelper.responseSuccess("Lấy thành công sinh viên", detailUserStudentFactory);
        }

        @Override
        public ResponseEntity<?> getAllPlanDateByStudent(USPDDetailShiftByStudentRequest request,
                                                         String userStudentId) {
                Pageable pageable = PaginationHelper.createPageable(request);
                PageableObject<STPDDetailShiftByStudentResponse> data = PageableObject
                                .of(studentFactoryRepository.getAllPlanDateByStudent(pageable, request, userStudentId));
                return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
        }

}
