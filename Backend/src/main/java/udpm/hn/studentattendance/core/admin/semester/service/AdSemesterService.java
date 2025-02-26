package udpm.hn.studentattendance.core.admin.semester.service;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.semester.model.request.AdCreateUpdateSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.model.request.AdSemesterRequest;

public interface AdSemesterService {
    ResponseEntity<?> getAllSemester(AdSemesterRequest request);

    ResponseEntity<?> getSemesterById(String semesterId);

    ResponseEntity<?> createSemester(@Valid AdCreateUpdateSemesterRequest request);

    ResponseEntity<?> updateSemester(@Valid AdCreateUpdateSemesterRequest request);

    ResponseEntity<?> changeStatusSemester(String semesterID);

}
