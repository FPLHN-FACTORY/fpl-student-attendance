package udpm.hn.studentattendance.core.admin.semester.service;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.semester.model.request.CreateUpdateSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.model.request.SemesterRequest;

public interface AdSemesterService {
    ResponseEntity<?> getAllSemester(SemesterRequest request);

    ResponseEntity<?> getSemesterById(String semesterId);

    ResponseEntity<?> createSemester(@Valid CreateUpdateSemesterRequest request);

    ResponseEntity<?> updateSemester(@Valid CreateUpdateSemesterRequest request);

    ResponseEntity<?> changeStatusSemester(String semesterID);

}
