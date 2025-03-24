package udpm.hn.studentattendance.core.teacher.teaching_schedule.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.request.Teacher_TSPlanDateUpdateRequest;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.request.Teacher_TeachingScheduleRequest;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.response.Teacher_TSDetailPlanDateResponse;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.repository.Teacher_TSFactoryExtendRepository;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.repository.Teacher_TSProjectExtendRepository;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.repository.Teacher_TSSubjectExtendRepository;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.repository.Teacher_TeachingScheduleExtendRepository;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.service.Teacher_TeachingScheduleService;
import udpm.hn.studentattendance.entities.*;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class Teacher_TeachingScheduleServiceImpl implements Teacher_TeachingScheduleService {

    private final Teacher_TeachingScheduleExtendRepository teacherTeachingScheduleExtendRepository;

    private final Teacher_TSProjectExtendRepository teacherTsProjectExtendRepository;

    private final Teacher_TSSubjectExtendRepository teacherTsSubjectExtendRepository;

    private final Teacher_TSFactoryExtendRepository teacherTsFactoryExtendRepository;

    private final SessionHelper sessionHelper;

    @Override
    public ResponseEntity<?> getAllTeachingScheduleByStaff(Teacher_TeachingScheduleRequest teachingScheduleRequest) {
        Pageable pageable = PaginationHelper.createPageable(teachingScheduleRequest);
        PageableObject list = PageableObject.of
                (teacherTeachingScheduleExtendRepository.getAllTeachingScheduleByStaff
                        (sessionHelper.getUserId(), pageable, teachingScheduleRequest));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả lịch dạy của " + sessionHelper.getUserId() + "thành công",
                        list
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllFactoryByStaff() {
        List<Factory> factories = teacherTsFactoryExtendRepository.getAllFactoryByStaff(sessionHelper.getUserId(), EntityStatus.ACTIVE);
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả nhóm xửng của " + sessionHelper.getUserId() + " dạy thành công",
                        factories
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllProjectByStaff() {
        List<Project> projects = teacherTsProjectExtendRepository.getAllProject(sessionHelper.getUserId(), EntityStatus.ACTIVE);
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả dự án đang dạy của " + sessionHelper.getUserId() + " thành công",
                        projects
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllSubjectByStaff() {
        List<Subject> subjects = teacherTsSubjectExtendRepository.getAllSubjectByStaff(sessionHelper.getUserId(), EntityStatus.ACTIVE);
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả môn học của " + sessionHelper.getUserId() + " thành công",
                        subjects
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllShift() {
        List<PlanDate> shifts = teacherTeachingScheduleExtendRepository.getAllShift();
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả ca học của " + sessionHelper.getUserId() + "thành công",
                        shifts
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> updatePlanDate(Teacher_TSPlanDateUpdateRequest planDateUpdateRequest) {
        Optional<PlanDate> existPlanDate = teacherTeachingScheduleExtendRepository.findById(planDateUpdateRequest.getIdPlanDate());
        if (existPlanDate.isPresent()) {
            PlanDate planDate = existPlanDate.get();
            planDate.setDescription(planDateUpdateRequest.getDescription());
            planDate.setLateArrival(planDateUpdateRequest.getLateArrival());
            teacherTeachingScheduleExtendRepository.save(planDate);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Cập nhật buổi học thành công",
                            planDate
                    ),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Kế hoạch không tồn tại",
                        null
                ),
                HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> getDetailPlanDate(String planDateId) {
        Optional<Teacher_TSDetailPlanDateResponse> getDetailPlanDateResponse =
                teacherTeachingScheduleExtendRepository.getPlanDateById(planDateId);
        if (getDetailPlanDateResponse.isPresent()) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Lấy chi tiết kế hoạch thành công",
                            getDetailPlanDateResponse
                    ), HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Kế hoạch không tồn tại",
                        null
                ),
                HttpStatus.BAD_REQUEST);
    }
}
