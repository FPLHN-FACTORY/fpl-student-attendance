package udpm.hn.studentattendance.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import udpm.hn.studentattendance.entities.base.PrimaryEntity;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "attendance_recovery")
@DynamicUpdate
public class AttendanceRecovery extends PrimaryEntity implements Serializable {

    @Column(name = "name", length = EntityProperties.LENGTH_NAME)
    private String name;

    @Column(name = "description", length = EntityProperties.LENGTH_TEXT)
    private String description;

    @Column(name = "day")
    private Long day;

    @Column(name = "total_student")
    private Integer totalStudent;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "id_import_log")
    private ImportLog importLog;

    @ManyToOne
    @JoinColumn(name = "id_facility")
    private Facility facility;

}
