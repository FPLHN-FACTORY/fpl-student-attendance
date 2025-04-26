package udpm.hn.studentattendance.core.staff.factory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.notification.model.request.NotificationAddRequest;
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_FactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_FactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.model.response.Staff_DetailFactoryResponse;
import udpm.hn.studentattendance.core.staff.factory.repository.factory.*;
import udpm.hn.studentattendance.core.staff.factory.service.Staff_FactoryService;
import udpm.hn.studentattendance.entities.*;
import udpm.hn.studentattendance.helpers.NotificationHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class Staff_FactoryServiceImpl implements Staff_FactoryService {

        private final Staff_FactoryExtendRepository factoryRepository;

        private final Staff_ProjectFactoryExtendRepository projectFactoryExtendRepository;

        private final Staff_StaffFactoryExtendRepository staffFactoryExtendRepository;

        private final Staff_SubjectFacilityFactoryExtendRepository subjectFacilityFactoryExtendRepository;

        private final Staff_FactoryPlanExtendRepository factoryPlanExtendRepository;

        private final Staff_FactorySemesterExtendRepository semesterRepository;

        private final NotificationService notificationService;

        private final SessionHelper sessionHelper;

        @Override
        public ResponseEntity<?> getAllFactory(Staff_FactoryRequest staffFactoryRequest) {
                Pageable pageable = PaginationHelper.createPageable(staffFactoryRequest, "createdAt");
                PageableObject factories = PageableObject.of(
                                factoryRepository.getAllFactory(pageable, sessionHelper.getFacilityId(),
                                                staffFactoryRequest));
                return new ResponseEntity<>(
                                new ApiResponse(
                                                RestApiStatus.SUCCESS,
                                                "Hiển thị tất cả nhóm xưởng thành công",
                                                factories),
                                HttpStatus.OK);
        }

        @Override
        public ResponseEntity<?> getAllProject() {
                List<Project> projects = projectFactoryExtendRepository.getAllProject(EntityStatus.ACTIVE,
                                EntityStatus.ACTIVE, EntityStatus.ACTIVE, EntityStatus.ACTIVE,
                                sessionHelper.getFacilityId());
                return new ResponseEntity<>(
                                new ApiResponse(
                                                RestApiStatus.SUCCESS,
                                                "Lấy tất cả dự án theo cơ sở thành công",
                                                projects),
                                HttpStatus.OK);
        }

        @Override
        public ResponseEntity<?> getAllSubjectFacility() {
                List<SubjectFacility> subjectFacilities = subjectFacilityFactoryExtendRepository.getAllSubjectFacility(
                                EntityStatus.ACTIVE, EntityStatus.ACTIVE, sessionHelper.getFacilityId());
                return new ResponseEntity<>(
                                new ApiResponse(
                                                RestApiStatus.SUCCESS,
                                                "Lấy tất cả bộ môn cơ sở thành công",
                                                subjectFacilities),
                                HttpStatus.OK);
        }

        @Override
        public ResponseEntity<?> getAllStaff() {
                List<UserStaff> staffs = staffFactoryExtendRepository.getListUserStaff(EntityStatus.ACTIVE,
                                EntityStatus.ACTIVE, sessionHelper.getFacilityId());
                return new ResponseEntity<>(
                                new ApiResponse(
                                                RestApiStatus.SUCCESS,
                                                "Lấy tất cả giảng viên theo cơ sở thành công",
                                                staffs),
                                HttpStatus.OK);
        }

        @Override
        public ResponseEntity<?> getDetailFactory(String factoryId) {
                Optional<Staff_DetailFactoryResponse> existFactory = factoryRepository.getFactoryById(factoryId);
                if (existFactory.isPresent()) {
                        return new ResponseEntity<>(
                                        new ApiResponse(
                                                        RestApiStatus.SUCCESS,
                                                        "Xem chi tiết nhóm xưởng thành công",
                                                        existFactory),
                                        HttpStatus.OK);
                }
                return new ResponseEntity<>(
                                new ApiResponse(
                                                RestApiStatus.ERROR,
                                                "Nhóm xưởng không tồn tại",
                                                null),
                                HttpStatus.NOT_FOUND);
        }

        @Override
        public ResponseEntity<?> createFactory(Staff_FactoryCreateUpdateRequest factoryCreateUpdateRequest) {
                Optional<UserStaff> userStaff = staffFactoryExtendRepository
                                .findById(factoryCreateUpdateRequest.getIdUserStaff());
                Optional<Project> project = projectFactoryExtendRepository
                                .findById(factoryCreateUpdateRequest.getIdProject());

                if (userStaff.isEmpty()) {
                        return new ResponseEntity<>(
                                        new ApiResponse(
                                                        RestApiStatus.ERROR,
                                                        "Giảng viên không tồn tại",
                                                        null),
                                        HttpStatus.BAD_REQUEST);
                }
                if (project.isEmpty()) {
                        return new ResponseEntity<>(
                                        new ApiResponse(
                                                        RestApiStatus.ERROR,
                                                        "Dự án không tồn tại",
                                                        null),
                                        HttpStatus.BAD_REQUEST);
                }
                boolean exists = factoryRepository.isExistNameAndProject(factoryCreateUpdateRequest.getFactoryName(),
                                factoryCreateUpdateRequest.getIdProject());
                if (exists) {
                        return new ResponseEntity<>(
                                        new ApiResponse(
                                                        RestApiStatus.ERROR,
                                                        "Nhóm xưởng đã tồn tại trong dự án này",
                                                        null),
                                        HttpStatus.BAD_REQUEST);
                }
                boolean teacherJoinThanThreeFactory = factoryRepository.isTeacherJoinThanThreeFactory(
                                factoryCreateUpdateRequest.getIdUserStaff(), project.get().getSemester().getId());
                if (teacherJoinThanThreeFactory) {
                        return new ResponseEntity<>(
                                        new ApiResponse(
                                                        RestApiStatus.ERROR,
                                                        "Giảng viên này đã tham gia 3 nhóm xưởng ở kỳ học này",
                                                        null),
                                        HttpStatus.BAD_REQUEST);
                }
                Factory factory = new Factory();
                factory.setName(factoryCreateUpdateRequest.getFactoryName());
                factory.setDescription(factoryCreateUpdateRequest.getFactoryDescription());
                factory.setUserStaff(userStaff.get());
                factory.setProject(project.get());
                factory.setStatus(EntityStatus.ACTIVE);
                factoryRepository.save(factory);

                Map<String, Object> dataNotification = new HashMap<>();
                dataNotification.put(NotificationHelper.KEY_USER_ADMIN,
                                sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());
                dataNotification.put(NotificationHelper.KEY_FACTORY, factory.getName());
                NotificationAddRequest notificationAddRequest = new NotificationAddRequest();
                notificationAddRequest.setIdUser(userStaff.get().getId());
                notificationAddRequest.setType(NotificationHelper.TYPE_ADD_TEACHER_TO_FACTORY);
                notificationAddRequest.setData(dataNotification);
                notificationService.add(notificationAddRequest);

                return new ResponseEntity<>(
                                new ApiResponse(
                                                RestApiStatus.SUCCESS,
                                                "Thêm nhóm xưởng mới thành công",
                                                factory),
                                HttpStatus.CREATED);

        }

        @Override
        public ResponseEntity<?> updateFactory(Staff_FactoryCreateUpdateRequest factoryCreateUpdateRequest) {
                Optional<Factory> existFactory = factoryRepository.findById(factoryCreateUpdateRequest.getId());
                Optional<UserStaff> userStaff = staffFactoryExtendRepository
                                .findById(factoryCreateUpdateRequest.getIdUserStaff());
                Optional<Project> project = projectFactoryExtendRepository
                                .findById(factoryCreateUpdateRequest.getIdProject());

                boolean teacherJoinThanThreeFactory = factoryRepository.isTeacherJoinThanThreeFactory(
                                factoryCreateUpdateRequest.getIdUserStaff(), project.get().getSemester().getId());
                if (teacherJoinThanThreeFactory) {
                        return new ResponseEntity<>(
                                        new ApiResponse(
                                                        RestApiStatus.ERROR,
                                                        "Giảng viên này đã tham gia 3 nhóm xưởng ở kỳ học này",
                                                        null),
                                        HttpStatus.BAD_REQUEST);
                }
                if (existFactory.isPresent()) {
                        Factory factory = existFactory.get();
                        UserStaff currentUserStaff = factory.getUserStaff();

                        factory.setName(factoryCreateUpdateRequest.getFactoryName());
                        factory.setDescription(factoryCreateUpdateRequest.getFactoryDescription());
                        factory.setUserStaff(userStaff.get());
                        factory.setProject(project.get());
                        factoryRepository.save(factory);

                        Map<String, Object> dataNotification = new HashMap<>();
                        NotificationAddRequest notificationAddRequest = new NotificationAddRequest();
                        dataNotification.put(NotificationHelper.KEY_USER_ADMIN,
                                        sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());
                        dataNotification.put(NotificationHelper.KEY_FACTORY, factory.getName());
                        notificationAddRequest.setData(dataNotification);
                        if (!currentUserStaff.getId().equals(userStaff.get().getId())) {
                                notificationAddRequest.setIdUser(currentUserStaff.getId());
                                notificationAddRequest.setType(NotificationHelper.TYPE_REMOVE_TEACHER_TO_FACTORY);
                                notificationService.add(notificationAddRequest);
                        }
                        notificationAddRequest.setIdUser(userStaff.get().getId());
                        notificationAddRequest.setType(NotificationHelper.TYPE_ADD_TEACHER_TO_FACTORY);
                        notificationService.add(notificationAddRequest);

                        return new ResponseEntity<>(
                                        new ApiResponse(
                                                        RestApiStatus.SUCCESS,
                                                        "Sửa nhóm xưởng thành công",
                                                        factory),
                                        HttpStatus.OK);
                }
                return new ResponseEntity<>(
                                new ApiResponse(
                                                RestApiStatus.ERROR,
                                                "Nhóm xưởng không tồn tại",
                                                null),
                                HttpStatus.NOT_FOUND);
        }

        @Override
        public ResponseEntity<?> changeStatus(String factoryId) {
                Optional<Factory> existFactory = factoryRepository.findById(factoryId);
                if (existFactory.isPresent()) {
                        Factory factory = existFactory.get();
                        factory.setStatus(existFactory.get().getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE
                                        : EntityStatus.ACTIVE);
                        factoryRepository.save(factory);
                }
                return null;
        }

        @Override
        public ResponseEntity<?> detailFactory(String factoryId) {
                Optional<Factory> existFactory = factoryRepository.findById(factoryId);
                if (existFactory.isPresent()) {
                        return new ResponseEntity<>(
                                        new ApiResponse(
                                                        RestApiStatus.SUCCESS,
                                                        "Xem chi tiết nhóm xưởng thành công",
                                                        existFactory),
                                        HttpStatus.OK);
                }
                return new ResponseEntity<>(
                                new ApiResponse(
                                                RestApiStatus.ERROR,
                                                "Nhóm xưởng không tồn tại",
                                                null),
                                HttpStatus.NOT_FOUND);
        }

        @Override
        public ResponseEntity<?> existsPlanByFactoryId(String factoryId) {
                boolean exists = factoryPlanExtendRepository.existsPlanByFactoryId(factoryId);
                return new ResponseEntity<>(
                                new ApiResponse(
                                                RestApiStatus.SUCCESS,
                                                "Lấy thông tin nhóm xưởng thành công",
                                                exists),
                                HttpStatus.OK);
        }

        @Override
        public ResponseEntity<?> changeAllStatusByFactory() {
                Long now = new Date().getTime();
                String semesterId = null;

                // Lấy kỳ học đã kết thúc (toDate < now)
                for (Semester semester : semesterRepository.findAll()) {
                        if (semester.getToDate() < now) {
                                semesterId = semester.getId();
                                break;
                        }
                }

                if (semesterId == null) {
                        return new ResponseEntity<>(
                                        new ApiResponse(RestApiStatus.ERROR, "Không có kỳ học nào đã kết thúc", null),
                                        HttpStatus.BAD_REQUEST);
                }

                // Lấy các Factory thuộc kỳ học đã kết thúc
                List<Factory> factories = factoryRepository.getAllFactoryBySemester(sessionHelper.getFacilityId(),
                                semesterId);

                if (factories.isEmpty()) {
                        return new ResponseEntity<>(
                                        new ApiResponse(RestApiStatus.SUCCESS,
                                                        "Không có nhóm xưởng nào thuộc kỳ học đã kết thúc", factories),
                                        HttpStatus.OK);
                }

                // Đổi trạng thái cho từng Factory
                for (Factory factory : factories) {
                        // Nếu đang ACTIVE thì chuyển sang INACTIVE, ngược lại
                        factory.setStatus(factory.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE
                                        : EntityStatus.ACTIVE);
                        factoryRepository.save(factory);
                }

                return new ResponseEntity<>(
                                new ApiResponse(RestApiStatus.SUCCESS, "Đổi trạng thái nhóm xưởng kỳ trước thành công",
                                                factories),
                                HttpStatus.OK);
        }

        @Override
        public ResponseEntity<?> getAllSemester() {
                List<Semester> semesters = semesterRepository.findAll();
                return new ResponseEntity<>(
                                new ApiResponse(RestApiStatus.SUCCESS, "Lấy thành công tất cả học kỳ", semesters),
                                HttpStatus.OK);
        }

}
