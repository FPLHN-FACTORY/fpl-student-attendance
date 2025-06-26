package udpm.hn.studentattendance.core.student.attendance.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.config.websocket.model.message.AttendanceMessage;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteWebsocketConstant;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;
import udpm.hn.studentattendance.utils.AppUtils;
import udpm.hn.studentattendance.utils.DateTimeUtils;
import udpm.hn.studentattendance.utils.FaceRecognitionUtils;
import udpm.hn.studentattendance.utils.GeoUtils;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SAAttendanceServiceImpl implements SAAttendanceService {

    private final SessionHelper sessionHelper;

    private final SAAttendanceRepository attendanceRepository;

    private final SAPlanDateRepository planDateRepository;

    private final SAUserStudentFactoryRepository userStudentFactoryRepository;

    private final SAFacilityIPRepository facilityIPRepository;

    private final SAFacilityLocationRepository facilityLocationRepository;

    private final HttpServletRequest httpServletRequest;

    private final SimpMessagingTemplate messagingTemplate;


    @Value("${app.config.attendance.early-checkin}")
    private int EARLY_CHECKIN;

    @Value("${app.config.face.threshold_checkin}")
    private double threshold_checkin;

    @Override
    public ResponseEntity<?> getAllList(SAFilterAttendanceRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        request.setIdUserStudent(sessionHelper.getUserId());

        // Không cache dữ liệu điểm danh vì cần độ chính xác cao và thay đổi thường
        // xuyên
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<SAAttendanceResponse> data = PageableObject
                .of(attendanceRepository.getAllByFilter(pageable, request));

        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> checkin(SACheckinAttendanceRequest request) {
        PlanDate planDate = planDateRepository.findById(request.getIdPlanDate()).orElse(null);
        if (planDate == null
                || !Objects.equals(
                        planDate.getPlanFactory().getFactory().getProject().getSubjectFacility().getFacility().getId(),
                        sessionHelper.getFacilityId())) {
            return RouterHelper.responseError("Không tìm thấy lịch học");
        }

        UserStudentFactory userStudentFactory = userStudentFactoryRepository.findByUserStudent_IdAndFactory_Id(
                sessionHelper.getUserId(), planDate.getPlanFactory().getFactory().getId()).orElse(null);
        if (userStudentFactory == null) {
            return RouterHelper.responseError("Ca học không tồn tại");
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
                return RouterHelper.responseError("Ca học đã được điểm danh");
            }
            if (attendance.getAttendanceStatus() == AttendanceStatus.ABSENT) {
                return RouterHelper.responseError("Bạn đã bị huỷ điểm danh.");
            }
        }

        SAAttendanceRecoveryResponse attendanceRecovery = attendanceRepository
                .getAttendanceRecovery(planDate.getId(), userStudentFactory.getUserStudent().getId()).orElse(null);
        boolean isRecovery = attendanceRecovery != null
                && attendanceRecovery.getTotalLateAttendance() > attendanceRecovery.getCurrentLateAttendance();

        Long lateCheckin = null;
        Long lateCheckout = null;

        if (attendance == null || attendance.getAttendanceStatus() == AttendanceStatus.NOTCHECKIN) {
            if (isEnableCheckin || !isEnableCheckout) {
                if (DateTimeUtils.getCurrentTimeMillis() <= planDate.getStartDate()
                        - (long) EARLY_CHECKIN * 60 * 1000) {
                    return RouterHelper
                            .responseError("Chưa đến giờ " + (isEnableCheckin ? "checkin đầu giờ" : "điểm danh"));
                }

                if (DateTimeUtils.getCurrentTimeMillis() > planDate.getStartDate()
                        + planDate.getLateArrival() * 60 * 1000) {
                    if (!isRecovery) {
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
                    if (!isRecovery) {
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
                if (!isRecovery) {
                    return RouterHelper.responseError("Đã quá giờ checkout cuối giờ");
                }
                attendance.setLateCheckout(Calendar.getInstance().getTimeInMillis());
            }

        }

        List<double[]> inputEmbedding = FaceRecognitionUtils.parseEmbeddings(request.getFaceEmbedding());
        double[] storedEmbedding = FaceRecognitionUtils.parseEmbedding(userStudent.getFaceEmbedding());
        boolean isMatch = FaceRecognitionUtils.isSameFaces(inputEmbedding, storedEmbedding, threshold_checkin);
        if (!isMatch) {
            return RouterHelper.responseError("Xác thực khuôn mặt thất bại");
        }

        ResponseEntity<?> response;

        if (attendance != null) {
            if (attendance.getAttendanceStatus() == AttendanceStatus.CHECKIN || !isEnableCheckin || !isEnableCheckout) {
                response = RouterHelper.responseSuccess("Điểm danh thành công",
                        markPresent(attendance, planDate, userStudent));
            } else {
                return RouterHelper.responseError("Không thể checkin/checkout ca học này");
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
