package udpm.hn.studentattendance.core.teacher.factory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFilterPlanDateAttendanceRequest;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCPlanDateAttendanceResponse;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCPlanDateStudentResponse;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCAttendanceRepository;
import udpm.hn.studentattendance.core.teacher.factory.service.TCPlanDateAttendanceService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;


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
