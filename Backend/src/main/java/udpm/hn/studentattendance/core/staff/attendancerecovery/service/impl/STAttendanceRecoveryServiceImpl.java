package udpm.hn.studentattendance.core.staff.attendancerecovery.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXDataRequest;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogDetailResponse;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;
import udpm.hn.studentattendance.helpers.RequestTrimHelper;

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

    private final RedisService redisService;

    private final RedisInvalidationHelper redisInvalidationHelper;

    @Value("${spring.cache.redis.time-to-live}")
    private long redisTTL;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final STAttendanceRecoveryHistoryLogRepository historyLogRepository;

    private final STAttendanceRecoveryHistoryLogDetailRepository historyLogDetailRepository;

    public PageableObject<?> getCachedAttendanceRecoveryList(STAttendanceRecoveryRequest request) {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_ATTENDANCE_RECOVERY + "list_"
                + sessionHelper.getFacilityId() + "_"
                + request.toString();

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, PageableObject.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<?> list = PageableObject.of(attendanceRecoveryRepository.getListAttendanceRecovery(request,
                sessionHelper.getFacilityId(), pageable));

        try {
            redisService.set(cacheKey, list, redisTTL);
        } catch (Exception ignored) {
        }

        return list;
    }

    @Override
    public ResponseEntity<?> getListAttendanceRecovery(STAttendanceRecoveryRequest request) {
        PageableObject<?> list = getCachedAttendanceRecoveryList(request);
        return RouterHelper.responseSuccess("Lấy danh sách sự kiện thành công", list);
    }

    @Override
    public ResponseEntity<?> deleteAttendanceRecovery(String attendanceRecoveryId) {
        Optional<AttendanceRecovery> attendanceRecoveryOptional = attendanceRecoveryRepository
                .findById(attendanceRecoveryId);
        if (attendanceRecoveryOptional.isPresent()) {
            List<Attendance> attendanceList = attendanceRepository
                    .findAllByAttendanceRecoveryId(attendanceRecoveryOptional.get().getId());
            attendanceRepository.deleteAll(attendanceList);

            userActivityLogHelper.saveLog(
                    "vừa xóa sự kiện khôi phục điểm danh: " + attendanceRecoveryOptional.get().getName());
            attendanceRecoveryRepository.deleteById(attendanceRecoveryOptional.get().getId());

            // Invalidate all caches
            redisInvalidationHelper.invalidateAllCaches();

            return RouterHelper.responseSuccess("Xóa sự kiện khôi phục điểm danh sinh viên thành công", null);
        } else {
            return RouterHelper.responseError("Sự kiện khôi phục điểm danh sinh viên không tồn tại", null);
        }
    }

    public List<Semester> getCachedSemesters() {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_ATTENDANCE_RECOVERY + "semesters";

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, List.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        List<Semester> semesters = semesterRepository.getAllSemester(EntityStatus.ACTIVE);

        try {
            redisService.set(cacheKey, semesters, redisTTL);
        } catch (Exception ignored) {
        }

        return semesters;
    }

    @Override
    public ResponseEntity<?> getAllSemester() {
        List<Semester> semesters = getCachedSemesters();
        return RouterHelper.responseSuccess("Lấy tất cả học kỳ thành công", semesters);
    }

    @Override
    public ResponseEntity<?> createNewEventAttendanceRecovery(STCreateOrUpdateNewEventRequest request) {
        // Trim all string fields in the request
        RequestTrimHelper.trimStringFields(request);

        Optional<Facility> facilityOptional = facilityRepository.findById(sessionHelper.getFacilityId());
        if (facilityOptional == null) {
            return RouterHelper.responseError("Cơ sở không tồn tại", null);
        }
        AttendanceRecovery attendanceRecovery = new AttendanceRecovery();
        attendanceRecovery.setName(request.getName());
        attendanceRecovery.setDescription(request.getDescription());
        attendanceRecovery.setDay(request.getDay());
        attendanceRecovery.setFacility(facilityOptional.get());
        AttendanceRecovery attendanceRecoverySave = attendanceRecoveryRepository.save(attendanceRecovery);

        userActivityLogHelper.saveLog("vừa thêm sự kiện khôi phục điểm danh mới: " + attendanceRecoverySave.getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Thêm sự kiện khôi phục điểm danh mới thành công", attendanceRecoverySave);
    }

    public AttendanceRecovery getCachedAttendanceRecoveryDetail(String id) {
        Optional<AttendanceRecovery> attendanceRecoveryOptional = attendanceRecoveryRepository.findById(id);
        return attendanceRecoveryOptional.orElse(null);
    }

    @Override
    public ResponseEntity<?> getDetailEventAttendanceRecovery(String idEventAttendanceRecovery) {
        AttendanceRecovery attendanceRecovery = getCachedAttendanceRecoveryDetail(idEventAttendanceRecovery);
        if (attendanceRecovery != null) {
            return RouterHelper.responseSuccess("Lấy chi tiết sự kiện khôi phục điểm danh thành công",
                    attendanceRecovery);
        }
        return RouterHelper.responseError("Sự Kiện khôi phục điểm danh không tồn tại", null);
    }

    @Override
    public ResponseEntity<?> updateEventAttendanceRecovery(STCreateOrUpdateNewEventRequest request, String id) {
        // Trim all string fields in the request
        RequestTrimHelper.trimStringFields(request);

        Optional<AttendanceRecovery> attendanceRecoveryOptional = attendanceRecoveryRepository.findById(id);
        if (attendanceRecoveryOptional.isPresent()) {
            AttendanceRecovery attendanceRecovery = attendanceRecoveryOptional.get();
            String oldName = attendanceRecovery.getName();
            attendanceRecovery.setName(request.getName());
            attendanceRecovery.setDescription(request.getDescription());
            attendanceRecovery.setDay(request.getDay());
            attendanceRecoveryRepository.save(attendanceRecovery);

            userActivityLogHelper.saveLog(
                    "vừa cập nhật sự kiện khôi phục điểm danh: " + oldName + " → " + attendanceRecovery.getName());

            // Invalidate all caches
            redisInvalidationHelper.invalidateAllCaches();

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
                    request.getDay());
            if (validPlanDates.isEmpty()) {
                return RouterHelper.responseError(
                        String.format("Ngày %s - Sinh viên %s - %s không có ca học nào",
                                formatDate(request.getDay()),
                                userStudent.getCode(),
                                userStudent.getName()),
                        null);
            }

            Optional<AttendanceRecovery> attendanceRecoveryOptional = attendanceRecoveryRepository
                    .findById(request.getAttendanceRecoveryId());
            AttendanceProcessResult result = processAttendanceRecords(validPlanDates, userStudent,
                    attendanceRecoveryOptional.get());

            if (result.getCreatedCount() == 0 && result.getUpdatedCount() == 0) {
                return RouterHelper.responseError(
                        "Sinh viên đã được điểm danh có mặt cho tất cả ca học trong ngày "
                                + formatDate(request.getDay()),
                        null);
            }

            // Invalidate all caches
            redisInvalidationHelper.invalidateAllCaches();

            return RouterHelper.responseSuccess(
                    String.format(
                            "Khôi phục điểm danh thành công: %d ca học mới, %d ca học đã cập nhật, ngày %s cho sinh viên %s",
                            result.getCreatedCount(), result.getUpdatedCount(), formatDate(request.getDay()),
                            userStudent.getCode()),
                    result.getResponseData());

        } catch (Exception e) {
            return RouterHelper.responseError("Có lỗi xảy ra khi khôi phục điểm danh", null);
        }
    }

    private AttendanceProcessResult processAttendanceRecords(List<PlanDate> planDates, UserStudent userStudent,
            AttendanceRecovery attendanceRecovery) {
        List<Object> responseData = new ArrayList<>();
        int createdCount = 0;
        int updatedCount = 0;

        for (PlanDate planDate : planDates) {
            Attendance existingAttendance = attendanceRepository.findByUserStudentIdAndPlanDateId(
                    userStudent.getId(), planDate.getId());

            if (existingAttendance == null) {
                Attendance newAttendance = new Attendance();
                newAttendance.setUserStudent(userStudent);
                newAttendance.setPlanDate(planDate);
                newAttendance.setStatus(EntityStatus.ACTIVE);
                newAttendance.setAttendanceStatus(AttendanceStatus.PRESENT);
                newAttendance.setAttendanceRecovery(attendanceRecovery);
                attendanceRepository.save(newAttendance);

                createdCount++;
                responseData.add(createAttendanceResponseItem(newAttendance, "CREATED"));

            } else if (existingAttendance.getAttendanceStatus() == AttendanceStatus.CHECKIN
                    || existingAttendance.getAttendanceStatus() == AttendanceStatus.ABSENT
                    || existingAttendance.getAttendanceStatus() == AttendanceStatus.NOTCHECKIN) {
                existingAttendance.setAttendanceStatus(AttendanceStatus.PRESENT);
                existingAttendance.setAttendanceRecovery(attendanceRecovery);
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

    private UserStudent validateAndGetStudent(String studentCode) {
        if (studentCode == null || studentCode.trim().isEmpty()) {
            return null;
        }
        return studentRepository.getStudentByCode(
                studentCode.trim(),
                EntityStatus.ACTIVE);
    }

    private UserStudentFactory validateAndGetStudentFactory(UserStudent userStudent) {
        if (userStudent == null || userStudent.getId() == null) {
            return null;
        }
        return studentFactoryRepository.getUserStudentFactoryByUserId(
                userStudent.getId(),
                EntityStatus.ACTIVE,
                EntityStatus.ACTIVE);
    }

    private PlanFactory validateAndGetPlanFactory(UserStudentFactory userStudentFactory) {
        if (userStudentFactory == null || userStudentFactory.getFactory() == null) {
            return null;
        }
        return planFactoryRepository.getPlanFactoryByFactoryId(
                userStudentFactory.getFactory().getId(),
                EntityStatus.ACTIVE,
                EntityStatus.ACTIVE);
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

    public PageableObject<?> getCachedHistoryLogByEvent(String idImportLog, EXDataRequest request) {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_ATTENDANCE_RECOVERY + "history_log_"
                + idImportLog + "_"
                + sessionHelper.getUserId() + "_"
                + sessionHelper.getFacilityId() + "_"
                + request.toString();

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, PageableObject.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<?> list = PageableObject.of(historyLogRepository.getListHistory(pageable, 6,
                sessionHelper.getUserId(), sessionHelper.getFacilityId(), idImportLog));

        try {
            redisService.set(cacheKey, list, redisTTL);
        } catch (Exception ignored) {
        }

        return list;
    }

    @Override
    public ResponseEntity<?> getAllHistoryLogByEvent(String idImportLog, EXDataRequest request) {
        PageableObject<?> list = getCachedHistoryLogByEvent(idImportLog, request);
        return RouterHelper.responseSuccess("Lấy tất cả log import excel khôi phục điểm danh thành công", list);
    }

    public List<ExImportLogDetailResponse> getCachedHistoryLogDetailEvent(String idImportLog) {
        return historyLogRepository.getAllList(idImportLog,
                sessionHelper.getUserId(), sessionHelper.getFacilityId());
    }

    @Override
    public ResponseEntity<?> getAllHistoryLogDetailEvent(String idImportLog) {
        List<ExImportLogDetailResponse> logDetailResponseList = getCachedHistoryLogDetailEvent(idImportLog);
        return RouterHelper.responseSuccess("Lấy chi tiết lịch sử import khôi phục điểm danh thành công",
                logDetailResponseList);
    }

    public Integer getCachedImportStudentSuccess(String idImportLog, String userId, String facilityId, Integer type) {
        return historyLogRepository.getAllLine(idImportLog, userId, facilityId, type);
    }

    @Override
    public Integer getAllImportStudentSuccess(String idImportLog, String userId, String facilityId, Integer type) {
        return getCachedImportStudentSuccess(idImportLog, userId, facilityId, type);
    }

    public Boolean getCachedHasStudentAttendanceRecovery(String idAttendanceRecovery) {
        Optional<AttendanceRecovery> attendanceRecoveryOptional = attendanceRecoveryRepository
                .findById(idAttendanceRecovery);

        if (attendanceRecoveryOptional.isPresent()) {
            AttendanceRecovery attendanceRecovery = attendanceRecoveryOptional.get();
            return attendanceRecovery.getImportLog() != null;
        }

        return false;
    }

    @Override
    public ResponseEntity<?> isHasStudentAttendanceRecovery(String idAttendanceRecovery) {
        Optional<AttendanceRecovery> attendanceRecoveryOptional = attendanceRecoveryRepository
                .findById(idAttendanceRecovery);
        if (attendanceRecoveryOptional.isPresent()) {
            Boolean hasStudents = getCachedHasStudentAttendanceRecovery(idAttendanceRecovery);
            return RouterHelper.responseSuccess("Kiểm tra sự kiện có sinh viên thành công", hasStudents);
        }
        return RouterHelper.responseError("Không tìm thấy sự kiện", false);
    }

    @Override
    public ResponseEntity<?> deleteAttendanceRecordByAttendanceRecovery(String idAttendanceRecovery) {
        Optional<AttendanceRecovery> attendanceRecoveryOptional = attendanceRecoveryRepository
                .findById(idAttendanceRecovery);
        if (attendanceRecoveryOptional.isPresent()) {
            AttendanceRecovery attendanceRecovery = attendanceRecoveryOptional.get();

            List<Attendance> attendanceList = attendanceRepository
                    .findAllByAttendanceRecoveryId(attendanceRecovery.getId());
            if (attendanceList != null) {
                attendanceRepository.deleteAll(attendanceList);
            }

            List<ImportLogDetail> importLogDetailList = historyLogRepository
                    .getAllByImportLog(attendanceRecoveryOptional.get().getImportLog().getId());
            if (importLogDetailList != null) {
                historyLogDetailRepository.deleteAll(importLogDetailList);

            }

            Optional<ImportLog> importLog = historyLogRepository.findById(attendanceRecoveryOptional.get().getId());
            if (importLog.isPresent()) {
                historyLogRepository.deleteById(importLog.get().getId());
            }

            attendanceRecovery.setImportLog(null);
            attendanceRecovery.setTotalStudent(0);
            attendanceRecoveryRepository.save(attendanceRecovery);

            userActivityLogHelper
                    .saveLog("đã xóa dữ liệu điểm danh của sự kiện khôi phục: " + attendanceRecovery.getName());

            // Invalidate all caches
            redisInvalidationHelper.invalidateAllCaches();

            return RouterHelper.responseSuccess("Xóa dữ liệu điểm danh thành công", null);
        }
        return RouterHelper.responseError("Không tìm thấy sự kiện khôi phục điểm danh", null);
    }

}
