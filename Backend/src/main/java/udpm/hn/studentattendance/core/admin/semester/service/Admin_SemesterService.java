package udpm.hn.studentattendance.core.admin.semester.service;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.semester.model.request.Admin_CreateUpdateSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.model.request.Admin_SemesterRequest;

public interface Admin_SemesterService {
    ResponseEntity<?> getAllSemester(Admin_SemesterRequest request);

    ResponseEntity<?> getSemesterById(String semesterId);

    ResponseEntity<?> createSemester(@Valid Admin_CreateUpdateSemesterRequest request);

    ResponseEntity<?> updateSemester(@Valid Admin_CreateUpdateSemesterRequest request);

    ResponseEntity<?> changeStatusSemester(String semesterID);

}
