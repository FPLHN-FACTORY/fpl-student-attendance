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
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.constants.SemesterName;
import udpm.hn.studentattendance.repositories.FacilityRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class AdSemesterServiceImpl implements AdSemesterService {
    private final AdSemesterRepository adSemesterRepository;

    private final FacilityRepository facilityRepository;

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
        return adSemesterRepository.findById(semesterId)
                .map(semester -> new ResponseEntity<>(
                        new ApiResponse(
                                RestApiStatus.SUCCESS,
                                "Tìm học kỳ thành công",
                                semester
                        ),
                        HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(
                        new ApiResponse(
                                RestApiStatus.ERROR,
                                "Học kỳ không tồn tại",
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
            // lấy thời gian bắt đầu và kêt thúc học kỳ
            Long fromTimeSemester = request.getStartTimeCustom();
            Long toTimeSemester = request.getEndTimeCustom();
            // lấy ra năm trên máy
            Integer yearStartTime = fromDate.getYear();
            Integer yearEndTime = toDate.getYear();
            // quy chuẩn lại tên kỳ học : Spring, Summer, Fall
            String name = SemesterName.valueOf(request.getSemesterName()).toString().trim();

            long monthsBetween = ChronoUnit.MONTHS.between(fromDate, toDate);
            if (monthsBetween < 3) {
                return new ResponseEntity<>(
                        new ApiResponse(
                                RestApiStatus.ERROR,
                                "Khoảng thời gian học kỳ phải tối thiểu 3 tháng",
                                null),
                        HttpStatus.BAD_REQUEST);
            }
            if (!yearStartTime.equals(yearEndTime)) {
                return new ResponseEntity<>(
                        new ApiResponse(
                                RestApiStatus.ERROR,
                                "Thời gian bắt đầu và kết thúc của học kỳ phải cùng 1 năm",
                                null),
                        HttpStatus.BAD_REQUEST);
            }
            if (!adSemesterRepository.checkConflictTime(fromTimeSemester, toTimeSemester).isEmpty()) {
                return new ResponseEntity<>(
                        new ApiResponse(
                                RestApiStatus.WARNING,
                                "Đã có học kỳ trong khoảng thời gian này!",
                                null
                        ),
                        HttpStatus.CONFLICT);
            }
            Optional<Semester> existSemester = adSemesterRepository.checkSemesterExistNameAndYear(name, yearStartTime, EntityStatus.ACTIVE);
            if (existSemester.isPresent()) {
                return new ResponseEntity<>(
                        new ApiResponse(
                                RestApiStatus.WARNING,
                                "Học kỳ đã tồn tại",
                                existSemester
                        ),
                        HttpStatus.CONFLICT);
            }
            Semester semester = new Semester();
            semester.setSemesterName(SemesterName.valueOf(name));
            semester.setCode(SemesterName.valueOf(name) + "-" + yearStartTime);
            semester.setYear(yearStartTime);
            semester.setFromDate(fromTimeSemester);
            semester.setToDate(toTimeSemester);
            semester.setStatus(EntityStatus.ACTIVE);
            for (Facility facility: facilityRepository.findAll()
                 ) {
                semester.setFacility(facility);
            }

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
        // tính năm
        long monthsBetween = ChronoUnit.MONTHS.between(fromDate, toDate);
        if (monthsBetween < 3) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Khoảng thời gian học kỳ phải tối thiểu 3 tháng",
                            null),
                    HttpStatus.BAD_REQUEST);
        }

        Optional<Semester> existSemester = adSemesterRepository.findById(request.getSemesterId());
        if (existSemester.isPresent()) {

            Semester semester = existSemester.get();

            List<Semester> semesters = adSemesterRepository.checkConflictTime(fromTimeSemester, toTimeSemester);
            if (!semesters.isEmpty()) {
                for (Semester s : semesters) {
                    if (!s.getId().equals(semester.getId())) {
                        return new ResponseEntity<>(
                                new ApiResponse(
                                        RestApiStatus.WARNING,
                                        "Đã có học kỳ trong khoảng thời gian này",
                                        null
                                ),
                                HttpStatus.CONFLICT);
                    }
                }
            }
            if (!yearStartTime.equals(yearEndTime)) {
                return new ResponseEntity<>(
                        new ApiResponse(
                                RestApiStatus.ERROR,
                                "Thời gian bắt đầu và kết thúc của học kỳ phải cùng 1 năm",
                                null),
                        HttpStatus.BAD_REQUEST);
            }
            semester.setSemesterName(SemesterName.valueOf(name));
            semester.setYear(yearStartTime);
            semester.setCode(SemesterName.valueOf(name) + "-" + yearStartTime);
            semester.setFromDate(fromTimeSemester);
            semester.setToDate(toTimeSemester);
            Semester semesterSave = adSemesterRepository.save(semester);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Cập nhật thành công",
                            semesterSave
                    ),
                    HttpStatus.CREATED);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Học kỳ không tồn tại",
                        existSemester
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
                            "Thay đổi trạng thái học kỳ thành công",
                            null
                    ),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Học kỳ không tồn tại",
                        null
                ),
                HttpStatus.NOT_FOUND);
    }
}
