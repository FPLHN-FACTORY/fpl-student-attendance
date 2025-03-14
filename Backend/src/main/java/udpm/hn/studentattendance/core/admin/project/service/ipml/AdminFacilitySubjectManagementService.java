package udpm.hn.studentattendance.core.admin.project.service.ipml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.project.model.response.FacilityResponse;
import udpm.hn.studentattendance.core.admin.project.model.response.SubjectResponse;
import udpm.hn.studentattendance.core.admin.project.repository.AdminFacilityBySubjectRepository;
import udpm.hn.studentattendance.core.admin.project.repository.AdminSubjectManagementRepository;

import java.util.List;

@Service
public class AdminFacilitySubjectManagementService {

    @Autowired
    private AdminFacilityBySubjectRepository repository;

    public List<FacilityResponse> getComboboxFacility(String subjectId) {
        return repository.getFacility(subjectId);
    }

}
