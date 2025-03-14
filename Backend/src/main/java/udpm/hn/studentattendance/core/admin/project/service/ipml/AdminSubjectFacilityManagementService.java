package udpm.hn.studentattendance.core.admin.project.service.ipml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.project.model.response.SubjectResponse;
import udpm.hn.studentattendance.core.admin.project.repository.AdminSubjectManagementRepository;

import java.util.List;

@Service
public class AdminSubjectFacilityManagementService {

    @Autowired
    private AdminSubjectManagementRepository repository;

    public List<SubjectResponse> getComboboxSubjectFacility(String facilityId) {
        return repository.getSubjectFacility(facilityId);
    }

    public List<SubjectResponse> getComboboxSubjectAdd() {
        return repository.getSubjectFacilityAdd();
    }



}
