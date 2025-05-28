package udpm.hn.studentattendance.core.staff.project.service.ipml;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.project.model.response.USSubjectResponse;
import udpm.hn.studentattendance.core.staff.project.repository.STProjectSubjectFacilityExtendRepository;
import udpm.hn.studentattendance.core.staff.project.service.STSubjectFacilityManagementService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class STSubjectFacilityManagementServiceImpl implements STSubjectFacilityManagementService {

    private final STProjectSubjectFacilityExtendRepository repository;

    public List<USSubjectResponse> getComboboxSubjectFacility(String facilityId) {
        return repository.getSubjectFacility(facilityId);
    }

}
