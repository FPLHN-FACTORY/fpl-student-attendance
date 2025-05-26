package udpm.hn.studentattendance.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.UserActivityLog;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.model.request.UALFilterRequest;
import udpm.hn.studentattendance.infrastructure.common.model.response.UALResponse;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserActivityLogRepository;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.repositories.FacilityRepository;

@Component
@RequiredArgsConstructor
public class UserActivityLogHelper {

    private final CommonUserActivityLogRepository commonUserActivityLogRepository;

    private final FacilityRepository facilityRepository;

    private final SessionHelper sessionHelper;

    public void saveLog(String message) {
        String idFacility = sessionHelper.getFacilityId();
        RoleConstant role = sessionHelper.getLoginRole();

        Facility facility = null;
        if (idFacility != null) {
            facility = facilityRepository.findById(idFacility).orElse(null);
        }

        UserActivityLog userActivityLog = new UserActivityLog();
        userActivityLog.setFacility(facility);
        userActivityLog.setMessage(message);
        userActivityLog.setRole(role);
        userActivityLog.setIdUser(sessionHelper.getUserId());

        commonUserActivityLogRepository.save(userActivityLog);
    }

    private PageableObject<UALResponse> getAll(UALFilterRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request);
        return PageableObject.of(commonUserActivityLogRepository.getListFilter(pageable, request));
    }

}
