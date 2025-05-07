package udpm.hn.studentattendance.core.staff.project.service.ipml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.project.model.response.USSemesterResponse;
import udpm.hn.studentattendance.core.staff.project.repository.STProjectSemesterExtendRepository;

import java.util.List;

@Service
public class STSemesterManagementService {

    @Autowired
    private STProjectSemesterExtendRepository repository;

    public List<USSemesterResponse> getComboboxSemester() {
        return repository.getSemesters();

    }
}
