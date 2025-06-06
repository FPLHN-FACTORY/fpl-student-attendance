package udpm.hn.studentattendance.core.staff.attendancerecovery.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STAttendanceRecoveryRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STCreateOrUpdateNewEventRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STStudentAttendanceRecoveryRequest;
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

import java.time.DateTimeException;
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

    @Override
    public ResponseEntity<?> getListAttendanceRecovery(STAttendanceRecoveryRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject list = PageableObject.of
                (attendanceRecoveryRepository.getListAttendanceRecovery(request, sessionHelper.getFacilityId(), pageable));
        return RouterHelper.responseSuccess("Lấy danh sách sự kiện thành công", list);
    }

    @Override
    public ResponseEntity<?> deleteAttendanceRecovery(String attendanceRecoveryId) {
        return null;
    }

    @Override
    public ResponseEntity<?> getAllSemester() {
        List<Semester> semesters = semesterRepository.getAllSemester(EntityStatus.ACTIVE);
        return RouterHelper.responseSuccess("Lấy tất cả học kỳ thành công", semesters);
    }    @Override
    public ResponseEntity<?> createNewEventAttendanceRecovery(STCreateOrUpdateNewEventRequest request) {
        Optional<Facility> facilityOptional = facilityRepository.findById(sessionHelper.getFacilityId());
        if (facilityOptional == null) {
            return RouterHelper.responseError("Cơ sở không tồn tại", null);
        }
        if (!ValidateHelper.isValidFullname(request.getName())) {
            return RouterHelper.responseError("Tên sự kiện không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và Chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt.", null);
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
    }    @Override
    public ResponseEntity<?> updateEventAttendanceRecovery(STCreateOrUpdateNewEventRequest request, String id) {
        Optional<AttendanceRecovery> attendanceRecoveryOptional = attendanceRecoveryRepository.findById(id);
        if (!ValidateHelper.isValidFullname(request.getName())) {
            return RouterHelper.responseError("Tên sự kiện không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và Chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt.", null);
        }
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
    public ResponseEntity<?> importAttendanceRecoveryStudent(STStudentAttendanceRecoveryRequest request) {
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

            // Gọi hàm đã chỉnh sửa để so sánh chỉ theo phần ngày (dd/MM/yyyy)
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

            List<PlanDate> planDatesToProcess = filterExistingAttendance(validPlanDates, userStudent);
            if (planDatesToProcess.isEmpty()) {
                return RouterHelper.responseError(
                        "Sinh viên đã được điểm danh cho tất cả ca học trong ngày này",
                        null);
            }

            List<Attendance> attendanceList = createAttendanceRecords(planDatesToProcess, userStudent);
            attendanceRepository.saveAll(attendanceList);

            Optional<AttendanceRecovery> attendanceRecoveryOptional = attendanceRecoveryRepository.findById(request.getAttendanceRecoveryId());


            return RouterHelper.responseSuccess(
                    String.format("Khôi phục điểm danh thành công cho %d ca học của sinh viên %s",
                            attendanceList.size(), userStudent.getCode()),
                    createAttendanceResponse(attendanceList)
            );

        } catch (Exception e) {
            return RouterHelper.responseError("Có lỗi xảy ra khi khôi phục điểm danh", null);
        }
    }

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

    /**
     * Lọc ra các PlanDate sao cho endDate (epochMilli) khi format sang "dd/MM/yyyy"
     * trùng với requestDay (epochMilli) format sang "dd/MM/yyyy"
     */
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

    private List<PlanDate> filterExistingAttendance(List<PlanDate> planDates, UserStudent userStudent) {
        if (planDates.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> planDateIds = planDates.stream()
                .map(PlanDate::getId)
                .collect(Collectors.toList());

        List<String> existingAttendanceIds = planDateRepository.findExistingAttendance(
                userStudent.getId(), planDateIds);

        return planDates.stream()
                .filter(pd -> !existingAttendanceIds.contains(pd.getId()))
                .collect(Collectors.toList());
    }

    private List<Attendance> createAttendanceRecords(List<PlanDate> planDates, UserStudent userStudent) {
        return planDates.stream()
                .map(planDate -> {
                    Attendance attendance = new Attendance();
                    attendance.setUserStudent(userStudent);
                    attendance.setPlanDate(planDate);
                    attendance.setStatus(EntityStatus.ACTIVE);
                    attendance.setAttendanceStatus(AttendanceStatus.PRESENT);
                    return attendance;
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

    private Object createAttendanceResponse(List<Attendance> attendanceList) {
        return attendanceList.stream()
                .map(attendance -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("planDateId", attendance.getPlanDate().getId());
                    item.put("studentCode", attendance.getUserStudent().getCode());
                    item.put("attendanceStatus", attendance.getAttendanceStatus());
                    return item;
                })
                .collect(Collectors.toList());
    }


}
