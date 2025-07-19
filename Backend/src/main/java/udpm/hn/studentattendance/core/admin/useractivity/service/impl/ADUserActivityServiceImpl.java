package udpm.hn.studentattendance.core.admin.useractivity.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.useractivity.repository.ADUserActivityFilterExtendRepository;
import udpm.hn.studentattendance.core.admin.useractivity.service.ADUserActivityService;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.model.request.UALFilterRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Service
@RequiredArgsConstructor
public class ADUserActivityServiceImpl implements ADUserActivityService {

    private final UserActivityLogHelper userActivityLogHelper;

    private final ADUserActivityFilterExtendRepository userActivityFilterExtendRepository;


    @Override
    public ResponseEntity<?> getAllUserActivity(UALFilterRequest request) {
        return RouterHelper.responseSuccess("Lấy danh sách hành động của người dùng thành công", userActivityLogHelper.getAll(request));
    }

    @Override
    public ResponseEntity<?> getAllFacility() {
        return RouterHelper.responseSuccess("Lấy tất cả cơ sở thành công", userActivityFilterExtendRepository.getAllFacility(EntityStatus.ACTIVE));
    }

    @Override
    public ResponseEntity<?> getAllUserAdmin() {
        return RouterHelper.responseSuccess("Lấy tất cả cơ sở thành công", userActivityFilterExtendRepository.getAllUserAdmin(EntityStatus.ACTIVE));
    }

    @Override
    public ResponseEntity<?> getAllUserStaff() {
        return RouterHelper.responseSuccess("Lấy tất cả phụ trách xưởng thành công", userActivityFilterExtendRepository.getAllUserStaff(EntityStatus.ACTIVE));
    }
}
