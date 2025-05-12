package udpm.hn.studentattendance.core.staff.plan.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateAttendanceRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDModifyPlanDateAttendanceRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateAttendanceResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateStudentResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanResponse;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDAttendanceRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanDateRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDUserStudentRepository;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanDateAttendanceService;
import udpm.hn.studentattendance.entities.Attendance;
import udpm.hn.studentattendance.entities.Plan;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.utils.DateTimeUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SPDPlanDateAttendanceServiceImpl implements SPDPlanDateAttendanceService {

    private final SessionHelper sessionHelper;

    private final SPDAttendanceRepository spdAttendanceRepository;

    private final SPDPlanDateRepository spdPlanDateRepository;

    private final SPDUserStudentRepository spdUserStudentRepository;

    @Override
    public ResponseEntity<?> getDetail(String idPlanDate) {
        Optional<SPDPlanDateAttendanceResponse> data = spdAttendanceRepository.getDetailPlanDate(idPlanDate, sessionHelper.getFacilityId());
        return data
                .map(spdPlanDateResponse -> RouterHelper.responseSuccess("Get dữ liệu thành công", spdPlanDateResponse))
                .orElseGet(() -> RouterHelper.responseError("Không tìm thấy kế hoạch"));
    }

    @Override
    public ResponseEntity<?> getAllList(SPDFilterPlanDateAttendanceRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<SPDPlanDateStudentResponse> data = PageableObject.of(spdAttendanceRepository.getAllByFilter(pageable, request));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> changeStatus(SPDModifyPlanDateAttendanceRequest request) {
        PlanDate planDate = spdPlanDateRepository.findById(request.getIdPlanDate()).orElse(null);
        if (planDate == null || planDate.getStartDate() > DateTimeUtils.getCurrentTimeMillis()) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch");
        }

        System.out.println(request.getIdUserStudent());
        UserStudent userStudent = spdUserStudentRepository.findById(request.getIdUserStudent()).orElse(null);
        if (userStudent == null || userStudent.getStatus() == EntityStatus.INACTIVE) {
            return RouterHelper.responseError("Không tìm thấy sinh viên");
        }

        Attendance attendance = spdAttendanceRepository.findByPlanDate_IdAndUserStudent_Id(planDate.getId(), userStudent.getId()).orElse(null);
        if (attendance == null) {
            attendance = new Attendance();
            attendance.setPlanDate(planDate);
            attendance.setUserStudent(userStudent);
        } else {
            if (attendance.getAttendanceStatus() == AttendanceStatus.PRESENT) {
                return RouterHelper.responseError("Không thể thay đổi trạng thái điểm danh này");
            }
        }

        attendance.setAttendanceStatus(AttendanceStatus.PRESENT);

        Attendance newEntity = spdAttendanceRepository.save(attendance);
        return RouterHelper.responseSuccess("Thay đổi trạng thái điểm danh thành công", newEntity);
    }

}
