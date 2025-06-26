package udpm.hn.studentattendance.core.admin.semester.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.admin.semester.model.request.ADCreateUpdateSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.model.request.ADSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.repository.ADSemesterRepository;
import udpm.hn.studentattendance.core.admin.semester.service.ADSemesterService;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.SemesterName;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class ADSemesterServiceImpl implements ADSemesterService {

    private final ADSemesterRepository adSemesterRepository;

    private final CommonUserStudentRepository commonUserStudentRepository;

    private final UserActivityLogHelper userActivityLogHelper;

    @Override
    public ResponseEntity<?> getAllSemester(ADSemesterRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request, "createdAt");
        PageableObject pageableObject = PageableObject
                .of(adSemesterRepository.getAllSemester(pageable, request));
        return RouterHelper.responseSuccess("Get semester successfully", pageableObject);
    }

    @Override
    public ResponseEntity<?> getSemesterById(String semesterId) {
        return adSemesterRepository.findById(semesterId)
                .map(semester -> RouterHelper.responseSuccess("Tìm học kỳ thành công", semester))
                .orElseGet(() -> RouterHelper.responseError("Học kỳ không tồn tại"));
    }

    @Override
    public ResponseEntity<?> createSemester(@Valid ADCreateUpdateSemesterRequest request) {
        try {

            LocalDateTime fromDate = Instant
                    .ofEpochMilli(request.getFromDate())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            LocalDateTime toDate = Instant
                    .ofEpochMilli(request.getToDate())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            if (fromDate.isBefore(LocalDateTime.now())) {
                return RouterHelper.responseError(
                        "Ngày bắt đầu học kỳ không thể là ngày quá khứ hoặc hiện tại");
            }

            Long fromTimeSemester = request.getStartTimeCustom();
            Long toTimeSemester = request.getEndTimeCustom();
            Integer yearStartTime = fromDate.getYear();
            Integer yearEndTime = toDate.getYear();
            String name = SemesterName.valueOf(request.getSemesterName()).toString().trim();

            long monthsBetween = ChronoUnit.MONTHS.between(fromDate, toDate);
            if (monthsBetween < 3) {
                return RouterHelper.responseError("Khoảng thời gian học kỳ phải tối thiểu 3 tháng");
            }
            if (!yearStartTime.equals(yearEndTime)) {
                return RouterHelper.responseError(
                        "Thời gian bắt đầu và kết thúc của học kỳ phải cùng 1 năm");
            }
            if (!adSemesterRepository.checkConflictTime(fromTimeSemester, toTimeSemester).isEmpty()) {
                return RouterHelper.responseError("Đã có học kỳ trong khoảng thời gian này!");
            }
            Optional<Semester> existSemester = adSemesterRepository.checkSemesterExistNameAndYear(name,
                    yearStartTime, EntityStatus.ACTIVE, null);
            if (existSemester.isPresent()) {
                return RouterHelper.responseError("Học kỳ đã tồn tại");
            }
            Semester semester = new Semester();
            semester.setSemesterName(SemesterName.valueOf(name));
            semester.setCode(SemesterName.valueOf(name) + "-" + yearStartTime);
            semester.setYear(yearStartTime);
            semester.setFromDate(fromTimeSemester);
            semester.setToDate(toTimeSemester);
            semester.setStatus(EntityStatus.ACTIVE);

            Semester semesterSave = adSemesterRepository.save(semester);
            userActivityLogHelper.saveLog("vừa thêm 1 học kỳ mới: " + semesterSave.getCode());
            return RouterHelper.responseSuccess("Created semester successfully", semesterSave);
        } catch (Exception e) {
            e.printStackTrace();
            return RouterHelper.responseError("Created semester failed");
        }
    }

    @Override
    public ResponseEntity<?> updateSemester(@Valid ADCreateUpdateSemesterRequest request) {
        Optional<Semester> existSemester = adSemesterRepository.findById(request.getSemesterId());
        if (existSemester.isEmpty()) {
            return RouterHelper.responseError("Không tìm thấy học kỳ");
        }

        Semester semester = existSemester.get();
        String oldCode = semester.getCode();

        LocalDateTime fromDate = Instant
                .ofEpochMilli(request.getFromDate())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        LocalDateTime toDate = Instant
                .ofEpochMilli(request.getToDate())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        Long fromTimeSemester = request.getStartTimeCustom();
        Long toTimeSemester = request.getEndTimeCustom();

        if (!Objects.equals(fromTimeSemester, semester.getFromDate())
                || !Objects.equals(toTimeSemester, semester.getToDate())) {
            if (fromDate.isBefore(LocalDateTime.now())) {
                return RouterHelper.responseError(
                        "Ngày bắt đầu học kỳ không thể là ngày trong quá khứ hoặc hiện tại");
            }
        }

        Integer yearStartTime = fromDate.getYear();
        Integer yearEndTime = toDate.getYear();
        String name = SemesterName.valueOf(request.getSemesterName()).toString().trim();

        long monthsBetween = ChronoUnit.MONTHS.between(fromDate, toDate);
        if (monthsBetween < 3) {
            return RouterHelper.responseError("Khoảng thời gian học kỳ phải tối thiểu 3 tháng");
        }

        Optional<Semester> existNameSemester = adSemesterRepository.checkSemesterExistNameAndYear(name,
                yearStartTime, EntityStatus.ACTIVE, semester.getId());
        if (existNameSemester.isPresent()) {
            return RouterHelper.responseError("Học kỳ đã tồn tại");
        }

        List<Semester> semesters = adSemesterRepository.checkConflictTime(fromTimeSemester,
                toTimeSemester);
        if (!semesters.isEmpty()) {
            for (Semester s : semesters) {
                if (!s.getId().equals(semester.getId())) {
                    return RouterHelper.responseError("Đã có học kỳ trong khoảng thời gian này");
                }
            }
        }
        if (!yearStartTime.equals(yearEndTime)) {
            return RouterHelper.responseError("Thời gian bắt đầu và kết thúc của học kỳ phải cùng 1 năm");
        }

        LocalDateTime oldFromDate = Instant
                .ofEpochMilli(existSemester.get().getFromDate())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        LocalDateTime oldToDate = Instant
                .ofEpochMilli(existSemester.get().getToDate())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        LocalDateTime currentDateTime = LocalDateTime.now();
        if (oldFromDate.isBefore(currentDateTime) && oldToDate.isBefore(currentDateTime)) {
            return RouterHelper.responseError("Không thể sửa học kỳ đã kết thúc");
        }

        if (oldFromDate.isBefore(currentDateTime)) {
            fromTimeSemester = semester.getFromDate();
        }

        semester.setSemesterName(SemesterName.valueOf(name));
        semester.setYear(yearStartTime);
        semester.setCode(SemesterName.valueOf(name) + "-" + yearStartTime);
        semester.setFromDate(fromTimeSemester);
        semester.setToDate(toTimeSemester);
        Semester semesterSave = adSemesterRepository.save(semester);
        userActivityLogHelper.saveLog("vừa cập nhật học kỳ: " + oldCode + " → " + semesterSave.getCode());
        return RouterHelper.responseSuccess("Cập nhật thành công", semesterSave);
    }

    @Override
    public ResponseEntity<?> changeStatusSemester(String semesterID) {
        Optional<Semester> existSemester = adSemesterRepository.findById(semesterID);
        if (existSemester.isPresent()) {
            Semester semester = existSemester.get();
            String oldStatus = semester.getStatus() == EntityStatus.ACTIVE ? "Hoạt động"
                    : "Không hoạt động";
            if (semester.getStatus().equals(EntityStatus.ACTIVE)) {
                semester.setStatus(EntityStatus.INACTIVE);
            } else {
                semester.setStatus(EntityStatus.ACTIVE);
            }
            String newStatus = semester.getStatus() == EntityStatus.ACTIVE ? "Hoạt động"
                    : "Không hoạt động";
            adSemesterRepository.save(semester);

            if (semester.getStatus() == EntityStatus.ACTIVE) {
                commonUserStudentRepository
                        .disableAllStudentDuplicateShiftByIdSemester(semester.getId());
            }

            userActivityLogHelper.saveLog("vừa thay đổi trạng thái học kỳ " + semester.getCode() + " từ "
                    + oldStatus + " thành " + newStatus);
            return RouterHelper.responseSuccess("Thay đổi trạng thái học kỳ thành công");
        }
        return RouterHelper.responseError("Học kỳ không tồn tại");
    }
}
