package udpm.hn.studentattendance.core.staff.project.service.ipml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.project.model.response.Staff_LevelProjectResponse;
import udpm.hn.studentattendance.core.staff.project.repository.Staff_LevelProjectManagementRepository;

import java.util.List;

@Service
public class Staff_LevelProjectManagementService {

    @Autowired
    private Staff_LevelProjectManagementRepository repository;

    public List<Staff_LevelProjectResponse> getComboboxLevelProject() {
        return repository.getLevelProject();
    }
}
