package udpm.hn.studentattendance.core.admin.semester.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.admin.semester.model.request.AdCreateUpdateSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.model.request.AdSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.repository.AdSemesterRepository;
import udpm.hn.studentattendance.core.admin.semester.service.AdSemesterService;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.constants.SemesterName;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class AdSemesterServiceImpl implements AdSemesterService {
    private final AdSemesterRepository adSemesterRepository;

    @Override
    public ResponseEntity<?> getAllSemester(AdSemesterRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request, "createdAt");
        PageableObject pageableObject = PageableObject.of(adSemesterRepository.getAllSemester(pageable, request));
        return new ResponseEntity<>(new ApiResponse(
                RestApiStatus.SUCCESS,
                "Get semester successfully",
                pageableObject
        ), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getSemesterById(String semesterId) {
        return adSemesterRepository.getDetailSemesterById(semesterId)
                .map(semester -> new ResponseEntity<>(
                        new ApiResponse(
                                RestApiStatus.SUCCESS,
                                "Get semester successfully",
                                semester
                        ),
                        HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(
                        new ApiResponse(
                                RestApiStatus.ERROR,
                                "Semester does not exist",
                                null
                        ),
                        HttpStatus.NOT_FOUND));
    }

    @Override
    public ResponseEntity<?> createSemester(@Valid AdCreateUpdateSemesterRequest request) {
        try {
            // fomat từ epoch milli giây sang Instant convert thành giờ của máy chủ
            LocalDateTime fromDate = Instant
                    .ofEpochMilli(request.getFromDate())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            // fomat từ epoch milli giây sang Instant convert thành giờ của máy chủ
            LocalDateTime toDate = Instant
                    .ofEpochMilli(request.getToDate())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            // lấy ra năm trên máy
            Integer yearStartTime = fromDate.getYear();
            Integer yearEndTime = toDate.getYear();
            // quy chuẩn lại tên kỳ học : Spring, Summer, Fall
            String name = SemesterName.valueOf(request.getSemesterName()).toString().trim();
            // lấy thời gian bắt đầu và kêt thúc học kỳ
            Long fromTimeSemester = request.getStartTimeCustom();
            Long toTimeSemester = request.getEndTimeCustom();
            if (!yearStartTime.equals(yearEndTime)) {
                return new ResponseEntity<>(
                        new ApiResponse(
                                RestApiStatus.ERROR,
                                "Start date and dnd date must be the same year",
                                null),
                        HttpStatus.BAD_REQUEST);
            }
            if (!adSemesterRepository.checkConflictTime(fromTimeSemester, toTimeSemester).isEmpty()) {
                return new ResponseEntity<>(
                        new ApiResponse(
                                RestApiStatus.WARNING,
                                "There were semesters during this period!",
                                null
                        ),
                        HttpStatus.CONFLICT);
            }
            Optional<Semester> existSemester = adSemesterRepository.checkSemesterExistNameAndYear(name, yearStartTime);
            if (existSemester.isPresent()) {
                return new ResponseEntity<>(
                        new ApiResponse(
                                RestApiStatus.WARNING,
                                "Semester already exist",
                                null
                        ),
                        HttpStatus.CONFLICT);
            }
            Semester semester = new Semester();
            semester.setSemesterName(SemesterName.valueOf(name));
            semester.setYear(yearStartTime);
            semester.setFromDate(fromTimeSemester);
            semester.setToDate(toTimeSemester);
            semester.setStatus(EntityStatus.ACTIVE);
            Semester semesterSave = adSemesterRepository.save(semester);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Created semester successfully",
                            semesterSave
                    ),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Created semester failed",
                            null
                    ),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> updateSemester(@Valid AdCreateUpdateSemesterRequest request) {
        // fomat từ epoch milli giây sang Instant convert thành giờ của máy chủ
        LocalDateTime fromDate = Instant
                .ofEpochMilli(request.getFromDate())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        // fomat từ epoch milli giây sang Instant convert thành giờ của máy chủ
        LocalDateTime toDate = Instant
                .ofEpochMilli(request.getToDate())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        // lấy ra năm trên máy
        Integer yearStartTime = fromDate.getYear();
        Integer yearEndTime = toDate.getYear();
        // quy chuẩn lại tên kỳ học : Spring, Summer, Fall
        String name = SemesterName.valueOf(request.getSemesterName()).toString().trim();
        // lấy thời gian bắt đầu và kêt thúc học kỳ
        Long fromTimeSemester = request.getStartTimeCustom();
        Long toTimeSemester = request.getEndTimeCustom();

        Optional<Semester> existSemester = adSemesterRepository.findById(request.getSemesterId());
        if (existSemester.isPresent()) {

            Semester semester = existSemester.get();

            List<Semester> semesters = adSemesterRepository.checkConflictTime(fromTimeSemester, toTimeSemester);
            if (!semesters.isEmpty()) {
                return new ResponseEntity<>(
                        new ApiResponse(
                                RestApiStatus.WARNING,
                                "There were semesters during this period!",
                                null
                        ),
                        HttpStatus.CONFLICT);
            }
            if (!yearStartTime.equals(yearEndTime)) {
                return new ResponseEntity<>(
                        new ApiResponse(
                                RestApiStatus.ERROR,
                                "Start date and dnd date must be the same year",
                                null),
                        HttpStatus.BAD_REQUEST);
            }
            semester.setSemesterName(SemesterName.valueOf(name));
            semester.setYear(yearStartTime);
            semester.setFromDate(fromTimeSemester);
            semester.setToDate(toTimeSemester);
            Semester semesterSave = adSemesterRepository.save(semester);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Updated semester Succesfully",
                            semesterSave
                    ),
                    HttpStatus.CREATED);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Semester does not exist",
                        null
                ),
                HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<?> changeStatusSemester(String semesterID) {
        Optional<Semester> existSemester = adSemesterRepository.findById(semesterID);
        if (existSemester.isPresent()) {
            Semester semester = existSemester.get();
            if (semester.getStatus().equals(EntityStatus.ACTIVE)) {
                semester.setStatus(EntityStatus.INACTIVE);
            } else {
                semester.setStatus(EntityStatus.ACTIVE);
            }
            adSemesterRepository.save(semester);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Changed semester status successfully",
                            null
                    ),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Semester does not exist",
                        null
                ),
                HttpStatus.NOT_FOUND);
    }
}
