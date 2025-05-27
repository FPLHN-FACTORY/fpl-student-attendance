package udpm.hn.studentattendance.core.staff.attendancerecovery.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STAttendanceRecoveryDeleteRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STAttendanceRecoveryRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.service.STAttendanceRecoveryService;
@Service
@RequiredArgsConstructor
@Validated
public class STAttendanceRecoveryServiceImpl implements STAttendanceRecoveryService {

    @Override
    public ResponseEntity<?> getListAttendanceRecovery(STAttendanceRecoveryRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<?> deleteAttendanceRecovery(STAttendanceRecoveryDeleteRequest recoveryDeleteRequest) {
        return null;
    }
}
