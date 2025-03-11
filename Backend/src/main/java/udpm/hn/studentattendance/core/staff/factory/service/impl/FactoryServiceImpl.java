package udpm.hn.studentattendance.core.staff.factory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_FactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_FactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.model.response.Staff_DetailFactoryResponse;
import udpm.hn.studentattendance.core.staff.factory.repository.FactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.repository.ProjectFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.repository.StaffFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.repository.SubjectFacilityFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.service.FactoryService;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.entities.SubjectFacility;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.utils.CodeGeneratorUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class FactoryServiceImpl implements FactoryService {

    private final FactoryExtendRepository factoryRepository;

    private final ProjectFactoryExtendRepository projectFactoryExtendRepository;

    private final StaffFactoryExtendRepository staffFactoryExtendRepository;

    private final SubjectFacilityFactoryExtendRepository subjectFacilityFactoryExtendRepository;

    private final SessionHelper sessionHelper;

    @Override
    public ResponseEntity<?> getAllFactory(Staff_FactoryRequest staffFactoryRequest) {
        Pageable pageable = PaginationHelper.createPageable(staffFactoryRequest, "createdAt");
        PageableObject factories = PageableObject.of(
                factoryRepository.getAllFactory(pageable, sessionHelper.getFacilityId(), staffFactoryRequest));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Hiển thị tất cả nhóm xưởng thành công",
                        factories
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllProject() {
        List<Project> projects = projectFactoryExtendRepository.getAllProject
                (EntityStatus.ACTIVE, EntityStatus.ACTIVE, EntityStatus.ACTIVE, sessionHelper.getFacilityId());
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả dự án theo cơ sở thành công",
                        projects
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllSubjectFacility() {
        List<SubjectFacility> subjectFacilities = subjectFacilityFactoryExtendRepository.getAllSubjectFacility
                (EntityStatus.ACTIVE, EntityStatus.ACTIVE, sessionHelper.getFacilityId());
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả bộ môn cơ sở thành công",
                        subjectFacilities
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllStaff() {
        List<UserStaff> staffs = staffFactoryExtendRepository.getListUserStaff
                (EntityStatus.ACTIVE, EntityStatus.ACTIVE, sessionHelper.getFacilityId());
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả giảng viên theo cơ sở thành công",
                        staffs
                ),
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
                            existFactory
                    ),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Nhóm xưởng không tồn tại",
                        null
                ),
                HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<?> createFactory(Staff_FactoryCreateUpdateRequest factoryCreateUpdateRequest) {
        Optional<UserStaff> userStaff = staffFactoryExtendRepository.findById(factoryCreateUpdateRequest.getIdUserStaff());
        Optional<Project> project = projectFactoryExtendRepository.findById(factoryCreateUpdateRequest.getIdProject());
        if (userStaff.isEmpty()){
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Giảng viên không tồn tại",
                            null
                    ),
                    HttpStatus.BAD_REQUEST);
        }
        if (project.isEmpty()){
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Dự án không tồn tại",
                            null
                    ),
                    HttpStatus.BAD_REQUEST);
        }
        if (factoryRepository.isExistNameAndProject(factoryCreateUpdateRequest.getFactoryName(), project.get().getId())){
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Nhóm xưởng đã tồn tại trong dự án này",
                            null
                    ),
                    HttpStatus.BAD_REQUEST);
        }
        Factory factory = new Factory();
        factory.setId(CodeGeneratorUtils.generateRandom());
        factory.setName(factoryCreateUpdateRequest.getFactoryName());
        factory.setDescription(factoryCreateUpdateRequest.getFactoryDescription());
        factory.setUserStaff(userStaff.get());
        factory.setProject(project.get());
        factory.setStatus(EntityStatus.ACTIVE);
        factoryRepository.save(factory);
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Thêm nhóm xưởng mới thành công",
                        factory
                ),
                HttpStatus.CREATED);


    }


    @Override
    public ResponseEntity<?> updateFactory(Staff_FactoryCreateUpdateRequest factoryCreateUpdateRequest) {
        Optional<Factory> existFactory = factoryRepository.findById(factoryCreateUpdateRequest.getId());
        Optional<UserStaff> userStaff = staffFactoryExtendRepository.findById(factoryCreateUpdateRequest.getIdUserStaff());
        Optional<Project> project = projectFactoryExtendRepository.findById(factoryCreateUpdateRequest.getIdProject());
        if (existFactory.isPresent()) {
            Factory factory = existFactory.get();
            factory.setName(factoryCreateUpdateRequest.getFactoryName());
            factory.setDescription(factory.getDescription());
            factory.setUserStaff(userStaff.get());
            factory.setProject(project.get());
            factoryRepository.save(factory);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Sửa nhóm xưởng  thành công",
                            factory
                    ),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Nhóm xưởng không tồn tại",
                        null
                ),
                HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<?> changeStatus(String factoryId) {
        Optional<Factory> existFactory = factoryRepository.findById(factoryId);
        if (existFactory.isPresent()) {
            Factory factory = existFactory.get();
            factory.setStatus(existFactory.get().getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
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
                            existFactory
                    ),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Nhóm xưởng không tồn tại",
                        null
                ),
                HttpStatus.NOT_FOUND);
    }

}
