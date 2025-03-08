package udpm.hn.studentattendance.core.staff.project.service.ipml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.project.model.response.LevelProjectResponse;
import udpm.hn.studentattendance.core.staff.project.repository.StaffLevelProjectManagementRepository;

import java.util.List;

@Service
public class StaffLevelProjectManagementService {

    @Autowired
    private StaffLevelProjectManagementRepository repository;

    public List<LevelProjectResponse> getComboboxLevelProject() {
        return repository.getLevelProject();
    }
}
