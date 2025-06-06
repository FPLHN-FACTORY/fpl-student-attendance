package udpm.hn.studentattendance.core.staff.useractivity.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.useractivity.repository.STUserActivityFilterExtendRepository;
import udpm.hn.studentattendance.core.staff.useractivity.service.STUserActivityService;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.model.request.UALFilterRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Service
@RequiredArgsConstructor
public class STUserActivityServiceImpl implements STUserActivityService {

    private final UserActivityLogHelper userActivityLogHelper;

    private final STUserActivityFilterExtendRepository userActivityFilterExtendRepository;

    private final SessionHelper sessionHelper;

    @Override
    public ResponseEntity<?> getAllUserActivity(UALFilterRequest request) {
        request.setFacilityId(sessionHelper.getFacilityId());
        return RouterHelper.responseSuccess("Lấy danh sách hành động của người dùng thành công",
                userActivityLogHelper.getAll(request));
    }


    @Override
    public ResponseEntity<?> getAllUserStaff() {
        return RouterHelper.responseSuccess("Lấy tất cả phụ trách xưởng thành công",
                userActivityFilterExtendRepository.getAllUserStaff
                        (EntityStatus.ACTIVE, EntityStatus.ACTIVE, EntityStatus.ACTIVE, sessionHelper.getFacilityId()));
    }
}
