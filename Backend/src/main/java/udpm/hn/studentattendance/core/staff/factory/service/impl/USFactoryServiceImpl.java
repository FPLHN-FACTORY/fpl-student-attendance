package udpm.hn.studentattendance.core.staff.factory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.notification.model.request.NotificationAddRequest;
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.model.response.USDetailFactoryResponse;
import udpm.hn.studentattendance.core.staff.factory.model.response.USProjectFactoryResponse;
import udpm.hn.studentattendance.core.staff.factory.repository.factory.*;
import udpm.hn.studentattendance.core.staff.factory.service.USFactoryService;
import udpm.hn.studentattendance.entities.*;
import udpm.hn.studentattendance.helpers.NotificationHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class USFactoryServiceImpl implements USFactoryService {

    private final USFactoryExtendRepository factoryRepository;

    private final USProjectFactoryExtendRepository projectFactoryExtendRepository;

    private final USStaffFactoryExtendRepository staffFactoryExtendRepository;

    private final USSubjectFacilityFactoryExtendRepository subjectFacilityFactoryExtendRepository;

    private final USFactoryPlanExtendRepository factoryPlanExtendRepository;

    private final USFactorySemesterExtendRepository semesterRepository;

    private final NotificationService notificationService;

    private final CommonUserStudentRepository commonUserStudentRepository;

    private final SessionHelper sessionHelper;

    private final USFactoryProjectPlanExtendRepository projectPlanExtendRepository;

//    private final USFactoryPlanDateExtendRepository planDateExtendRepository;
//
//    private final USFactoryAttendanceExtendRepository attendanceExtendRepository;

    @Override
    public ResponseEntity<?> getAllFactory(USFactoryRequest staffFactoryRequest) {
        Pageable pageable = PaginationHelper.createPageable(staffFactoryRequest, "createdAt");
        PageableObject factories = PageableObject.of(
                factoryRepository.getAllFactory(pageable, sessionHelper.getFacilityId(),
                        staffFactoryRequest));
        return RouterHelper.responseSuccess("Hiển thị tất cả nhóm xưởng thành công", factories);
    }

    @Override
    public ResponseEntity<?> getAllProject() {
        List<USProjectFactoryResponse> projects = projectFactoryExtendRepository.getAllProject(
                sessionHelper.getFacilityId());
        return RouterHelper.responseSuccess("Lấy tất cả dự án theo cơ sở thành công", projects);
    }

    @Override
    public ResponseEntity<?> getAllSubjectFacility() {
        List<SubjectFacility> subjectFacilities = subjectFacilityFactoryExtendRepository.getAllSubjectFacility(
                EntityStatus.ACTIVE, EntityStatus.ACTIVE, sessionHelper.getFacilityId());
        return RouterHelper.responseSuccess("Lấy tất cả bộ môn cơ sở thành công", subjectFacilities);
    }

    @Override
    public ResponseEntity<?> getAllStaff() {
        List<UserStaff> staffs = staffFactoryExtendRepository.getListUserStaff(EntityStatus.ACTIVE,
                EntityStatus.ACTIVE, sessionHelper.getFacilityId(), RoleConstant.TEACHER);
        return RouterHelper.responseSuccess("Lấy tất cả giảng viên theo cơ sở thành công", staffs);
    }

    @Override
    public ResponseEntity<?> getDetailFactory(String factoryId) {
        Optional<USDetailFactoryResponse> existFactory = factoryRepository.getFactoryById(factoryId);
        if (existFactory.isPresent()) {
            return RouterHelper.responseSuccess("Xem chi tiết nhóm xưởng thành công", existFactory);
        }
        return RouterHelper.responseError("Nhóm xưởng không tồn tại");
    }

    @Override
    public ResponseEntity<?> createFactory(USFactoryCreateUpdateRequest factoryCreateUpdateRequest) {
        Optional<UserStaff> userStaff = staffFactoryExtendRepository
                .findById(factoryCreateUpdateRequest.getIdUserStaff());
        Optional<Project> project = projectFactoryExtendRepository
                .findById(factoryCreateUpdateRequest.getIdProject());

        if (userStaff.isEmpty()) {
            return RouterHelper.responseError("Giảng viên không tồn tại");
        }
        if (project.isEmpty()) {
            return RouterHelper.responseError("Dự án không tồn tại");
        }
        boolean exists = factoryRepository.isExistNameAndProject(factoryCreateUpdateRequest.getFactoryName(),
                factoryCreateUpdateRequest.getIdProject(), null);
        if (exists) {
            return RouterHelper.responseError("Nhóm xưởng đã tồn tại trong dự án này");
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

        return RouterHelper.responseSuccess("Thêm nhóm xưởng mới thành công", factory);
    }

    //    @Override
//    public ResponseEntity<?> updateFactory(USFactoryCreateUpdateRequest factoryCreateUpdateRequest) {
//        Optional<Factory> existFactory = factoryRepository.findById(factoryCreateUpdateRequest.getId());
//
//        Optional<UserStaff> userStaff = staffFactoryExtendRepository
//                .findById(factoryCreateUpdateRequest.getIdUserStaff());
//        Optional<Project> project = projectFactoryExtendRepository
//                .findById(factoryCreateUpdateRequest.getIdProject());
//
//        if (existFactory.isEmpty()) {
//            return RouterHelper.responseError("Không tìm thấy nhóm xưởng");
//        }
//
//        if (userStaff.isEmpty()) {
//            return RouterHelper.responseError("Giảng viên không tồn tại");
//        }
//
//        if (project.isEmpty()) {
//            return RouterHelper.responseError("Dự án không tồn tại");
//        }
//
//        Factory factory = existFactory.get();
//
//        if (project.get() != existFactory.get().getProject()) {
//            Plan planProject = projectPlanExtendRepository.getPlanByProjectId(project.get().getId());
//            if (planProject != null) {
//                PlanFactory planFactory = factoryPlanExtendRepository.getPlanFactoryByFactoryId(factory.getId());
//                if (planFactory != null) {
//                    Plan planProject2 = projectPlanExtendRepository.getPlanByProjectId(factory.getProject().getId());
//
//                    planFactory.setFactory(factory);
//                    planFactory.setPlan(planProject2);
//                    factoryPlanExtendRepository.save(planFactory);
//                }
//            }
//        }
//        UserStaff currentUserStaff = factory.getUserStaff();
//
//        factory.setName(factoryCreateUpdateRequest.getFactoryName());
//        factory.setDescription(factoryCreateUpdateRequest.getFactoryDescription());
//        factory.setUserStaff(userStaff.get());
//        factory.setProject(project.get());
//
//
//        if (factoryRepository.isExistNameAndProject(factory.getName(),
//                factory.getProject().getId(), factory.getId())) {
//            return RouterHelper.responseError("Nhóm xưởng đã tồn tại trong dự án này");
//        }
//
//        factoryRepository.save(factory);
//
//        Map<String, Object> dataNotification = new HashMap<>();
//        NotificationAddRequest notificationAddRequest = new NotificationAddRequest();
//        dataNotification.put(NotificationHelper.KEY_USER_ADMIN,
//                sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());
//        dataNotification.put(NotificationHelper.KEY_FACTORY, factory.getName());
//        notificationAddRequest.setData(dataNotification);
//        if (!currentUserStaff.getId().equals(userStaff.get().getId())) {
//            notificationAddRequest.setIdUser(currentUserStaff.getId());
//            notificationAddRequest.setType(NotificationHelper.TYPE_REMOVE_TEACHER_TO_FACTORY);
//            notificationService.add(notificationAddRequest);
//        }
//        notificationAddRequest.setIdUser(userStaff.get().getId());
//        notificationAddRequest.setType(NotificationHelper.TYPE_ADD_TEACHER_TO_FACTORY);
//        notificationService.add(notificationAddRequest);
//
//        return RouterHelper.responseSuccess("Cập nhật nhóm xưởng thành công", factory);
//    }
    @Override
    public ResponseEntity<?> updateFactory(USFactoryCreateUpdateRequest req) {
        Factory factory = factoryRepository.findById(req.getId())
                .orElseThrow();
        UserStaff newStaff = staffFactoryExtendRepository.findById(req.getIdUserStaff())
                .orElseThrow();
        Project newProject = projectFactoryExtendRepository.findById(req.getIdProject())
                .orElseThrow();

        if (factoryRepository.isExistNameAndProject(req.getFactoryName(), newProject.getId(), factory.getId())) {
            return RouterHelper.responseError("Nhóm xưởng đã tồn tại trong dự án này");
        }
        if (newStaff == null) {
            return RouterHelper.responseError("Giảng viên không tồn tại");
        }
        if (newProject == null) {
            return RouterHelper.responseError("Dự án không tồn tại");
        }
        if (factory == null){
            return RouterHelper.responseError("Nhóm xưởng không tồn tại");
        }

        // 3. Nếu project thay đổi thì xử lý plan chỉ bằng 2 query:
        String oldProjectId = factory.getProject().getId();
        String newProjectId = newProject.getId();
        if (!oldProjectId.equals(newProjectId)) {
            // Lấy plan cũ & plan mới chỉ trong 2 lần gọi
            Plan oldPlan = projectPlanExtendRepository.getPlanByProjectId(oldProjectId);
            Plan newPlan = projectPlanExtendRepository.getPlanByProjectId(newProjectId);

            if (oldPlan != null && newPlan != null) {
                // Kiểm tra xem association đã tồn tại chưa, nếu chưa thì tạo mới
                boolean associationExists = factoryPlanExtendRepository
                        .existsByFactoryIdAndPlanId(factory.getId(), newPlan.getId());
                if (!associationExists) {
                    PlanFactory pf = factoryPlanExtendRepository.getPlanFactoryByFactoryId(factory.getId());
                    pf.setPlan(newPlan);
                    factoryPlanExtendRepository.save(pf);
                }
            }
        }

        UserStaff oldStaff = factory.getUserStaff();
        factory.setName(req.getFactoryName());
        factory.setDescription(req.getFactoryDescription());
        factory.setUserStaff(newStaff);
        factory.setProject(newProject);
        factoryRepository.save(factory);

        Map<String, Object> dataNotification = new HashMap<>();
        NotificationAddRequest notificationAddRequest = new NotificationAddRequest();
        dataNotification.put(NotificationHelper.KEY_USER_ADMIN,
                sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());
        dataNotification.put(NotificationHelper.KEY_FACTORY, factory.getName());
        notificationAddRequest.setData(dataNotification);
        if (!oldStaff.getId().equals(newStaff.getId())) {
            notificationAddRequest.setIdUser(oldStaff.getId());
            notificationAddRequest.setType(NotificationHelper.TYPE_REMOVE_TEACHER_TO_FACTORY);
            notificationService.add(notificationAddRequest);
        }
        notificationAddRequest.setIdUser(newStaff.getId());
        notificationAddRequest.setType(NotificationHelper.TYPE_ADD_TEACHER_TO_FACTORY);
        notificationService.add(notificationAddRequest);

        return RouterHelper.responseSuccess("Cập nhật nhóm xưởng thành công", factory);
    }

    @Override
    public ResponseEntity<?> changeStatus(String factoryId) {
        Optional<Factory> existFactory = factoryRepository.findById(factoryId);
        if (existFactory.isPresent()) {
            Factory factory = existFactory.get();
            factory.setStatus(existFactory.get().getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE
                    : EntityStatus.ACTIVE);
            factoryRepository.save(factory);
            if (factory.getStatus() == EntityStatus.ACTIVE) {
                commonUserStudentRepository.disableAllStudentDuplicateShiftByIdFactory(factory.getId());
            }
        }
        return RouterHelper.responseSuccess("Đổi trạng thái nhóm xưởng thành công", null);
    }

    @Override
    public ResponseEntity<?> detailFactory(String factoryId) {
        Optional<Factory> existFactory = factoryRepository.findById(factoryId);
        if (existFactory.isPresent()) {
            return RouterHelper.responseSuccess("Xem chi tiết nhóm xưởng thành công", existFactory);
        }
        return RouterHelper.responseError("Nhóm xưởng không tồn tại");
    }

    @Override
    public ResponseEntity<?> existsPlanByFactoryId(String factoryId) {
        boolean exists = factoryPlanExtendRepository.existsPlanByFactoryId(factoryId);
        return RouterHelper.responseSuccess("Lấy thông tin nhóm xưởng thành công", exists);
    }

    @Override
    public ResponseEntity<?> changeAllStatusByFactory() {
        Long now = new Date().getTime();
        String semesterId = null;

        for (Semester semester : semesterRepository.findAll()) {
            if (semester.getToDate() < now) {
                semesterId = semester.getId();
                break;
            }
        }

        if (semesterId == null) {
            return RouterHelper.responseError("Không có kỳ học nào đã kết thúc");
        }

        List<Factory> factories = factoryRepository.getAllFactoryBySemester(sessionHelper.getFacilityId(),
                semesterId);

        if (factories.isEmpty()) {
            return RouterHelper.responseSuccess("Không có nhóm xưởng nào thuộc kỳ học đã kết thúc",
                    factories);
        }

        for (Factory factory : factories) {
            factory.setStatus(factory.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE
                    : EntityStatus.ACTIVE);
            factoryRepository.save(factory);
            if (factory.getStatus() == EntityStatus.ACTIVE) {
                commonUserStudentRepository.disableAllStudentDuplicateShiftByIdFactory(factory.getId());
            }
        }

        return RouterHelper.responseSuccess("Đổi trạng thái nhóm xưởng kỳ trước thành công", factories);
    }

    @Override
    public ResponseEntity<?> getAllSemester() {
        List<Semester> semesters = semesterRepository.findAll();
        return RouterHelper.responseSuccess("Lấy thành công tất cả học kỳ", semesters);
    }

}
