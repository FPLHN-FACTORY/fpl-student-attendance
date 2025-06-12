package udpm.hn.studentattendance.core.authentication.services.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.RedirectView;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationToken;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationStudentRegisterRequest;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationStudentUpdateFaceIDRequest;
import udpm.hn.studentattendance.core.authentication.oauth2.AuthUser;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationFacilityRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationSemesterRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserAdminRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStaffRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStudentRepository;
import udpm.hn.studentattendance.core.authentication.utils.JwtUtil;
import udpm.hn.studentattendance.core.notification.model.request.NotificationAddRequest;
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.helpers.NotificationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAuthenticationConstant;
import udpm.hn.studentattendance.core.authentication.services.AuthenticationService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.infrastructure.constants.SessionConstant;
import udpm.hn.studentattendance.utils.AppUtils;
import udpm.hn.studentattendance.utils.FaceRecognitionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    @PersistenceContext
    private EntityManager entityManager;

    private final HttpSession httpSession;

    private final SessionHelper sessionHelper;

    private final JwtUtil jwtUtil;

    private final NotificationService notificationService;

    private final AuthenticationFacilityRepository authenticationFacilityRepository;

    private final AuthenticationSemesterRepository authenticationSemesterRepository;

    private final AuthenticationUserAdminRepository authenticationUserAdminRepository;

    private final AuthenticationUserStaffRepository authenticationUserStaffRepository;

    private final AuthenticationUserStudentRepository authenticationUserStudentRepository;

    @Override
    public RedirectView authorSwitch(String role, String redirectUri, String facilityId) {
        httpSession.setAttribute(SessionConstant.LOGIN_ROLE, role);
        httpSession.setAttribute(SessionConstant.LOGIN_REDIRECT, redirectUri);
        httpSession.setAttribute(SessionConstant.LOGIN_FACILITY, facilityId);

        return new RedirectView(RouteAuthenticationConstant.REDIRECT_GOOGLE_AUTHORIZATION);
    }

    @Override
    public ResponseEntity<?> getAllFacility() {
        return RouterHelper.responseSuccess("Tải dữ liệu danh sách cơ sở thành công", authenticationFacilityRepository.findAllByStatusOrderByPositionAsc(EntityStatus.ACTIVE));
    }

    @Override
    public ResponseEntity<?> getAllSemester() {
        return RouterHelper.responseSuccess("Tải dữ liệu danh sách học kỳ thành công", authenticationSemesterRepository.findAllByStatusOrderByFromDateDesc(EntityStatus.ACTIVE));
    }

    @Override
    public ResponseEntity<?> getInfoUser(String role) {
        if (role == null) {
            return RouterHelper.responseError("Token đăng nhập không hợp lệ");
        }

        role = role.trim();
        String facilityID = sessionHelper.getFacilityId();
        AuthUser userData = null;

        if (role.equalsIgnoreCase(RoleConstant.ADMIN.name())) {
            Optional<UserAdmin> userAdmin = authenticationUserAdminRepository.findByEmail(sessionHelper.getUserEmail());
            if (userAdmin.isEmpty()) {
                return RouterHelper.responseError("Token đăng nhập không hợp lệ hoặc đã hết hạn");
            }
            userData = sessionHelper.buildAuthUser(userAdmin.get(), sessionHelper.getUserRole(), facilityID);
        } else if (role.equalsIgnoreCase(RoleConstant.STAFF.name())
                || role.equalsIgnoreCase(RoleConstant.TEACHER.name())) {
            Optional<UserStaff> userStaff = authenticationUserStaffRepository.findLogin(sessionHelper.getUserEmailFpt(),
                    facilityID);
            if (userStaff.isEmpty()) {
                return RouterHelper.responseError("Token đăng nhập không hợp lệ hoặc đã hết hạn");
            }
            userData = sessionHelper.buildAuthUser(userStaff.get(), sessionHelper.getUserRole(), facilityID);
        } else if (role.equalsIgnoreCase(RoleConstant.STUDENT.name())) {
            Optional<UserStudent> userStudent = facilityID != null && !facilityID.equalsIgnoreCase("null")
                    ? authenticationUserStudentRepository.findByEmailAndFacility_Id(sessionHelper.getUserEmail(),
                            facilityID)
                    : authenticationUserStudentRepository.findByEmail(sessionHelper.getUserEmail());
            if (userStudent.isEmpty()) {
                return RouterHelper.responseError("Token đăng nhập không hợp lệ hoặc đã hết hạn");
            }
            userData = sessionHelper.buildAuthUser(userStudent.get(), sessionHelper.getUserRole(), facilityID);
        }

        return RouterHelper.responseSuccess("Lấy thông tin người dùng thành công", userData);
    }

    @Override
    public ResponseEntity<?> studentRegister(AuthenticationStudentRegisterRequest request) {
        UserStudent student = authenticationUserStudentRepository.findById(sessionHelper.getUserId()).orElse(null);
        if (student == null) {
            return RouterHelper.responseError("Không tìm thấy sinh viên");
        }

        if (student.getFacility() != null) {
            return RouterHelper.responseError("Sinh viên đã tồn tại trên hệ thống");
        }

        Facility facility = authenticationFacilityRepository.findById(request.getIdFacility()).orElse(null);

        if (facility == null) {
            return RouterHelper.responseError("Cơ sở không tồn tại");
        }

        if (authenticationUserStudentRepository.isExistsCode(request.getCode(), student.getId(), facility.getId())) {
            return RouterHelper.responseError("Mã số sinh viên đã tồn tại trên cơ sở này");
        }

        if (request.getFaceEmbedding() == null || request.getFaceEmbedding().isEmpty()) {
            return RouterHelper.responseError("Thông tin khuôn mặt không hợp lệ");
        }

        List<double[]> faceEmbeddings = FaceRecognitionUtils.parseEmbeddings(request.getFaceEmbedding());
        if (isFaceExists(faceEmbeddings)) {
            return RouterHelper.responseError("Đã tồn tại khuôn mặt trên hệ thống");
        }

        student.setFacility(facility);
        student.setCode(request.getCode());
        student.setName(request.getName());
        student.setFaceEmbedding(Arrays.toString(faceEmbeddings.get(0)));
        authenticationUserStudentRepository.save(student);

        String accessToken = jwtUtil.generateToken(student.getEmail(),
                sessionHelper.buildAuthUser(student, Set.of(RoleConstant.STUDENT), student.getFacility().getId()));
        String refreshToken = jwtUtil.generateRefreshToken(accessToken);

        return RouterHelper.responseSuccess("Đăng ký thông tin sinh viên thành công", new AuthenticationToken(accessToken, refreshToken));
    }

    @Override
    public ResponseEntity<?> studentInfo() {
        UserStudent student = authenticationUserStudentRepository.findById(sessionHelper.getUserId()).orElse(null);
        if (student == null
                || student.getFacility() == null
                || !student.getFacility().getId().equals(sessionHelper.getFacilityId())) {
            return RouterHelper.responseError("Vui lòng đăng ký thông tin sinh viên");
        }

        if (student.getFaceEmbedding() != null && !student.getFaceEmbedding().isEmpty()) {
            student.setFaceEmbedding("OK");
        }

        return RouterHelper.responseSuccess("Lấy thông tinh sinh viên thành công", student);
    }

    @Override
    public ResponseEntity<?> studentUpdateFaceID(AuthenticationStudentUpdateFaceIDRequest request) {
        UserStudent student = authenticationUserStudentRepository.findById(sessionHelper.getUserId()).orElse(null);
        if (student == null) {
            return RouterHelper.responseError("Không tìm thấy sinh viên");
        }

        if (StringUtils.hasText(student.getFaceEmbedding())) {
            return RouterHelper.responseError("Không thể cập nhật khuôn mặt tài khoản này");
        }

        if (request.getFaceEmbedding() == null || request.getFaceEmbedding().isEmpty()) {
            return RouterHelper.responseError("Thông tin khuôn mặt không hợp lệ");
        }

        List<double[]> faceEmbeddings = FaceRecognitionUtils.parseEmbeddings(request.getFaceEmbedding());
        if (isFaceExists(faceEmbeddings)) {
            return RouterHelper.responseError("Đã tồn tại khuôn mặt trên hệ thống");
        }

        student.setFaceEmbedding(Arrays.toString(faceEmbeddings.get(0)));

        NotificationAddRequest notificationAddRequest = new NotificationAddRequest();
        notificationAddRequest.setType(NotificationHelper.TYPE_SUCCESS_UPDATE_FACE_ID);
        notificationAddRequest.setIdUser(student.getId());
        notificationService.add(notificationAddRequest);

        return RouterHelper.responseSuccess("Cập nhật khuôn mặt thành công",
                authenticationUserStudentRepository.save(student));
    }

    @Override
    public ResponseEntity<?> refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            return RouterHelper.responseError("Refresh Token không hợp lệ hoặc đã hết hạn");
        }

        String newToken = jwtUtil.generateToken(refreshToken);
        String newRefreshToken = jwtUtil.generateRefreshToken(newToken);

        AuthenticationToken data = new AuthenticationToken();
        data.setAccessToken(newToken);
        data.setRefreshToken(newRefreshToken);
        return RouterHelper.responseSuccess("Gia hạn token thành công", data);
    }

    @Override
    public ResponseEntity<?> getAvater(String urlImage) {
        return RouterHelper.responseSuccess("Lấy dữ liệu avatar thành công", AppUtils.imageUrlToBase64(urlImage));
    }

    private boolean isFaceExists(List<double[]> embeddings) {
        List<String> lstRawFaceEmbeddings = authenticationUserStudentRepository.getAllFaceEmbedding(sessionHelper.getFacilityId());
        List<double[]> lstFaceEmbeddings = lstRawFaceEmbeddings.stream().map(FaceRecognitionUtils::parseEmbedding).toList();

        if (lstFaceEmbeddings.isEmpty()) {
            return false;
        }

        int matching_faces = 0;
        for (double[] face: embeddings) {
            if(FaceRecognitionUtils.isSameFaces(lstFaceEmbeddings, face)) {
                matching_faces++;
            }
        }
        return matching_faces > 1;
    }

}
