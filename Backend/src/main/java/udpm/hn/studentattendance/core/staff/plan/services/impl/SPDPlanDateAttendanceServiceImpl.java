package udpm.hn.studentattendance.core.staff.plan.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateAttendanceRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDModifyPlanDateAttendanceRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateAttendanceResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateStudentResponse;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDAttendanceRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanDateRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDUserStudentRepository;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanDateAttendanceService;
import udpm.hn.studentattendance.entities.Attendance;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;
import udpm.hn.studentattendance.utils.DateTimeUtils;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SPDPlanDateAttendanceServiceImpl implements SPDPlanDateAttendanceService {

    private final SessionHelper sessionHelper;

    private final SPDAttendanceRepository spdAttendanceRepository;

    private final SPDPlanDateRepository spdPlanDateRepository;

    private final SPDUserStudentRepository spdUserStudentRepository;

    private final UserActivityLogHelper userActivityLogHelper;

    @Override
    public ResponseEntity<?> getDetail(String idPlanDate) {
        Optional<SPDPlanDateAttendanceResponse> data = spdAttendanceRepository.getDetailPlanDate(idPlanDate,
                sessionHelper.getFacilityId());
        return data
                .map(spdPlanDateResponse -> RouterHelper.responseSuccess("Get dữ liệu thành công", spdPlanDateResponse))
                .orElseGet(() -> RouterHelper.responseError("Không tìm thấy kế hoạch"));
    }

    @Override
    public ResponseEntity<?> getAllList(SPDFilterPlanDateAttendanceRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<SPDPlanDateStudentResponse> data = PageableObject
                .of(spdAttendanceRepository.getAllByFilter(pageable, request));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> changeStatus(SPDModifyPlanDateAttendanceRequest request) {
        if (request.getStatus() == null) {
            return RouterHelper.responseError("Yêu cầu trạng thái không hợp lệ");
        }

        PlanDate planDate = spdPlanDateRepository.findById(request.getIdPlanDate()).orElse(null);
        if (planDate == null || planDate.getStartDate() > DateTimeUtils.getCurrentTimeMillis()) {
            return RouterHelper.responseError("Không tìm thấy kế hoạch");
        }

        UserStudent userStudent = spdUserStudentRepository.findById(request.getIdUserStudent()).orElse(null);
        if (userStudent == null || userStudent.getStatus() == EntityStatus.INACTIVE) {
            return RouterHelper.responseError("Không tìm thấy sinh viên");
        }

        boolean isEnableCheckin = planDate.getRequiredCheckin() == StatusType.ENABLE;
        boolean isEnableCheckout = planDate.getRequiredCheckout() == StatusType.ENABLE;

        Attendance attendance = spdAttendanceRepository
                .findByPlanDate_IdAndUserStudent_Id(planDate.getId(), userStudent.getId()).orElse(null);
        if (attendance == null) {
            attendance = new Attendance();
            attendance.setPlanDate(planDate);
            attendance.setUserStudent(userStudent);
        } else {
            if (attendance.getAttendanceStatus() == AttendanceStatus.PRESENT) {
                return RouterHelper.responseError("Không thể thay đổi trạng thái điểm danh này");
            }
        }

        AttendanceStatus status = AttendanceStatus.PRESENT;
        String message = "Thay đổi trạng thái điểm danh thành công";

        if (request.getStatus() == 0) {
            status = !isEnableCheckout ? AttendanceStatus.PRESENT : AttendanceStatus.CHECKIN;
            message = "Thay đổi trạng thái checkin thành công";
        } else if (request.getStatus() == 1) {
            if (isEnableCheckin && attendance.getAttendanceStatus() != AttendanceStatus.CHECKIN) {
                return RouterHelper.responseError("Không thể checkout khi chưa checkin");
            }
            message = "Thay đổi trạng thái checkout thành công";
        }

        if (!isEnableCheckin && !isEnableCheckout) {
            status = AttendanceStatus.PRESENT;
            message = "Thay đổi trạng thái điểm danh thành công";
        }

        attendance.setAttendanceStatus(status);

        AttendanceStatus oldStatus = attendance.getAttendanceStatus();

        attendance.setAttendanceStatus(status);
        Attendance newEntity = spdAttendanceRepository.save(attendance);

        String logMessage = String.format(
                "vừa thay đổi trạng thái điểm danh của sinh viên '%s' (Mã: %s) " +
                        "từ '%s' thành '%s' - " +
                        "Kế hoạch: %s, Nhóm xưởng: %s, " +
                        "Thời gian: %s",
                userStudent.getName(), userStudent.getCode(),
                oldStatus != null ? oldStatus.name() : "CHƯA CÓ",
                status.name(),
                planDate.getPlanFactory().getPlan().getName(),
                planDate.getPlanFactory().getFactory().getName(),
                DateTimeUtils.convertMillisToDate(planDate.getStartDate(), "dd/MM/yyyy HH:mm"));
        userActivityLogHelper.saveLog(logMessage);
        return RouterHelper.responseSuccess(message + " sinh viên " + userStudent.getName(), newEntity);
    }

}
