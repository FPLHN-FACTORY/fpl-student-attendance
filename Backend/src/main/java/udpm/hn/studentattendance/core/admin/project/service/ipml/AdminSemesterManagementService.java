package udpm.hn.studentattendance.core.admin.project.service.ipml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.project.model.response.SemesterResponse;
import udpm.hn.studentattendance.core.admin.project.repository.AdminSemesterManagementRepository;

import java.util.List;

@Service
public class AdminSemesterManagementService {

    @Autowired
    private AdminSemesterManagementRepository repository;

    public List<SemesterResponse> getComboboxSemester(String facilityId) {
        return repository.getSemesters();

    }
}
