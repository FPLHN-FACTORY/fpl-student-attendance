package udpm.hn.studentattendance.core.staff.project.service.ipml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.project.model.response.Staff_SubjectResponse;
import udpm.hn.studentattendance.core.staff.project.repository.Staff_SubjectManagementRepository;

import java.util.List;

@Service
public class Staff_SubjectFacilityManagementService {

    @Autowired
    private Staff_SubjectManagementRepository repository;

    public List<Staff_SubjectResponse> getComboboxSubjectFacility(String facilityId) {
        return repository.getSubjectFacility(facilityId);
    }

}
