package udpm.hn.studentattendance.core.staff.project.service.ipml;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.project.model.response.USLevelProjectResponse;
import udpm.hn.studentattendance.core.staff.project.repository.STLevelProjectExtendRepository;
import udpm.hn.studentattendance.core.staff.project.service.STLevelProjectManagementService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class STLevelProjectManagementServiceImpl implements STLevelProjectManagementService {

    private final STLevelProjectExtendRepository repository;

    public List<USLevelProjectResponse> getComboboxLevelProject() {
        return repository.getLevelProject();
    }

}
