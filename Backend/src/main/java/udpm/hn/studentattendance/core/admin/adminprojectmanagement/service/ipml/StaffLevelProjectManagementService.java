package udpm.hn.studentattendance.core.admin.adminprojectmanagement.service.ipml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.adminprojectmanagement.model.response.LevelProjectResponse;
import udpm.hn.studentattendance.core.admin.adminprojectmanagement.repository.StaffLevelProjectManagementRepository;

import java.util.List;

@Service
public class StaffLevelProjectManagementService {

    @Autowired
    private StaffLevelProjectManagementRepository repository;

    public List<LevelProjectResponse> getComboboxLevelProject() {
        return repository.getLevelProject();
    }
}
