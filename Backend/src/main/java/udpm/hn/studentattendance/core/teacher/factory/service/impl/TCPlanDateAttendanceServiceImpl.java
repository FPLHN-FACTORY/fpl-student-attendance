package udpm.hn.studentattendance.core.teacher.factory.service.impl;

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
import udpm.hn.studentattendance.core.student.schedule.repository.STDScheduleAttendanceRepository;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFilterPlanDateAttendanceRequest;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCPlanDateAttendanceResponse;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCPlanDateStudentResponse;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCAttendanceRepository;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCPlanDateRepository;
import udpm.hn.studentattendance.core.teacher.factory.service.TCPlanDateAttendanceService;
import udpm.hn.studentattendance.entities.Attendance;
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
public class TCPlanDateAttendanceServiceImpl implements TCPlanDateAttendanceService {

    private final SessionHelper sessionHelper;

    private final TCAttendanceRepository tcAttendanceRepository;

    @Override
    public ResponseEntity<?> getDetail(String idPlanDate) {
        Optional<TCPlanDateAttendanceResponse> data = tcAttendanceRepository.getDetailPlanDate(idPlanDate, sessionHelper.getFacilityId());
        return data
                .map(response -> RouterHelper.responseSuccess("Get dữ liệu thành công", response))
                .orElseGet(() -> RouterHelper.responseError("Không tìm thấy kế hoạch"));
    }

    @Override
    public ResponseEntity<?> getAllList(TCFilterPlanDateAttendanceRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<TCPlanDateStudentResponse> data = PageableObject.of(tcAttendanceRepository.getAllByFilter(pageable, request));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

}
