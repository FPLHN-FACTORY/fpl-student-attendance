package udpm.hn.studentattendance.core.staff.project.service.ipml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.project.model.response.USLevelProjectResponse;
import udpm.hn.studentattendance.core.staff.project.repository.STLevelProjectExtendRepository;

import java.util.List;

@Service
public class STLevelProjectManagementService {

    @Autowired
    private STLevelProjectExtendRepository repository;

    public List<USLevelProjectResponse> getComboboxLevelProject() {
        return repository.getLevelProject();
    }
}
