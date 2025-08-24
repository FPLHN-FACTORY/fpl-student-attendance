package udpm.hn.studentattendance.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import udpm.hn.studentattendance.core.notification.model.response.NotificationResponse;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class NotificationHelperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void canInstantiate() {
        NotificationHelper helper = new NotificationHelper();
        assertThat(helper).isNotNull();
    }

    @Test
    void testRenderMessageRemoveFaceId() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put(NotificationHelper.KEY_USER_STAFF, "StaffName");
        String dataJson = objectMapper.writeValueAsString(data);

        NotificationResponse notification = new NotificationResponse("1", NotificationHelper.TYPE_REMOVE_FACE_ID,
                dataJson, EntityStatus.ACTIVE, 1234567890L);
        String result = NotificationHelper.renderMessage(notification);
        assertThat(result).contains("đặt lại bởi <b>StaffName</b>");
    }

    @Test
    void testRenderMessageAddRoleWithValidRole() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put(NotificationHelper.KEY_ROLE, RoleConstant.ADMIN.name());
        data.put(NotificationHelper.KEY_USER_ADMIN, "AdminName");
        String dataJson = objectMapper.writeValueAsString(data);

        NotificationResponse notification = new NotificationResponse("1", NotificationHelper.TYPE_ADD_ROLE, dataJson,
                EntityStatus.ACTIVE, 1234567890L);
        String result = NotificationHelper.renderMessage(notification);
        assertThat(result).contains("cấp quyền <b>Cán bộ đào tạo</b>");
        assertThat(result).contains("bởi <b>AdminName</b>");
    }

    @Test
    void testRenderMessageAddRoleWithInvalidRole() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put(NotificationHelper.KEY_ROLE, "INVALID_ROLE");
        data.put(NotificationHelper.KEY_USER_ADMIN, "AdminName");
        String dataJson = objectMapper.writeValueAsString(data);

        NotificationResponse notification = new NotificationResponse("1", NotificationHelper.TYPE_ADD_ROLE, dataJson,
                EntityStatus.ACTIVE, 1234567890L);
        String result = NotificationHelper.renderMessage(notification);
        assertThat(result).contains("cấp quyền <b>Chức vụ không tồn tại</b>");
    }

    @Test
    void testRenderMessageRemoveRole() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put(NotificationHelper.KEY_ROLE, RoleConstant.STAFF.name());
        data.put(NotificationHelper.KEY_USER_ADMIN, "AdminName");
        String dataJson = objectMapper.writeValueAsString(data);

        NotificationResponse notification = new NotificationResponse("1", NotificationHelper.TYPE_REMOVE_ROLE, dataJson,
                EntityStatus.ACTIVE, 1234567890L);
        String result = NotificationHelper.renderMessage(notification);
        assertThat(result).contains("xoá quyền <b>Giảng viên</b>");
        assertThat(result).contains("bởi <b>AdminName</b>");
    }

    @Test
    void testRenderMessageAddTeacherToFactory() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put(NotificationHelper.KEY_FACTORY, "FactoryName");
        data.put(NotificationHelper.KEY_USER_ADMIN, "AdminName");
        String dataJson = objectMapper.writeValueAsString(data);

        NotificationResponse notification = new NotificationResponse("1",
                NotificationHelper.TYPE_ADD_TEACHER_TO_FACTORY, dataJson, EntityStatus.ACTIVE, 1234567890L);
        String result = NotificationHelper.renderMessage(notification);
        assertThat(result).contains("giảng viên của nhóm <b>FactoryName</b>");
        assertThat(result).contains("bởi <b>AdminName</b>");
    }

    @Test
    void testRenderMessageRemoveTeacherToFactory() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put(NotificationHelper.KEY_FACTORY, "FactoryName");
        data.put(NotificationHelper.KEY_USER_ADMIN, "AdminName");
        String dataJson = objectMapper.writeValueAsString(data);

        NotificationResponse notification = new NotificationResponse("1",
                NotificationHelper.TYPE_REMOVE_TEACHER_TO_FACTORY, dataJson, EntityStatus.ACTIVE, 1234567890L);
        String result = NotificationHelper.renderMessage(notification);
        assertThat(result).contains("không còn là giảng viên của nhóm <b>FactoryName</b>");
        assertThat(result).contains("bởi <b>AdminName</b>");
    }

    @Test
    void testRenderMessageAddStudentToFactory() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put(NotificationHelper.KEY_FACTORY, "FactoryName");
        data.put(NotificationHelper.KEY_USER_STAFF, "StaffName");
        String dataJson = objectMapper.writeValueAsString(data);

        NotificationResponse notification = new NotificationResponse("1",
                NotificationHelper.TYPE_ADD_STUDENT_TO_FACTORY, dataJson, EntityStatus.ACTIVE, 1234567890L);
        String result = NotificationHelper.renderMessage(notification);
        assertThat(result).contains("sinh viên của nhóm <b>FactoryName</b>");
        assertThat(result).contains("bởi <b>StaffName</b>");
    }

    @Test
    void testRenderMessageRemoveStudentToFactory() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put(NotificationHelper.KEY_FACTORY, "FactoryName");
        data.put(NotificationHelper.KEY_USER_STAFF, "StaffName");
        String dataJson = objectMapper.writeValueAsString(data);

        NotificationResponse notification = new NotificationResponse("1",
                NotificationHelper.TYPE_REMOVE_STUDENT_TO_FACTORY, dataJson, EntityStatus.ACTIVE, 1234567890L);
        String result = NotificationHelper.renderMessage(notification);
        assertThat(result).contains("không còn là sinh viên của nhóm <b>FactoryName</b>");
        assertThat(result).contains("bởi <b>StaffName</b>");
    }

    @Test
    void testRenderMessageSuccessUpdateFaceId() throws Exception {
        Map<String, Object> data = new HashMap<>();
        String dataJson = objectMapper.writeValueAsString(data);

        NotificationResponse notification = new NotificationResponse("1",
                NotificationHelper.TYPE_SUCCESS_UPDATE_FACE_ID, dataJson, EntityStatus.ACTIVE, 1234567890L);
        String result = NotificationHelper.renderMessage(notification);
        assertThat(result).contains("Cập nhật thông tin khuôn mặt thành công");
    }

    @Test
    void testRenderMessageAddAdmin() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put(NotificationHelper.KEY_USER_STAFF, "StaffName");
        String dataJson = objectMapper.writeValueAsString(data);

        NotificationResponse notification = new NotificationResponse("1", NotificationHelper.TYPE_ADD_ADMIN, dataJson,
                EntityStatus.ACTIVE, 1234567890L);
        String result = NotificationHelper.renderMessage(notification);
        assertThat(result).contains("thêm <b>StaffName<b>");
    }

    @Test
    void testRenderMessageUpdateAdmin() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put(NotificationHelper.KEY_USER_ADMIN, "AdminName");
        String dataJson = objectMapper.writeValueAsString(data);

        NotificationResponse notification = new NotificationResponse("1", NotificationHelper.TYPE_UPDATE_ADMIN,
                dataJson, EntityStatus.ACTIVE, 1234567890L);
        String result = NotificationHelper.renderMessage(notification);
        assertThat(result).contains("sửa <b>AdminName<b>");
    }
}
