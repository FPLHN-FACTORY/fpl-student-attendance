package udpm.hn.studentattendance.core.staff.projectmanagement.service.ipml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.projectmanagement.model.response.LevelProjectResponse;
import udpm.hn.studentattendance.core.staff.projectmanagement.repository.StaffLevelProjectManagementRepository;

import java.util.List;

@Service
public class StaffLevelProjectManagementService {

    @Autowired
    private StaffLevelProjectManagementRepository repository;

    public List<LevelProjectResponse> getComboboxLevelProject() {
        return repository.getLevelProject();
    }
}
