package udpm.hn.studentattendance.core.admin.project.service.ipml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.project.model.response.LevelProjectResponse;
import udpm.hn.studentattendance.core.admin.project.repository.AdminLevelProjectManagementRepository;

import java.util.List;

@Service
public class AdminLevelProjectManagementService {

    @Autowired
    private AdminLevelProjectManagementRepository repository;

    public List<LevelProjectResponse> getComboboxLevelProject() {
        return repository.getLevelProject();
    }
}
