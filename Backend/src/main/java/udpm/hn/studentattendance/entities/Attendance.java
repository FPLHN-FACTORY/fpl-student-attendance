package udpm.hn.studentattendance.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import udpm.hn.studentattendance.entities.base.PrimaryEntity;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "attendance")
@DynamicUpdate
@ToString
public class Attendance extends PrimaryEntity implements Serializable {

    @ManyToOne
    @JoinColumn(name = "id_plan_date")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PlanDate planDate;

    @ManyToOne
    @JoinColumn(name = "id_user_student")
    private UserStudent userStudent;

    @Column(name = "attendance_status")
    private AttendanceStatus attendanceStatus = AttendanceStatus.NOTCHECKIN;

}
