package udpm.hn.studentattendance.core.staff.project.service.ipml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.project.model.response.USSemesterResponse;
import udpm.hn.studentattendance.core.staff.project.repository.Staff_SemesterManagementRepository;

import java.util.List;

@Service
public class Staff_SemesterManagementService {

    @Autowired
    private Staff_SemesterManagementRepository repository;

    public List<USSemesterResponse> getComboboxSemester(String facilityId) {
        return repository.getSemesters();

    }
}
