package udpm.hn.studentattendance.core.staff.attendancerecovery.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STAttendanceRecoveryRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STCreateOrUpdateNewEventRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STStudentAttendanceRecoveryAddRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.repository.*;
import udpm.hn.studentattendance.core.staff.attendancerecovery.service.STAttendanceRecoveryService;
import udpm.hn.studentattendance.entities.*;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogDetailResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Validated
public class STAttendanceRecoveryServiceImpl implements STAttendanceRecoveryService {

    private final STAttendanceRecoveryRepository attendanceRecoveryRepository;

    private final STAttendanceRecoverySemesterRepository semesterRepository;

    private final STAttendanceRecoveryFacilityRepository facilityRepository;

    private final SessionHelper sessionHelper;

    private final STAttendanceRecoveryStudentRepository studentRepository;

    private final STAttendanceRecoveryStudentFactoryRepository studentFactoryRepository;

    private final STAttendanceRecoveryPlanFactoryRepository planFactoryRepository;

    private final STAttendanceRecoveryPlanDateRepository planDateRepository;

    private final STAttendanceRevoveryAttendanceRepository attendanceRepository;

    private final UserActivityLogHelper userActivityLogHelper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final STAttendanceRecoveryHistoryLogRepository historyLogRepository;

    @Override
    public ResponseEntity<?> getListAttendanceRecovery(STAttendanceRecoveryRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject list = PageableObject.of
                (attendanceRecoveryRepository.getListAttendanceRecovery(request, sessionHelper.getFacilityId(), pageable));
        return RouterHelper.responseSuccess("Lấy danh sách sự kiện thành công", list);
    }

    @Override
    public ResponseEntity<?> deleteAttendanceRecovery(String attendanceRecoveryId, STStudentAttendanceRecoveryAddRequest request) {

        return null;
    }

    @Override
    public ResponseEntity<?> getAllSemester() {
        List<Semester> semesters = semesterRepository.getAllSemester(EntityStatus.ACTIVE);
        return RouterHelper.responseSuccess("Lấy tất cả học kỳ thành công", semesters);
    }

    @Override
    public ResponseEntity<?> createNewEventAttendanceRecovery(STCreateOrUpdateNewEventRequest request) {
        Optional<Facility> facilityOptional = facilityRepository.findById(sessionHelper.getFacilityId());
        if (facilityOptional == null) {
            return RouterHelper.responseError("Cơ sở không tồn tại", null);
        }
        if (!ValidateHelper.isValidFullname(request.getName())) {
            return RouterHelper.responseError("Tên sự kiện không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và Chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt.", null);
        }

        if (ValidateHelper.containsEmoji(request.getName())) {
            return RouterHelper.responseError("Tên sự kiện không được chứa emoji", null);
        }

        if (request.getDescription() != null && ValidateHelper.containsEmoji(request.getDescription())) {
            return RouterHelper.responseError("Mô tả sự kiện không được chứa emoji", null);
        }

        AttendanceRecovery attendanceRecovery = new AttendanceRecovery();
        attendanceRecovery.setName(request.getName());
        attendanceRecovery.setDescription(request.getDescription());
        attendanceRecovery.setDay(request.getDay());
        attendanceRecovery.setFacility(facilityOptional.get());
        AttendanceRecovery attendanceRecoverySave = attendanceRecoveryRepository.save(attendanceRecovery);

        userActivityLogHelper.saveLog("vừa thêm sự kiện khôi phục điểm danh mới: " + attendanceRecoverySave.getName());
        return RouterHelper.responseSuccess("Thêm sự kiện khôi phục điểm danh mới thành công", attendanceRecoverySave);
    }

    @Override
    public ResponseEntity<?> getDetailEventAttendanceRecovery(String idEventAttendanceRecovery) {
        Optional<AttendanceRecovery> attendanceRecoveryOptional = attendanceRecoveryRepository.findById(idEventAttendanceRecovery);
        if (attendanceRecoveryOptional.isPresent()) {
            return RouterHelper.responseSuccess("Lấy chi tiết sự kiện khôi phục điểm danh thành công", attendanceRecoveryOptional);
        }
        return RouterHelper.responseError("Sự Kiện khôi phục điểm danh không tồn tại", null);
    }

    @Override
    public ResponseEntity<?> updateEventAttendanceRecovery(STCreateOrUpdateNewEventRequest request, String id) {
        if (!ValidateHelper.isValidFullname(request.getName())) {
            return RouterHelper.responseError("Tên sự kiện không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và Chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt.", null);
        }

        // Validate emoji in event name
        if (ValidateHelper.containsEmoji(request.getName())) {
            return RouterHelper.responseError("Tên sự kiện không được chứa emoji", null);
        }

        // Validate emoji in event description
        if (request.getDescription() != null && ValidateHelper.containsEmoji(request.getDescription())) {
            return RouterHelper.responseError("Mô tả sự kiện không được chứa emoji", null);
        }

        Optional<AttendanceRecovery> attendanceRecoveryOptional = attendanceRecoveryRepository.findById(id);
        if (attendanceRecoveryOptional.isPresent()) {
            AttendanceRecovery attendanceRecovery = attendanceRecoveryOptional.get();
            String oldName = attendanceRecovery.getName();
            attendanceRecovery.setName(request.getName());
            attendanceRecovery.setDescription(request.getDescription());
            attendanceRecovery.setDay(request.getDay());
            attendanceRecoveryRepository.save(attendanceRecovery);

            userActivityLogHelper.saveLog("vừa cập nhật sự kiện khôi phục điểm danh: " + oldName + " → " + attendanceRecovery.getName());
            return RouterHelper.responseSuccess("Cập nhật sự kiện khôi phục điểm danh thành công", attendanceRecovery);
        }
        return RouterHelper.responseError("Sự kiện khôi phục điểm danh không tồn tại", null);
    }


    @Override
    public ResponseEntity<?> importAttendanceRecoveryStudent(STStudentAttendanceRecoveryAddRequest request) {
        try {
            if (request == null || request.getStudentCode() == null || request.getDay() == null) {
                return RouterHelper.responseError("Dữ liệu đầu vào không hợp lệ", null);
            }

            UserStudent userStudent = validateAndGetStudent(request.getStudentCode());
            if (userStudent == null) {
                return RouterHelper.responseError(
                        "Không tìm thấy mã sinh viên " + request.getStudentCode(), null);
            }

            UserStudentFactory userStudentFactory = validateAndGetStudentFactory(userStudent);
            if (userStudentFactory == null) {
                return RouterHelper.responseError(
                        String.format("Sinh viên %s - %s chưa tham gia nhóm xưởng nào",
                                userStudent.getCode(), userStudent.getName()),
                        null);
            }

            PlanFactory planFactory = validateAndGetPlanFactory(userStudentFactory);
            if (planFactory == null) {
                return RouterHelper.responseError(
                        String.format("Nhóm xưởng của sinh viên %s - %s chưa có kế hoạch hoặc không tồn tại",
                                userStudent.getCode(), userStudent.getName()),
                        null);
            }

            List<PlanDate> validPlanDates = getValidPlanDatesForRecovery(
                    planFactory.getId(),
                    request.getDay()
            );
            if (validPlanDates.isEmpty()) {
                return RouterHelper.responseError(
                        String.format("Ngày %s - Sinh viên %s - %s không có ca học nào",
                                formatDate(request.getDay()),
                                userStudent.getCode(),
                                userStudent.getName()),
                        null);
            }

            AttendanceProcessResult result = processAttendanceRecords(validPlanDates, userStudent);

            if (result.getCreatedCount() == 0 && result.getUpdatedCount() == 0) {
                return RouterHelper.responseError(
                        "Sinh viên đã được điểm danh PRESENT cho tất cả ca học trong ngày này",
                        null);
            }

            return RouterHelper.responseSuccess(
                    String.format("Khôi phục điểm danh thành công: %d ca học mới, %d ca học đã cập nhật cho sinh viên %s",
                            result.getCreatedCount(), result.getUpdatedCount(), userStudent.getCode()),
                    result.getResponseData()
            );

        } catch (Exception e) {
            return RouterHelper.responseError("Có lỗi xảy ra khi khôi phục điểm danh", null);
        }
    }

    private AttendanceProcessResult processAttendanceRecords(List<PlanDate> planDates, UserStudent userStudent) {
        List<Object> responseData = new ArrayList<>();
        int createdCount = 0;
        int updatedCount = 0;

        for (PlanDate planDate : planDates) {
            // Kiểm tra xem đã có attendance record chưa
            Attendance existingAttendance = attendanceRepository.findByUserStudentIdAndPlanDateId(
                    userStudent.getId(), planDate.getId());

            if (existingAttendance == null) {
                // Tạo mới attendance record
                Attendance newAttendance = new Attendance();
                newAttendance.setUserStudent(userStudent);
                newAttendance.setPlanDate(planDate);
                newAttendance.setStatus(EntityStatus.ACTIVE);
                newAttendance.setAttendanceStatus(AttendanceStatus.PRESENT);
                attendanceRepository.save(newAttendance);

                createdCount++;
                responseData.add(createAttendanceResponseItem(newAttendance, "CREATED"));

            } else if (existingAttendance.getAttendanceStatus() == AttendanceStatus.CHECKIN) {
                // Update attendance từ CHECKIN thành PRESENT
                existingAttendance.setAttendanceStatus(AttendanceStatus.PRESENT);
                attendanceRepository.save(existingAttendance);

                updatedCount++;
                responseData.add(createAttendanceResponseItem(existingAttendance, "UPDATED"));
            }
        }

        return new AttendanceProcessResult(createdCount, updatedCount, responseData);
    }

    private Map<String, Object> createAttendanceResponseItem(Attendance attendance, String action) {
        Map<String, Object> item = new HashMap<>();
        item.put("planDateId", attendance.getPlanDate().getId());
        item.put("studentCode", attendance.getUserStudent().getCode());
        item.put("attendanceStatus", attendance.getAttendanceStatus());
        item.put("action", action); // "CREATED" hoặc "UPDATED"
        return item;
    }

    // Inner class để lưu kết quả xử lý
    private static class AttendanceProcessResult {
        private final int createdCount;
        private final int updatedCount;
        private final List<Object> responseData;

        public AttendanceProcessResult(int createdCount, int updatedCount, List<Object> responseData) {
            this.createdCount = createdCount;
            this.updatedCount = updatedCount;
            this.responseData = responseData;
        }

        public int getCreatedCount() {
            return createdCount;
        }

        public int getUpdatedCount() {
            return updatedCount;
        }

        public List<Object> getResponseData() {
            return responseData;
        }
    }

    // Các phương thức validation giữ nguyên như cũ
    private UserStudent validateAndGetStudent(String studentCode) {
        if (studentCode == null || studentCode.trim().isEmpty()) {
            return null;
        }
        return studentRepository.getStudentByCode(
                studentCode.trim(),
                EntityStatus.ACTIVE
        );
    }

    private UserStudentFactory validateAndGetStudentFactory(UserStudent userStudent) {
        if (userStudent == null || userStudent.getId() == null) {
            return null;
        }
        return studentFactoryRepository.getUserStudentFactoryByUserId(
                userStudent.getId(),
                EntityStatus.ACTIVE,
                EntityStatus.ACTIVE
        );
    }

    private PlanFactory validateAndGetPlanFactory(UserStudentFactory userStudentFactory) {
        if (userStudentFactory == null || userStudentFactory.getFactory() == null) {
            return null;
        }
        return planFactoryRepository.getPlanFactoryByFactoryId(
                userStudentFactory.getFactory().getId(),
                EntityStatus.ACTIVE,
                EntityStatus.ACTIVE
        );
    }

    private List<PlanDate> getValidPlanDatesForRecovery(String planFactoryId, Long requestDay) {
        LocalDate requestLocalDate = Instant.ofEpochMilli(requestDay)
                .atZone(VN_ZONE)
                .toLocalDate();
        String requestDateStr = requestLocalDate.format(DATE_FORMATTER);

        List<PlanDate> allPlanDates = planDateRepository
                .getAllPlanDateByPlanFactoryId(planFactoryId, EntityStatus.ACTIVE, EntityStatus.ACTIVE);

        return allPlanDates.stream()
                .filter(pd -> {
                    Long pdEndMillis = pd.getEndDate(); // epochMilli
                    LocalDate pdLocalDate = Instant.ofEpochMilli(pdEndMillis)
                            .atZone(VN_ZONE)
                            .toLocalDate();
                    String pdDateStr = pdLocalDate.format(DATE_FORMATTER);
                    return pdDateStr.equals(requestDateStr);
                })
                .collect(Collectors.toList());
    }

    private String formatDate(Long timestamp) {
        try {
            LocalDate date = Instant.ofEpochMilli(timestamp)
                    .atZone(VN_ZONE)
                    .toLocalDate();
            return date.format(DATE_FORMATTER);
        } catch (Exception e) {
            return timestamp.toString();
        }
    }

    @Override
    public ResponseEntity<?> getAllHistoryLogByEvent(String idImportLog, EXDataRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject list = PageableObject.of(historyLogRepository.getListHistory(pageable, 6, sessionHelper.getUserId(), sessionHelper.getFacilityId(), idImportLog));
        return RouterHelper.responseSuccess("Lấy tất cả log import excel khôi phục điểm danh thành công", list);
    }

    @Override
    public ResponseEntity<?> getAllHistoryLogDetailEvent(String idImportLog) {
        List<ExImportLogDetailResponse> logDetailResponseList = historyLogRepository.getAllList(idImportLog, sessionHelper.getUserId(), sessionHelper.getFacilityId());
        return RouterHelper.responseSuccess("Lấy chi tiết lịch sử import khôi phục điểm danh thành công", logDetailResponseList);
    }

}
