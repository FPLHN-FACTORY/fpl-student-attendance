package udpm.hn.studentattendance.core.student.attendance.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.student.attendance.model.request.SACheckinAttendanceRequest;
import udpm.hn.studentattendance.core.student.attendance.model.request.SAFilterAttendanceRequest;
import udpm.hn.studentattendance.core.student.attendance.model.response.SAAttendanceRecoveryResponse;
import udpm.hn.studentattendance.core.student.attendance.model.response.SAAttendanceResponse;
import udpm.hn.studentattendance.core.student.attendance.repositories.SAAttendanceRepository;
import udpm.hn.studentattendance.core.student.attendance.repositories.SAFacilityIPRepository;
import udpm.hn.studentattendance.core.student.attendance.repositories.SAFacilityLocationRepository;
import udpm.hn.studentattendance.core.student.attendance.repositories.SAPlanDateRepository;
import udpm.hn.studentattendance.core.student.attendance.repositories.SAUserStudentFactoryRepository;
import udpm.hn.studentattendance.core.student.attendance.service.SAAttendanceService;
import udpm.hn.studentattendance.entities.Attendance;
import udpm.hn.studentattendance.entities.FacilityLocation;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.SettingHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.services.OnnxService;
import udpm.hn.studentattendance.infrastructure.config.websocket.model.message.AttendanceMessage;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteWebsocketConstant;
import udpm.hn.studentattendance.utils.AppUtils;
import udpm.hn.studentattendance.utils.DateTimeUtils;
import udpm.hn.studentattendance.utils.FaceRecognitionUtils;
import udpm.hn.studentattendance.utils.GeoUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SAAttendanceServiceImpl implements SAAttendanceService {

    private final OnnxService onnxService;

    private final SessionHelper sessionHelper;

    private final SAAttendanceRepository attendanceRepository;

    private final SAPlanDateRepository planDateRepository;

    private final SAUserStudentFactoryRepository userStudentFactoryRepository;

    private final SAFacilityIPRepository facilityIPRepository;

    private final SAFacilityLocationRepository facilityLocationRepository;

    private final HttpServletRequest httpServletRequest;

    private final SimpMessagingTemplate messagingTemplate;

    private final SettingHelper settingHelper;

    @Value("${app.config.face.threshold_checkin}")
    private double FACE_THRESHOLD_CHECKIN;

    @Value("${app.config.face.threshold_antispoof}")
    private double FACE_THRESHOLD_ANTIS_POOF;

    private int TIME_LIVE_SIGN = 5;

    @Override
    public ResponseEntity<?> getAllList(SAFilterAttendanceRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        request.setIdUserStudent(sessionHelper.getUserId());

        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<SAAttendanceResponse> data = PageableObject
                .of(attendanceRepository.getAllByFilter(pageable, request));

        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> checkin(SACheckinAttendanceRequest request, MultipartFile image) {
        PlanDate planDate = planDateRepository.findById(request.getIdPlanDate()).orElse(null);
        if (planDate == null
                || !Objects.equals(
                        planDate.getPlanFactory().getFactory().getProject().getSubjectFacility().getFacility().getId(),
                        sessionHelper.getFacilityId())) {
            return RouterHelper.responseError("Không tìm thấy lịch");
        }

        UserStudentFactory userStudentFactory = userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(
                sessionHelper.getUserId(), planDate.getPlanFactory().getFactory().getId()).orElse(null);
        if (userStudentFactory == null) {
            return RouterHelper.responseError("Ca không tồn tại");
        }

        if (planDate.getType() != ShiftType.ONLINE) {
            if (planDate.getRequiredIp() == StatusType.ENABLE) {
                String clientIP = AppUtils.getClientIP(httpServletRequest);
                if (clientIP == null) {
                    return RouterHelper.responseError("IP đăng nhập không hợp lệ");
                }

                Set<String> allowIPs = facilityIPRepository.getAllIP(sessionHelper.getFacilityId());
                if (!allowIPs.isEmpty() && !ValidateHelper.isAllowedIP(clientIP, allowIPs)) {
                    return RouterHelper.responseError("Vui lòng kết nối bằng mạng trường để tiếp tục checkin/checkout");
                }
            }

            if (planDate.getRequiredLocation() == StatusType.ENABLE) {
                if (request.getLatitude() == null || request.getLongitude() == null) {
                    return RouterHelper.responseError("Không thể lấy thông tin vị trí");
                }
                List<FacilityLocation> lstLocation = facilityLocationRepository
                        .getAllList(sessionHelper.getFacilityId());
                if (!GeoUtils.isAllowedLocation(lstLocation, request.getLatitude(), request.getLongitude())) {
                    return RouterHelper.responseError("Địa điểm checkin/checkout nằm ngoài vùng cho phép");
                }
            }
        }

        UserStudent userStudent = userStudentFactory.getUserStudent();

        boolean isEnableCheckin = planDate.getRequiredCheckin() == StatusType.ENABLE;
        boolean isEnableCheckout = planDate.getRequiredCheckout() == StatusType.ENABLE;

        if (!StringUtils.hasText(userStudent.getFaceEmbedding())) {
            return RouterHelper.responseError("Tài khoản chưa đăng ký thông tin khuôn mặt");
        }

        Attendance attendance = attendanceRepository
                .findByUserStudent_IdAndPlanDate_Id(userStudent.getId(), planDate.getId()).orElse(null);

        if (attendance != null) {
            if (attendance.getAttendanceStatus() == AttendanceStatus.PRESENT) {
                return RouterHelper.responseError("Ca đã được điểm danh");
            }
            if (attendance.getAttendanceStatus() == AttendanceStatus.ABSENT) {
                return RouterHelper.responseError("Bạn đã bị huỷ điểm danh.");
            }
        }

        SAAttendanceRecoveryResponse attendanceRecovery = attendanceRepository
                .getAttendanceRecovery(planDate.getId(), userStudentFactory.getUserStudent().getId()).orElse(null);
        boolean isRecovery = attendanceRecovery != null
                && attendanceRecovery.getTotalLateAttendance() > attendanceRecovery.getCurrentLateAttendance();

        boolean isCanCompensateCheckin = isRecovery && DateTimeUtils.getCurrentTimeMillis() <= planDate.getEndDate();
        boolean isCanCompensateCheckout = isRecovery && DateTimeUtils.getCurrentTimeMillis() <= planDate.getEndDate() + planDate.getLateArrival() * 60 * 1000 * 2;

        Long lateCheckin = null;
        Long lateCheckout = null;

        int EARLY_CHECKIN = settingHelper.getSetting(SettingKeys.ATTENDANCE_EARLY_CHECKIN, Integer.class);

        if (attendance == null || attendance.getAttendanceStatus() == AttendanceStatus.NOTCHECKIN) {
            if (isEnableCheckin || !isEnableCheckout) {
                if (DateTimeUtils.getCurrentTimeMillis() <= planDate.getStartDate()
                        - (long) EARLY_CHECKIN * 60 * 1000) {
                    return RouterHelper
                            .responseError("Chưa đến giờ " + (isEnableCheckin ? "checkin đầu giờ" : "điểm danh"));
                }

                if (DateTimeUtils.getCurrentTimeMillis() > planDate.getStartDate()
                        + planDate.getLateArrival() * 60 * 1000) {
                    if (!isCanCompensateCheckin) {
                        return RouterHelper
                                .responseError("Đã quá giờ " + (isEnableCheckin ? "checkin đầu giờ" : "điểm danh"));
                    }
                    lateCheckin = Calendar.getInstance().getTimeInMillis();
                }
            }

            if (!isEnableCheckin && isEnableCheckout) {
                if (DateTimeUtils.getCurrentTimeMillis() < planDate.getEndDate()) {
                    return RouterHelper.responseError("Chưa đến giờ checkout cuối giờ");
                }

                if (DateTimeUtils.getCurrentTimeMillis() > planDate.getEndDate()
                        + planDate.getLateArrival() * 60 * 1000) {
                    if (!isCanCompensateCheckout) {
                        return RouterHelper.responseError("Đã quá giờ checkout cuối giờ");
                    }
                    lateCheckout = Calendar.getInstance().getTimeInMillis();
                }
            }

        } else {

            if (isEnableCheckin && attendance.getAttendanceStatus() != AttendanceStatus.CHECKIN) {
                return RouterHelper.responseError("Không thể checkout khi chưa checkin");
            }

            if (DateTimeUtils.getCurrentTimeMillis() < planDate.getEndDate()) {
                return RouterHelper.responseError("Chưa đến giờ checkout cuối giờ");
            }

            if (DateTimeUtils.getCurrentTimeMillis() > planDate.getEndDate() + planDate.getLateArrival() * 60 * 1000) {
                if (!isCanCompensateCheckout) {
                    return RouterHelper.responseError("Đã quá giờ checkout cuối giờ");
                }
                attendance.setLateCheckout(Calendar.getInstance().getTimeInMillis());
            }
        }


        try {
            if (image == null || image.isEmpty()) {
                throw new RuntimeException();
            }

            Set<String> serverSignature = new HashSet<>();
            for(int i = 0; i < TIME_LIVE_SIGN; i++) {
                long timestamp = DateTimeUtils.getCurrentTimeSecond() - i;
                String toSign = image.getSize() + "|" + timestamp;
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(request.getIdPlanDate().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
                byte[] hash = mac.doFinal(toSign.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (byte b : hash) {
                    sb.append(String.format("%02x", b));
                }
                String signature = sb.toString();
                serverSignature.add(signature);
            }

            if (!serverSignature.contains(request.getSignature())) {
                throw new RuntimeException();
            }

            if (onnxService.isFake(image.getBytes(), FACE_THRESHOLD_ANTIS_POOF)) {
                return RouterHelper.responseError("Ảnh quá mờ hoặc không thể nhận diện. Vui lòng thử lại");
            }

            float[] faceEmbedding = onnxService.getEmbedding(image.getBytes());

            float[] storedEmbedding = FaceRecognitionUtils.parseEmbedding(userStudent.getFaceEmbedding());

            boolean isMatch = FaceRecognitionUtils.isSameFace(faceEmbedding, storedEmbedding, FACE_THRESHOLD_CHECKIN);
            if (!isMatch) {
                return RouterHelper.responseError("Xác thực khuôn mặt thất bại");
            }

            ResponseEntity<?> response;

            if (attendance != null) {
                if (attendance.getAttendanceStatus() == AttendanceStatus.CHECKIN || !isEnableCheckin || !isEnableCheckout) {
                    response = RouterHelper.responseSuccess("Điểm danh thành công",
                            markPresent(attendance, planDate, userStudent));
                } else {
                    return RouterHelper.responseError("Không thể checkin/checkout ca này");
                }
            } else if (!isEnableCheckin || !isEnableCheckout) {
                response = RouterHelper.responseSuccess("Điểm danh thành công",
                        markPresent(attendance, planDate, userStudent));
            } else {
                attendance = new Attendance();
                attendance.setAttendanceStatus(AttendanceStatus.CHECKIN);
                attendance.setUserStudent(userStudent);
                attendance.setPlanDate(planDate);
                attendance.setLateCheckin(lateCheckin);
                attendance.setLateCheckout(lateCheckout);

                Attendance entity = attendanceRepository.save(attendance);
                sendMessageWS(planDate, userStudent);

                response = RouterHelper.responseSuccess("Checkin đầu giờ thành công", entity);
            }

            return response;
        } catch (Exception e) {
            return RouterHelper.responseError("Thông tin khuôn mặt không hợp lệ. Vui lòng thử lại");
        }
    }

    private Attendance markPresent(Attendance attendance, PlanDate planDate, UserStudent userStudent) {
        if (attendance == null) {
            attendance = new Attendance();
            attendance.setUserStudent(userStudent);
            attendance.setPlanDate(planDate);
        }
        attendance.setAttendanceStatus(AttendanceStatus.PRESENT);
        sendMessageWS(planDate, userStudent);
        return attendanceRepository.save(attendance);
    }

    private void sendMessageWS(PlanDate planDate, UserStudent userStudent) {
        AttendanceMessage attendanceMessage = new AttendanceMessage();
        attendanceMessage.setPlanDateId(planDate.getId());
        attendanceMessage.setUserStudentId(userStudent.getId());
        messagingTemplate.convertAndSend(RouteWebsocketConstant.TOPIC_ATTENDANCE, attendanceMessage);
    }

}
