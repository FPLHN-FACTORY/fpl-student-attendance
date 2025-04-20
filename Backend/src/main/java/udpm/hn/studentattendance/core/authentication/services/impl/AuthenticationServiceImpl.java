package udpm.hn.studentattendance.core.authentication.services.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.RedirectView;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationStudentRegisterRequest;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationStudentUpdateFaceIDRequest;
import udpm.hn.studentattendance.core.authentication.oauth2.AuthUser;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationFacilityRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationRoleRepository;
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
import udpm.hn.studentattendance.utils.FaceRecognitionUtils;

import java.io.IOException;
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
    public List<Facility> getAllFacility() {
        return authenticationFacilityRepository.findAllByStatusOrderByPositionAsc(EntityStatus.ACTIVE);
    }

    @Override
    public AuthUser getInfoUser(String role) {
        if (role == null) {
            return null;
        }

        role = role.trim();
        String facilityID = sessionHelper.getFacilityId();
        AuthUser userData = null;

        if (role.equalsIgnoreCase(RoleConstant.ADMIN.name())) {
            Optional<UserAdmin> userAdmin = authenticationUserAdminRepository.findByEmail(sessionHelper.getUserEmail());
            if (userAdmin.isEmpty()) {
                return null;
            }
            userData = sessionHelper.buildAuthUser(userAdmin.get(), sessionHelper.getUserRole(), facilityID);
        } else if(role.equalsIgnoreCase(RoleConstant.STAFF.name()) || role.equalsIgnoreCase(RoleConstant.TEACHER.name())) {
            Optional<UserStaff> userStaff = authenticationUserStaffRepository.findLogin(sessionHelper.getUserEmailFpt(), facilityID);
            if (userStaff.isEmpty()) {
                return null;
            }
            userData = sessionHelper.buildAuthUser(userStaff.get(), sessionHelper.getUserRole(), facilityID);
        } else if (role.equalsIgnoreCase(RoleConstant.STUDENT.name())) {
            Optional<UserStudent> userStudent = facilityID != null && !facilityID.equalsIgnoreCase("null") ? authenticationUserStudentRepository.findByEmailAndFacility_Id(sessionHelper.getUserEmail(), facilityID) : authenticationUserStudentRepository.findByEmail(sessionHelper.getUserEmail());
            if (userStudent.isEmpty()) {
                return null;
            }
            userData = sessionHelper.buildAuthUser(userStudent.get(), sessionHelper.getUserRole(), facilityID);
        }

        return userData;
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

        double[] faceEmbedding = FaceRecognitionUtils.parseEmbedding(request.getFaceEmbedding());
        if (isFaceExists(faceEmbedding)) {
            return RouterHelper.responseError("Đã tồn tại khuôn mặt trên hệ thống");
        }

        student.setFacility(facility);
        student.setCode(request.getCode());
        student.setName(request.getName());
        student.setFaceEmbedding(request.getFaceEmbedding());
        authenticationUserStudentRepository.save(student);

        String token = jwtUtil.generateToken(student.getEmail(), sessionHelper.buildAuthUser(student, Set.of(RoleConstant.STUDENT), student.getFacility().getId()));
        return RouterHelper.responseSuccess("Đăng ký thông tin sinh viên thành công", token);
    }

    @Override
    public ResponseEntity<?> studentInfo() {
        UserStudent student = authenticationUserStudentRepository.findById(sessionHelper.getUserId()).orElse(null);
        if (student == null
                || student.getFacility() == null
                || !student.getFacility().getId().equals(sessionHelper.getFacilityId())) {
            return RouterHelper.responseError("Vui lòng đăng ký thông tin sinh viên");
        }

        if (StringUtils.hasText(student.getFaceEmbedding())) {
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

        double[] faceEmbedding = FaceRecognitionUtils.parseEmbedding(request.getFaceEmbedding());
        if (isFaceExists(faceEmbedding)) {
            return RouterHelper.responseError("Đã tồn tại khuôn mặt trên hệ thống");
        }

        student.setFaceEmbedding(request.getFaceEmbedding());

        NotificationAddRequest notificationAddRequest = new NotificationAddRequest();
        notificationAddRequest.setType(NotificationHelper.TYPE_SUCCESS_UPDATE_FACE_ID);
        notificationAddRequest.setIdUser(student.getId());
        notificationService.add(notificationAddRequest);

        return RouterHelper.responseSuccess("Cập nhật khuôn mặt thành công", authenticationUserStudentRepository.save(student));
    }

    private boolean isFaceExists(double[] embedding) {
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(*) > 0 FROM user_student WHERE face_embedding IS NOT NULL AND face_embedding <> '' AND ");

        queryBuilder.append("SQRT(");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) queryBuilder.append(" + ");
            queryBuilder.append("POW(IFNULL(JSON_EXTRACT(face_embedding, '$[").append(i).append("]'), 0) - :e")
                    .append(i).append(", 2)");
        }
        queryBuilder.append(") < :threshold");

        Query query = entityManager.createNativeQuery(queryBuilder.toString());

        for (int i = 0; i < embedding.length; i++) {
            query.setParameter("e" + i, embedding[i]);
        }

        query.setParameter("threshold", FaceRecognitionUtils.THRESHOLD);

        Number count = (Number) query.getSingleResult();
        return count != null && count.longValue() > 0;
    }

}
