package udpm.hn.studentattendance.core.staff.attendancerecovery.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STAttendanceRecoveryRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STCreateOrUpdateNewEventRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.repository.STAttendanceRecoveryFacilityRepository;
import udpm.hn.studentattendance.core.staff.attendancerecovery.repository.STAttendanceRecoveryRepository;
import udpm.hn.studentattendance.core.staff.attendancerecovery.repository.STAttendanceRecoverySemesterRepository;
import udpm.hn.studentattendance.core.staff.attendancerecovery.service.STAttendanceRecoveryService;
import udpm.hn.studentattendance.entities.AttendanceRecovery;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class STAttendanceRecoveryServiceImpl implements STAttendanceRecoveryService {

    private final STAttendanceRecoveryRepository attendanceRecoveryRepository;

    private final STAttendanceRecoverySemesterRepository semesterRepository;

    private final STAttendanceRecoveryFacilityRepository facilityRepository;

    private final SessionHelper sessionHelper;

    @Override
    public ResponseEntity<?> getListAttendanceRecovery(STAttendanceRecoveryRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject list = PageableObject.of
                (attendanceRecoveryRepository.getListAttendanceRecovery(request, sessionHelper.getFacilityId(),pageable));
        return RouterHelper.responseSuccess("Lấy danh sách sự kiện thành công", list);
    }

    @Override
    public ResponseEntity<?> deleteAttendanceRecovery(String attendanceRecoveryId) {
        return null;
    }

    @Override
    public ResponseEntity<?> getAllSemester() {
        List<Semester> semesters = semesterRepository.getAllSemester(EntityStatus.ACTIVE);
        return RouterHelper.responseSuccess("Lấy tất cả học kỳ thành công", semesters);
    }

    @Override
    public ResponseEntity<?> createNewEventAttendanceRecovery(STCreateOrUpdateNewEventRequest request) {
        Optional<Facility> facilityOptional = facilityRepository.findById(sessionHelper.getFacilityId());
        if (facilityOptional == null){
            return RouterHelper.responseError("Cơ sở không tồn tại", null);
        }
        AttendanceRecovery attendanceRecovery = new AttendanceRecovery();
        attendanceRecovery.setName(request.getName());
        attendanceRecovery.setDescription(request.getDescription());
        attendanceRecovery.setDay(request.getDay());
        attendanceRecovery.setFacility(facilityOptional.get());
        AttendanceRecovery attendanceRecoverySave = attendanceRecoveryRepository.save(attendanceRecovery);

        return RouterHelper.responseSuccess("Thêm sự kiện khôi phục điểm danh mới thành công", attendanceRecoverySave);
    }

    @Override
    public ResponseEntity<?> getDetailEventAttendanceRecovery(String idEventAttendanceRecovery) {
        Optional<AttendanceRecovery> attendanceRecoveryOptional = attendanceRecoveryRepository.findById(idEventAttendanceRecovery);
        if (attendanceRecoveryOptional.isPresent()){
            return RouterHelper.responseSuccess("Lấy chi tiết sự kiện khôi phục điểm danh thành công", attendanceRecoveryOptional);
        }
        return RouterHelper.responseError("Sự Kiện khôi phục điểm danh không tồn tại", null);
    }

    @Override
    public ResponseEntity<?> updateEventAttendanceRecovery(STCreateOrUpdateNewEventRequest request, String id) {
        Optional<AttendanceRecovery> attendanceRecoveryOptional = attendanceRecoveryRepository.findById(id);
        if (attendanceRecoveryOptional.isPresent()){
            AttendanceRecovery attendanceRecovery = attendanceRecoveryOptional.get();
        }
        return null;
    }
}
