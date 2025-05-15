package udpm.hn.studentattendance.core.staff.project.service;

import udpm.hn.studentattendance.core.staff.project.model.response.USSemesterResponse;
import udpm.hn.studentattendance.entities.Semester;

import java.util.List;

public interface STSemesterManagementService {

    List<USSemesterResponse> getComboboxSemester();

    List<Semester> getSemester();

}
