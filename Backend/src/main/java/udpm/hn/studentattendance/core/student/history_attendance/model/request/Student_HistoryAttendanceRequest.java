package udpm.hn.studentattendance.core.student.history_attendance.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class Student_HistoryAttendanceRequest extends PageableRequest {

    private String studentFactoryId;

    private String factoryId;

    private String semesterId;


    public String getFactoryId() {
        return (factoryId == null || factoryId.trim().isEmpty()) ? null : factoryId;
    }

    public String getSemesterId() {
        return (semesterId == null || semesterId.trim().isEmpty()) ? null : semesterId;
    }

    public String getStudentFactoryId() {
        return (studentFactoryId == null || studentFactoryId.trim().isEmpty()) ? null : studentFactoryId;
    }
}

