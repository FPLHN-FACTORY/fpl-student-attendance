package udpm.hn.studentattendance.helpers;

import udpm.hn.studentattendance.core.notification.model.response.NotificationResponse;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

import java.util.Map;

public class NotificationHelper {

    public static final int TYPE_REMOVE_FACE_ID = 0;
    public static final int TYPE_ADD_ROLE = 1;
    public static final int TYPE_REMOVE_ROLE = 2;
    public static final int TYPE_ADD_TEACHER_TO_FACTORY = 3;
    public static final int TYPE_REMOVE_TEACHER_TO_FACTORY = 4;
    public static final int TYPE_ADD_STUDENT_TO_FACTORY = 5;
    public static final int TYPE_REMOVE_STUDENT_TO_FACTORY = 6;
    public static final int TYPE_SUCCESS_UPDATE_FACE_ID = 7;
    public static final int TYPE_ADD_ADMIN = 8;
    public static final int TYPE_UPDATE_ADMIN = 9;
    public static final int TYPE_CHANGE_POWER_SHIFT = 10;

    public static final String KEY_USER_ADMIN = "USER_ADMIN";
    public static final String KEY_USER_STAFF = "USER_STAFF";
    public static final String KEY_ROLE = "NAME_ROLE";
    public static final String KEY_FACTORY = "NAME_FACTORY";

    public static String renderMessage(NotificationResponse notification) {
        StringBuilder result = new StringBuilder();
        Map<String, Object> data = notification.getData();
        switch (notification.getType()) {
            case TYPE_REMOVE_FACE_ID -> {
                result
                        .append("Dữ liệu khuôn mặt của bạn vừa được đặt lại bởi <b>")
                        .append(data.get(KEY_USER_STAFF))
                        .append("</b>");
            }
            case TYPE_ADD_ROLE -> {
                String roleName = getRoleName((String) data.get(KEY_ROLE));
                result
                        .append("Bạn vừa được cấp quyền <b>")
                        .append(roleName)
                        .append("</b>")
                        .append(" bởi <b>")
                        .append(data.get(KEY_USER_ADMIN))
                        .append("</b>");
            }
            case TYPE_REMOVE_ROLE -> {
                String roleName = getRoleName((String) data.get(KEY_ROLE));
                result
                        .append("Bạn đã bị xoá quyền <b>")
                        .append(roleName)
                        .append("</b>")
                        .append(" bởi <b>")
                        .append(data.get(KEY_USER_ADMIN))
                        .append("</b>");
            }
            case TYPE_ADD_TEACHER_TO_FACTORY -> {
                result
                        .append("Bạn vừa trở thành giảng viên của nhóm <b>")
                        .append(data.get(KEY_FACTORY))
                        .append("</b>")
                        .append(" bởi <b>")
                        .append(data.get(KEY_USER_ADMIN))
                        .append("</b>");
            }
            case TYPE_REMOVE_TEACHER_TO_FACTORY -> {
                result
                        .append("Bạn không còn là giảng viên của nhóm <b>")
                        .append(data.get(KEY_FACTORY))
                        .append("</b>")
                        .append(" bởi <b>")
                        .append(data.get(KEY_USER_ADMIN))
                        .append("</b>");
            }
            case TYPE_ADD_STUDENT_TO_FACTORY -> {
                result
                        .append("Bạn vừa trở thành sinh viên của nhóm <b>")
                        .append(data.get(KEY_FACTORY))
                        .append("</b>")
                        .append(" bởi <b>")
                        .append(data.get(KEY_USER_STAFF))
                        .append("</b>");
            }
            case TYPE_REMOVE_STUDENT_TO_FACTORY -> {
                result
                        .append("Bạn không còn là sinh viên của nhóm <b>")
                        .append(data.get(KEY_FACTORY))
                        .append("</b>")
                        .append(" bởi <b>")
                        .append(data.get(KEY_USER_STAFF))
                        .append("</b>");
            }
            case TYPE_SUCCESS_UPDATE_FACE_ID -> {
                result
                        .append("Cập nhật thông tin khuôn mặt thành công");
            }
            case TYPE_ADD_ADMIN -> {
                result
                        .append("Bạn vừa thêm <b>")
                        .append(data.get(KEY_USER_STAFF))
                        .append("<b>")
                        .append(" với quyền admin")
                        .append("<b>");
            }
            case TYPE_UPDATE_ADMIN -> {
                result
                        .append("Bạn vừa sửa <b>")
                        .append(data.get(KEY_USER_ADMIN))
                        .append("<b>")
                        .append(" với quyền admin")
                        .append("<b>");
            }
        }
        return result.toString();
    }

    private static String getRoleName(String r) {
        String roleName;
        try {
            RoleConstant role = RoleConstant.valueOf(r);
            switch (role) {
                case ADMIN -> roleName = "Cán bộ đào tạo";
                case STAFF -> roleName = "Giảng viên";
                case STUDENT -> roleName = "Sinh viên";
                default -> roleName = role.name();
            }
        } catch (Exception e) {
            roleName = "Chức vụ không tồn tại";
        }
        return roleName;
    }
}
