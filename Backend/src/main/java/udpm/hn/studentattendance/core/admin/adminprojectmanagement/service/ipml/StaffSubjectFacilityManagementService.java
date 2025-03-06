package udpm.hn.studentattendance.core.admin.adminprojectmanagement.service.ipml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.adminprojectmanagement.model.response.SubjectResponse;
import udpm.hn.studentattendance.core.admin.adminprojectmanagement.repository.StaffSubjectManagementRepository;

import java.util.List;

@Service
public class StaffSubjectFacilityManagementService {

    @Autowired
    private StaffSubjectManagementRepository repository;

    public List<SubjectResponse> getComboboxSubjectFacility(String facilityId) {
        return repository.getSubjectFacility(facilityId);
    }

}
