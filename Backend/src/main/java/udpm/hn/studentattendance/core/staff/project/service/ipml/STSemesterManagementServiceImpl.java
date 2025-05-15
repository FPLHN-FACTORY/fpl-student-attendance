package udpm.hn.studentattendance.core.staff.project.service.ipml;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.project.model.response.USSemesterResponse;
import udpm.hn.studentattendance.core.staff.project.repository.STProjectSemesterExtendRepository;
import udpm.hn.studentattendance.core.staff.project.service.STSemesterManagementService;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
public class STSemesterManagementServiceImpl implements STSemesterManagementService {

    private final STProjectSemesterExtendRepository repository;

    public List<USSemesterResponse> getComboboxSemester() {
        return repository.getSemesters();
    }

    public List<Semester> getSemester() {
        return repository.getAllSemester(EntityStatus.ACTIVE);
    }

}
