package udpm.hn.studentattendance.core.admin.adminprojectmanagement.service.ipml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.adminprojectmanagement.model.response.SemesterResponse;
import udpm.hn.studentattendance.core.admin.adminprojectmanagement.repository.StaffSemesterManagementRepository;

import java.util.List;

@Service
public class StaffSemesterManagementService {

    @Autowired
    private StaffSemesterManagementRepository repository;

    public List<SemesterResponse> getComboboxSemester(String facilityId) {
        return repository.getSemesters(facilityId);

    }
}
