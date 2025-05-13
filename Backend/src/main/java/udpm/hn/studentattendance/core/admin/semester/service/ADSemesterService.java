package udpm.hn.studentattendance.core.admin.semester.service;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.semester.model.request.ADCreateUpdateSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.model.request.ADSemesterRequest;

public interface ADSemesterService {
    ResponseEntity<?> getAllSemester(ADSemesterRequest request);

    ResponseEntity<?> getSemesterById(String semesterId);

    ResponseEntity<?> createSemester(@Valid ADCreateUpdateSemesterRequest request);

    ResponseEntity<?> updateSemester(@Valid ADCreateUpdateSemesterRequest request);

    ResponseEntity<?> changeStatusSemester(String semesterID);

}
